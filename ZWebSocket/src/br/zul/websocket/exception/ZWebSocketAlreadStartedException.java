package br.zul.websocket.exception;

/**
 *
 * @author luizh
 */
public class ZWebSocketAlreadStartedException extends ZWebSocketException {

    public ZWebSocketAlreadStartedException() {
    }

    public ZWebSocketAlreadStartedException(String message) {
        super(message);
    }

    public ZWebSocketAlreadStartedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZWebSocketAlreadStartedException(Throwable cause) {
        super(cause);
    }

    public ZWebSocketAlreadStartedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
