package br.zul.websocket.client;

import br.zul.websocket.listener.ZWebSocketMessageListener;
import br.zul.websocket.model.ZWebSocketMessage;
import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @author luizh
 */
public interface ZWebSocket extends Closeable {

    public ZWebSocket sendMessage(String data) throws IOException;
    public String getId();
    public boolean isConnected();
    public ZWebSocketMessage readMessage() throws IOException;
    public void autoReadMessage() throws IOException;
    
    public ZWebSocket addMessageListener(ZWebSocketMessageListener listener);
    public ZWebSocket removeMessageListener(ZWebSocketMessageListener listener);

}
