package nl.uva.netcentric.murt.protocol;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public class MurtConnection {

	public final int identifier;
	public final Socket connection;

	private boolean closed;
	private Thread thread;

	public MurtConnection(int identifier, Socket connection) {
		this.identifier = identifier;
		this.connection = connection;
	}

	public void close() throws IOException {
		if (connection != null) {
			connection.close();
			thread.interrupt();
			closed = true;
		}
	}

	public boolean isClosed() {
		return connection.isClosed() || closed;
	}

	public Thread getThread() {
		return this.thread;
	}

	public void setThread(Thread t) {
		this.thread = t;
	}

}
