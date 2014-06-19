package nl.uva.netcentric.murt.protocol;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public class MurtConnection {

    public final int identifier;
    public final Socket connection;

    public final int resX;
    public final int resY;

    private boolean closed;

    public MurtConnection(int identifier, Socket connection, int resX, int resY) {
        this.identifier = identifier;
        this.connection = connection;
        this.resX = resX;
        this.resY = resY;
    }

    public void close() throws IOException {
        if(connection != null) {
            connection.close();
            closed = true;
        }
    }

    public boolean isClosed() {
        return closed;
    }

}
