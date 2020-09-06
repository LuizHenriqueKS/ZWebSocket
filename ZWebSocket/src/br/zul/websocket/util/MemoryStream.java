package br.zul.websocket.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author luizh
 */
public class MemoryStream {
    
    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    private final ByteArrayOutputStream stream;
    
    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    public MemoryStream() {
        this.stream = new ByteArrayOutputStream();
    }
    
    //==========================================================================
    //MÉTODOS PÚBLICOS
    //==========================================================================
    public void write1Byte(int i) {
        stream.write(i);
    }

    public void write2Bytes(int i) {
        write1Byte(i >>> 8);
        write1Byte(i);
    }

    public void write8Bytes(int i) {
        write1Byte(0);
        write1Byte(0);
        write1Byte(0);
        write1Byte(0);
        write1Byte(i >>> 24);
        write1Byte(i >>> 16);
        write1Byte(i >>> 8);
        write1Byte(i);
    }

    public void writeBytes(byte[] bytes) throws IOException {
        stream.write(bytes);
    }

    public byte[] toByteArray() {
        return stream.toByteArray();
    }
    
}
