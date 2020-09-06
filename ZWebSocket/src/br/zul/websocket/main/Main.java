package br.zul.websocket.main;

import br.zul.websocket.exception.ZWebSocketException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author luizh
 */
public class Main {

    public static void main(String[] args) throws IOException, ZWebSocketException, NoSuchAlgorithmException {
        Test.connectToWsServer("ws://localhost:3000");
        //Test.connectToWsServer("wss://aa56c12af061.ngrok.io");
        //Test.startServerWsSimples();
        //Test.startTunnel();
    }
    
}
