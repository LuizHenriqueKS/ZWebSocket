package br.zul.websocket.client;

import br.zul.websocket.exception.ZWebSocketAlreadStartedException;
import br.zul.websocket.exception.ZWebSocketException;
import br.zul.websocket.exception.ZWebSocketHostInvalidException;
import br.zul.websocket.exception.ZWebSocketProtocolInvalidException;
import br.zul.websocket.listener.ZWebSocketMessageListener;
import br.zul.websocket.model.ZWebSocketMessage;
import br.zul.websocket.reader.ZWebSocketServerMessageReader;
import br.zul.websocket.util.MemoryStream;
import br.zul.websocket.util.StrHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import static jdk.nashorn.internal.objects.ArrayBufferView.buffer;

/**
 *
 * @author luizh
 */
class ZWebSocketCientSideImpl implements ZWebSocket {
    
    //==========================================================================
    //CONSTANTES
    //==========================================================================
    public static final String PROTOCOL_WS = "ws";
    public static final String PROTOCOL_WSS = "wss";
    
    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    protected final String id;
    
    protected String url;
    protected List<ZWebSocketMessageListener> messageListenerList;
    protected Map<String, List<String>> requestPropertyMap;
    
    protected Socket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected Thread messageListenerThread;
    protected boolean alreadyStarted;
    protected boolean connected;
    
    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    protected ZWebSocketCientSideImpl() {
        this.id = UUID.randomUUID().toString().substring(0, 16);
    }
    
    //==========================================================================
    //MÉTODOS PÚBLICOS
    //==========================================================================
    @Override
    public synchronized ZWebSocket sendMessage(String data) throws IOException {
        MemoryStream stream = new MemoryStream();
        byte[] dataDecoded = data.getBytes();
        int length = dataDecoded.length;
        stream.write1Byte(129);
        if (length<=125){
            stream.write1Byte(length - 128);
        } else if (length<255*255) {
            stream.write1Byte(126);
            stream.write2Bytes(length);
        } else { 
            stream.write1Byte(127);
            stream.write8Bytes(length);
        }
        byte[] key = generateByteArray(4);
        stream.writeBytes(key);
        for (int i=0;i<dataDecoded.length;i++){
            stream.write1Byte((byte) (dataDecoded[i] ^ (key[i % 4])));
        }
        stream.write1Byte(0);
        outputStream.write(stream.toByteArray());
        outputStream.flush();
        return this;
    }
    
    @Override
    public void close() throws IOException{
        connected = false;
        stopMessageListenerThread();
        socket.close();
        socket = null;
        inputStream = null;
        outputStream = null;
    }

    @Override
    public ZWebSocket addMessageListener(ZWebSocketMessageListener listener) {
        messageListenerList.add(listener);
        return this;
    }

    @Override
    public ZWebSocket removeMessageListener(ZWebSocketMessageListener listener) {
        messageListenerList.remove(listener);
        return this;
    }
    
    //==========================================================================
    //MÉTODOS PROTEGIDOS
    //==========================================================================
    protected void connect() throws IOException, ZWebSocketProtocolInvalidException, ZWebSocketHostInvalidException, ZWebSocketException {
        validateProtocol();
        validateHost();
        validateAlreadyStarted();
        alreadyStarted = true;
        connectSocket();
        sendClientHeaders();
        readServerHeaders();
        startMessageListenerThread();
        connected = true;
    }
    
    //==========================================================================
    //MÉTODOS PRIVADOS
    //==========================================================================
    private void validateProtocol() throws ZWebSocketProtocolInvalidException {
        String protocol = getProtocol();
        if (protocol.equalsIgnoreCase(PROTOCOL_WS)) return;
        if (protocol.equalsIgnoreCase(PROTOCOL_WSS)) return;
        throw new ZWebSocketProtocolInvalidException(protocol);
    }

    private void validateHost() throws ZWebSocketHostInvalidException {
        String host = getHost();
        if (host.isEmpty()||host.contains(":")||host.contains("/")) {
            throw new ZWebSocketHostInvalidException(host);
        }
    }

    private void validateAlreadyStarted() throws ZWebSocketException {
        if (alreadyStarted) {
            throw new ZWebSocketAlreadStartedException();
        }
    }

