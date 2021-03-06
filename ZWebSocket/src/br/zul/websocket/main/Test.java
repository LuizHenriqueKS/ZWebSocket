package br.zul.websocket.main;

import br.zul.websocket.client.ZWebSocket;
import br.zul.websocket.client.ZWebSocketBuilder;
import br.zul.websocket.exception.ZWebSocketException;
import br.zul.websocket.exception.ZWebSocketHostInvalidException;
import br.zul.websocket.exception.ZWebSocketProtocolInvalidException;
import br.zul.websocket.model.ZWebSocketMessage;
import br.zul.websocket.util.StrHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author luizh
 */
class Test {
    
    //==========================================================================
    //MÉTODOS PÚBLICOS ESTÁTICOS
    //==========================================================================
    public static void connectToWsServer(String url) throws IOException, ZWebSocketProtocolInvalidException, ZWebSocketHostInvalidException, ZWebSocketException {
        ZWebSocket webSocket = new ZWebSocketBuilder()
                                        .setUrl(url)
                                        .addMessageListener(Test::printMessage)
                                        .build();
        webSocket.readMessage();
        webSocket.sendMessage("Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!");
    }
    
    public static void connectToWssSrver() throws IOException{
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) sf.createSocket("487e0ea97cde.ngrok.io", 443);
        socket.startHandshake();
        redirectToConsole(socket.getInputStream());
        redirectFromConsoleTo(socket.getOutputStream());
    }
    
    public static void startServerWsSimples() throws IOException, NoSuchAlgorithmException {
        ServerSocket server = new ServerSocket(3003);
        Socket socket;
        while ((socket = server.accept())!=null){
            InputStream is = socket.getInputStream();
            String headers = readString(is);
            System.out.println(headers);
            String key = new StrHelper(headers).from("Sec-WebSocket-Key: ").till("\n").toString().trim();
            String accept = sha1(key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11");  
            OutputStream os = socket.getOutputStream();
            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append("HTTP/1.1 101 Switching Protocols\n");
            responseBuilder.append("Upgrade: websocket\n");
            responseBuilder.append("Connection: Upgrade\n");
            responseBuilder.append("Sec-WebSocket-Accept: ").append(accept).append("\n\n");
            os.write(responseBuilder.toString().getBytes());
            //os.write(new byte[]{-120, 2, 3, -22});
            redirectToConsole(socket.getInputStream());
        }
    }
    
    //==========================================================================
    //MÉTODOS PRIVADOS ESTÁTICOS
    //==========================================================================
    private static void redirectToConsole(InputStream inputStream) {
        new Thread(()->{
            try{
                byte[] buffer = new byte[10];
                int len;
                while ((len = inputStream.read(buffer))!=-1){
                    System.out.println(new String(buffer, 0, len));
                }
            } catch (IOException ex){
                ex.printStackTrace(System.err);
            }
        }).start();
    }

    private static void redirectFromConsoleTo(OutputStream outputStream) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true){
            String line = scanner.nextLine();
            outputStream.write((line+"\r\n").getBytes());
            outputStream.flush();
        }
    }
    
    private static void printMessage(ZWebSocketMessage message){
        System.out.printf("%s: %s\r\n", message.getSocket().getId(), message.getText());
    }

    private static String readString(InputStream is) throws IOException {
        byte[] buffer = new byte[8196];
        int len = is.read(buffer);
        return new String(buffer, 0, len);
    }
    
    private static String sha1(String str) throws NoSuchAlgorithmException{
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
	digest.reset();
	digest.update(str.getBytes());
        return Base64.getEncoder().encodeToString(digest.digest());
    }

    static void startTunnel() throws IOException {
        ServerSocket server = new ServerSocket(3003);
        Socket client = server.accept();
        Socket socket = new Socket("agentcore.omnize.com.br", 80);
        redirect("clinet>server", client.getInputStream(), socket.getOutputStream());
        redirect("server>client", socket.getInputStream(), client.getOutputStream());
    }

    private static void redirect(String name, InputStream inputStream, OutputStream outputStream) {
        new Thread(()->{
            try {
                byte buffer[] = new byte[10000];
                int len;
                boolean first = true;
                while ((len=inputStream.read(buffer))!=-1){
                    String data = new String(buffer, 0, len);
                    System.out.printf("%s: %s\r\n", name, data);
                    if (first) {
                        outputStream.write(data.replace("localhost:3003", "agentcore.omnize.com.br").getBytes());
                    } else { 
                        for (int i=0;i<len;i++){
                            System.out.printf("%d|", buffer[i]);
                        }
                        System.out.println("");
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.flush();
                    first = false;
                }
                System.out.println("Fim: " + name);
            } catch (IOException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

}
