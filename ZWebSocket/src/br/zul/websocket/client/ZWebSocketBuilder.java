package br.zul.websocket.client;

import br.zul.websocket.exception.ZWebSocketException;
import br.zul.websocket.exception.ZWebSocketHostInvalidException;
import br.zul.websocket.exception.ZWebSocketProtocolInvalidException;
import br.zul.websocket.listener.ZWebSocketMessageListener;
import br.zul.websocket.model.ZWebSocketProxy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author luizh
 */
public class ZWebSocketBuilder {

    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    private String url;
    private List<ZWebSocketMessageListener> messageListenerList;
    
    private Map<String, List<String>> requestPropertyMap;
    private boolean printHeaders;
    private ZWebSocketProxy proxy;
    
    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    public ZWebSocketBuilder(){
        this.messageListenerList = new ArrayList<>();
        this.requestPropertyMap = new LinkedHashMap<>();
        initDefaultRequestProperties();
    }
    
    //==========================================================================
    //MÉTODOS PÚBLICOS
    //==========================================================================
    public ZWebSocket build() throws IOException, ZWebSocketProtocolInvalidException, ZWebSocketHostInvalidException, ZWebSocketException{
        validate();
        return implement();
    }
    
    public ZWebSocketBuilder addMessageListener(ZWebSocketMessageListener listener){
        messageListenerList.add(listener);
        return this;
    }
    
    public ZWebSocketBuilder addRequestProperty(String key, String value){
        List<String> valueList = requestPropertyMap.get(key);
        if (valueList==null) valueList = new ArrayList<>();
        valueList.add(value);
        requestPropertyMap.put(key, valueList);
        return this;
    }
    
    public ZWebSocketBuilder setRequestProperty(String key, String value){
        requestPropertyMap.put(key, new ArrayList<>(Arrays.asList(value)));
        return this;
    }
    
    //==========================================================================
    //MÉTODOS PRIVADOS
    //==========================================================================
    private void initDefaultRequestProperties() {
        addRequestProperty("Connection", "Upgrade");
        addRequestProperty("Pragma", "no-cache");
        addRequestProperty("Cache-Control", "no-cache");
        addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
        addRequestProperty("Upgrade", "websocket");
        addRequestProperty("Sec-WebSocket-Version", "13");
        addRequestProperty("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
        addRequestProperty("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
        
    }
    
    private void validate(){
        Objects.requireNonNull(url);
        Objects.requireNonNull(messageListenerList);
    }
    
    private ZWebSocket implement() throws IOException, ZWebSocketProtocolInvalidException, ZWebSocketHostInvalidException, ZWebSocketException{
        ZWebSocketCientSideImpl socket = new ZWebSocketCientSideImpl();
        socket.url = url;
        socket.messageListenerList = messageListenerList;
        socket.requestPropertyMap = requestPropertyMap;
        socket.printHeaders = printHeaders;
        socket.proxy = proxy;
        socket.connect();
        return socket;
    }
    
    //==========================================================================
    //GETTERS E SETTERS
    //==========================================================================
    public String getUrl() {
        return url;
    }
    public ZWebSocketBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public List<ZWebSocketMessageListener> getMessageListenerList() {
        return messageListenerList;
    }
    public ZWebSocketBuilder setMessageListenerList(List<ZWebSocketMessageListener> messageListenerList) {
        this.messageListenerList = messageListenerList;
        return this;
    }

    public boolean isPrintHeaders() {
        return printHeaders;
    }
    public ZWebSocketBuilder setPrintHeaders(boolean printHeaders) {
        this.printHeaders = printHeaders;
        return this;
    }

    public ZWebSocketProxy getProxy() {
        return proxy;
    }

    public ZWebSocketBuilder setProxy(ZWebSocketProxy proxy) {
        this.proxy = proxy;
        return this;
    }
    
}
