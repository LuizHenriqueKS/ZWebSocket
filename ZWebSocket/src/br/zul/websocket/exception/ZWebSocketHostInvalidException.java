package br.zul.websocket.exception;

/**
 *
 * @author luizh
 */
public class ZWebSocketHostInvalidException extends ZWebSocketException {

    public ZWebSocketHostInvalidException() {
    }

    public ZWebSocketHostInvalidException(String message) {
        super(message);
    }

    public ZWebSocketHostInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZWebSocketHostInvalidException(Throwable cause) {
        super(cause);
    }

    public ZWebSocketHostInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
