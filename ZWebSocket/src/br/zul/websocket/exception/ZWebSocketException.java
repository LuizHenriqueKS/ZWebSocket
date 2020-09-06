package br.zul.websocket.exception;

/**
 *
 * @author luizh
 */
public class ZWebSocketException extends Exception {

    public ZWebSocketException() {
    }

    public ZWebSocketException(String message) {
        super(message);
    }

    public ZWebSocketException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZWebSocketException(Throwable cause) {
        super(cause);
    }

    public ZWebSocketException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
