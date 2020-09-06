package br.zul.websocket.main;

import br.zul.websocket.exception.ZWebSocketException;
import java.io.IOException;

/**
 *
 * @author luizh
 */
public class Main {

    public static void main(String[] args) throws IOException, ZWebSocketException {
        //Test.connectToWsServer("ws://localhost:3000");
        Test.connectToWsServer("wss://a910921c51dc.ngrok.io");
        //Test.startServerWsSimples();
    }
    
}
