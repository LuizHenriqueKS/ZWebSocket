package br.zul.websocket.client;

import br.zul.websocket.exception.ZWebSocketAlreadStartedException;
import br.zul.websocket.exception.ZWebSocketException;
import br.zul.websocket.exception.ZWebSocketHostInvalidException;
import br.zul.websocket.exception.ZWebSocketProtocolInvalidException;
import br.zul.websocket.listener.ZWebSocketMessageListener;
import br.zul.websocket.model.ZWebSocketMessage;
import br.zul.websocket.model.ZWebSocketProxy;
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
import javax.net.ssl.SSLSocketFactory;

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
    protected ZWebSocketProxy proxy;
    
    protected Socket socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected Thread messageListenerThread;
    protected boolean alreadyStarted;
    protected boolean connected;
    protected boolean printHeaders;
    
    protected ZWebSocketServerMessageReader reader;
    
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
        sendData(129, data.getBytes(), new byte[]{});
        return this;
    }
    
    @Override
    public void close() throws IOException{
        connected = false;
        if (socket!=null) socket.close();
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
        //((SSLSocket)socket).startHandshake();
        readServerHeaders();
        reader = new ZWebSocketServerMessageReader(this, inputStream);
        connected = true;
    }
    
    @Override
    public ZWebSocketMessage readMessage() throws IOException {
        try {
            while (isConnected()) {
                ZWebSocketMessage message = reader.read();
                if (isPing(message)) {
                    sendPong(message);
                } else {
                    fireMessageListeners(message);
                    return message;
                }
            }
        } catch (IOException ex) {
            closeNoException();
            throw ex;
        }
        throw new IOException("closed");
    }

    @Override
    public void autoReadMessage() throws IOException {
        try {
            while (isConnected()) {
                readMessage();
            }
        } finally {
            closeNoException();
        }
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
        String host = proxy==null?getHost():proxy.getHost();
        Integer port = proxy==null?getPort():proxy.getPort();
        if (getProtocol().equalsIgnoreCase(PROTOCOL_WS)){
            socket = new Socket(host, port);
        } else {
            socket = SSLSocketFactory.getDefault().createSocket(host, port);
        }
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
    }
    
    private void sendClientHeaders() throws IOException {
        StringBuilder headersBuilder = new StringBuilder();
        headersBuilder.append("GET ").append(getPath()).append(" HTTP/1.1\r\n");
        if (isDefaultPort()){
            headersBuilder.append("Host: ").append(getHost()).append("\r\n");
        } else {
            headersBuilder.append("Host: ").append(getHost()).append(":").append(getPort()).append("\r\n");
        }
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
        if (printHeaders) System.out.println(headersBuilder.toString());
        send(headersBuilder.toString()/*.replace("\r\n", "\n")*/);
    }

    private void readServerHeaders() throws IOException {
        StringBuilder headersBuilder = new StringBuilder();
        byte[] buffer = new byte[1];
        int len;
        while ((len=inputStream.read(buffer))!=-1){
            headersBuilder.append(new String(buffer, 0, len));
            if (headersBuilder.toString().replace("\r\n", "\n").endsWith("\n\n")) break;
        }
        if (printHeaders) System.out.println(headersBuilder.toString());
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

    private void sendData(int type, byte[] data, byte[] end) throws IOException {
        MemoryStream stream = new MemoryStream();
        byte[] dataDecoded = data;
        int length = dataDecoded.length;
        stream.write1Byte(type);
        if (length<=125){
            stream.write1Byte(length - 128);
        } else if (length<256*256) {
            stream.write1Byte(126 - 128);
            stream.write2Bytes(length);
        } else { 
            stream.write1Byte(127 - 128);
            stream.write8Bytes(length);
        }
        byte[] key = generateByteArray(4);
        stream.writeBytes(key);
        for (int i=0;i<dataDecoded.length;i++){
            stream.write1Byte((byte) (dataDecoded[i] ^ (key[i % 4])));
        }
        if (end!=null) stream.writeBytes(end);
        outputStream.write(stream.toByteArray());
        outputStream.flush();
    }

    private boolean isPing(ZWebSocketMessage message) {
        return message.getEncodedData()[0] == -120;
    }

    private void sendPong(ZWebSocketMessage message) throws IOException {
        sendData(-120, message.getDecodedData(), null);
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

    private boolean isDefaultPort() {
        if (getPort()==80&&getProtocol().equalsIgnoreCase(PROTOCOL_WS)){
            return true;
        } else if (getPort()==443&&getProtocol().equalsIgnoreCase(PROTOCOL_WSS)){
            return true;
        } else {
            return false;
        }
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
