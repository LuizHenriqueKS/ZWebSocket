package br.zul.websocket.reader;

import br.zul.websocket.client.ZWebSocket;
import br.zul.websocket.model.ZWebSocketMessage;
import br.zul.websocket.model.ZWebSocketMessageBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author luizh
 */
public class ZWebSocketServerMessageReader {
    
    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    private final ZWebSocket socket;
    private final InputStream inputStream;
    
    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    public ZWebSocketServerMessageReader(ZWebSocket socket, InputStream inputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
    }
    
    //==========================================================================
    //MÉTODOS PÚBLICOS
    //==========================================================================
    public ZWebSocketMessage read() throws IOException{
        ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
        int length  = readLength(memoryStream);
        byte[] dataBytes = readBytes(length);
        memoryStream.write(dataBytes);
        return new ZWebSocketMessageBuilder()
                            .setEncodedData(memoryStream.toByteArray())
                            .setDecodedData(dataBytes)
                            .setSocket(socket)
                            .build();
    }

    private byte[] readBytes(int expectedLength) throws IOException {
        byte[] buffer = new byte[expectedLength];
        int len;
        while ((len = inputStream.read(buffer))!=-1) {
            if (len==expectedLength) return buffer;
            if (len!=expectedLength) throw new IOException("length inexpected");
        }
        throw new IOException("end inputstream");
    }

    private int readLength(ByteArrayOutputStream memoryStream) throws IOException {
        byte[] start = readBytes(2);
        memoryStream.write(start);
        switch (start[1]) {
            case 127:
            {
                byte[] lengthBytes = readBytes(8);
                memoryStream.write(lengthBytes);
                return (lengthBytes[0]<< 24)&0xff000000|
                        (lengthBytes[0]<< 16)&0x00ff0000|
                        (lengthBytes[0]<< 8)&0x0000ff00|
                        (lengthBytes[1])&0x000000ff;
            }
            case 126:
            {
                byte[] lengthBytes = readBytes(2);
                memoryStream.write(lengthBytes);
                return (lengthBytes[0]<< 8)&0x0000ff00|
                        (lengthBytes[1])&0x000000ff;
            }
            default:
                return start[1];
        }
    }
    
}
