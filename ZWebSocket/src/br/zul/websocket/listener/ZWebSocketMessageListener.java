package br.zul.websocket.listener;

import br.zul.websocket.model.ZWebSocketMessage;

/**
 *
 * @author luizh
 */
public interface ZWebSocketMessageListener {
    
    public void onMessage(ZWebSocketMessage message);
    
}
