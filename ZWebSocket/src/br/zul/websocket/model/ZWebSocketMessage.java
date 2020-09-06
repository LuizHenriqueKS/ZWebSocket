package br.zul.websocket.model;

import br.zul.websocket.client.ZWebSocket;


/**
 *
 * @author luizh
 */
public class ZWebSocketMessage {

    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    protected byte[] data;
    protected ZWebSocket socket;
    
    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    protected ZWebSocketMessage() {
        
    }
    
    //==========================================================================
    //GETTERS E SETTERS
    //==========================================================================
    public ZWebSocket getSocket() {
        return socket;
    }
    
    public String getText(){
        return new String(data);
    }
    
    public byte[] getData() {
        return data;
    }
    
}
