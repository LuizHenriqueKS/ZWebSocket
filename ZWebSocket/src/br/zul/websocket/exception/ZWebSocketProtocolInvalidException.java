package br.zul.websocket.exception;

/**
 *
 * @author luizh
 */
public class ZWebSocketProtocolInvalidException extends ZWebSocketException {

    public ZWebSocketProtocolInvalidException() {
    }

    public ZWebSocketProtocolInvalidException(String message) {
        super(message);
    }

    public ZWebSocketProtocolInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZWebSocketProtocolInvalidException(Throwable cause) {
        super(cause);
    }

    public ZWebSocketProtocolInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
