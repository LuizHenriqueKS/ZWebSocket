package br.zul.websocket.model;

import br.zul.websocket.client.ZWebSocket;
import java.util.Objects;

/**
 *
 * @author luizh
 */
public class ZWebSocketMessageBuilder {
    
    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    private ZWebSocket socket;
    private byte[] data;
    
    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    public ZWebSocketMessageBuilder() {
        
    }

    //==========================================================================
    //MÉTODOS PÚBLICOS
    //==========================================================================
    public ZWebSocketMessage build(){
        validate();
        return implement();
    }
    
    //==========================================================================
    //MÉTODOS PRIVADOS
    //==========================================================================
    private void validate() {
        Objects.requireNonNull(socket);
        Objects.requireNonNull(data);
    }

    private ZWebSocketMessage implement() {
        ZWebSocketMessage message = new ZWebSocketMessage();
        message.data = data;
        message.socket = socket;
        return message;
    }
    
    //==========================================================================
    //GETTERS E SETTERS
    //==========================================================================
    public ZWebSocket getSocket() {
        return socket;
    }
    public ZWebSocketMessageBuilder setSocket(ZWebSocket socket) {
        this.socket = socket;
        return this;
    }

    public byte[] getData() {
        return data;
    }
    public ZWebSocketMessageBuilder setData(byte[] data) {
        this.data = data;
        return this;
    }
    
}
