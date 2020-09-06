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
    private byte[] decodedData;
    private byte[] encodedData;
    
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
        Objects.requireNonNull(decodedData);
    }

    private ZWebSocketMessage implement() {
        ZWebSocketMessage message = new ZWebSocketMessage();
        message.decodedData = decodedData;
        message.encodedData = encodedData;
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

    public byte[] getDecodedData() {
        return decodedData;
    }
    public ZWebSocketMessageBuilder setDecodedData(byte[] decodedData) {
        this.decodedData = decodedData;
        return this;
    }

    public byte[] getEncodedData() {
        return encodedData;
    }
    public ZWebSocketMessageBuilder setEncodedData(byte[] encodedData) {
        this.encodedData = encodedData;
        return this;
    }
    
}