    private void connectSocket() throws ZWebSocketProtocolInvalidException, IOException {
        validateProtocol();
        if (getProtocol().equalsIgnoreCase(PROTOCOL_WS)){
            socket = new Socket(getHost(), getPort());
        } else {
            socket = SSLSocketFactory.getDefault().createSocket();
            ((SSLSocket)socket).startHandshake();
        }
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }
    
    private void sendClientHeaders() throws IOException {
        StringBuilder headersBuilder = new StringBuilder();
        headersBuilder.append("GET ").append(getPath()).append(" HTTP/1.1\r\n");
        headersBuilder.append("Host: ").append(getHost()).append(":").append(getPort()).append("\r\n");
        requestPropertyMap.forEach((key, valList)->{
            valList.forEach(val->{
               headersBuilder.append(key).append(": ").append(val).append("\r\n");
            });
        });
        /*if (!requestPropertyMap.containsKey("Origin")){
            String origin = getProtocol().replace("wss", "https").replace("ws", "http") + "://" + getHost() + ":" +getPort();
            headersBuilder.append("Origin: ").append(origin).append("\r\n");    
        }*/
        headersBuilder.append("Sec-WebSocket-Key: ").append(getSecWebSocketKey()).append("\r\n");
        headersBuilder.append("\r\n");
        System.out.println(headersBuilder.toString());
        send(headersBuilder.toString().replace("\r\n", "\n"));
    }

    private void readServerHeaders() throws IOException {
        StringBuilder headersBuilder = new StringBuilder();
        byte[] buffer = new byte[1];
        int len;
        while ((len=inputStream.read(buffer))!=-1){
            headersBuilder.append(new String(buffer, 0, len));
            if (headersBuilder.toString().replace("\r\n", "\n").endsWith("\n\n")) break;
        }
        System.out.println(headersBuilder.toString());
    }

    private void startMessageListenerThread() {
        messageListenerThread = new Thread(()->{
            try {
                ZWebSocketServerMessageReader reader = new ZWebSocketServerMessageReader(this, inputStream);
                while (isConnected()) {
                   ZWebSocketMessage message = reader.read();
                   fireMessageListeners(message);
                }
            } catch (IOException ex) {
                Logger.getLogger(ZWebSocketCientSideImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                closeNoException();
            }
        });
        messageListenerThread.start();
    }
    
    private void stopMessageListenerThread() {
        if (messageListenerThread!=null){
            messageListenerThread.interrupt();
            messageListenerThread = null;
        }
    }

    private void closeNoException() {
        try {
            close();
        } catch (IOException ex){
            ex.printStackTrace(System.err);
        }
    }
    
    private void send(String str) throws IOException {
        outputStream.write(str.getBytes());
    }
    
    //==========================================================================
    //GETTERS E SETTERS MODIFICADOS
    //==========================================================================
    public String getProtocol(){
        int index = url.indexOf("://");
        if (index==-1) return "";
        return url.substring(0, index);
    }
    
    public String getHost(){
        return new StrHelper(url)
                        .from(getProtocol()+"://")
                        .till(":", "/")
                        .toString();
    }
    
    private String getPath() {
        StrHelper strHelper = new StrHelper(url).from("://");
        if (strHelper.contains("/")){
            return "/" + strHelper.from("/").toString();
        } else {
            return "/";
        }                        
    }
    
    public int getPort(){
        try {
            String portStr = new StrHelper(url).from(":").from(":").till("/").toString();
            return Integer.parseInt(portStr);
        } catch (NumberFormatException ex){
            return getProtocol().equalsIgnoreCase(PROTOCOL_WS)?80:443;
        }
    }
    
    private String getSecWebSocketKey() {
        return Base64.getEncoder().encodeToString(getId().getBytes());
    }

    private void fireMessageListeners(ZWebSocketMessage message) {
        messageListenerList.forEach(listener->listener.onMessage(message));
    }
    
    private byte[] generateByteArray(int length) {
        byte[] array = new byte[length];
        Random random = new SecureRandom();
        random.nextBytes(array);
        return array;
    }
    
    //==========================================================================
    //GETTERS E SETTERS
    //==========================================================================
    @Override
    public String getId(){
        return id;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

}
