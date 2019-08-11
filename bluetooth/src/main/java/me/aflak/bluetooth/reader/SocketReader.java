package me.aflak.bluetooth.reader;

import java.io.IOException;
import java.io.InputStream;

public abstract class SocketReader {
    protected InputStream inputStream;

    public SocketReader(InputStream inputStream){
        this.inputStream = inputStream;
    }

    /**
     * Will be called continuously to read from the socket.
     * @return byte array of data, or null if no message.
     */
    public byte[] read() throws IOException {
        return null;
    }
}
