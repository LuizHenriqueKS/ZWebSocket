package br.zul.websocket.model;


/**
 *
 * @author luizh
 */
public class ZWebSocketProxy {

    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    private String host;
    private int port;
    
    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    public ZWebSocketProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    //==========================================================================
    //GETTERS E SETTERS
    //==========================================================================
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    
}
