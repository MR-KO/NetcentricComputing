package nl.uva.netcentric.murt.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sjoerd on 18-6-2014.
 * <p/>
 * Implements the Murt Protocol but keeps details abstract.
 * Listeners are notified when a MurtClient connects and are able to send data.
 */
public abstract class AbstractMurtServer implements Runnable {

	protected final int port;
	protected ServerSocket serverSocket;
	protected List<MurtConnection> connections;
	protected MurtConnectionListener listener;

	private boolean running;

	public AbstractMurtServer(int port, MurtConnectionListener listener) {
		this.port = port;
		this.listener = listener;
		connections = new ArrayList<MurtConnection>();
		new Thread(this).start();
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			running = true;

		} catch (IOException e) {
			log(e.getMessage());
			running = false;
			return;
		}

		log("MurtServer running on port " + port);
		log("Accepting...");

		while (running) {
			Socket s = null;

			try {
				s = serverSocket.accept();
			} catch (IOException e) {
				log(e.getMessage());
			}

			log("Accepted connection");

				final MurtConnection conn = new MurtConnection(connections.size(), s);
				connections.add(conn);

				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						while (!conn.isClosed() && !Thread.currentThread().isInterrupted()) {
							try {

								byte[] data = listener.onSend(conn);
//                                log("data == null? " + (data == null));

                                // todo this fixes disconnect? we have to keep sending in order to detect remote close
                                //if(data != null) {
                                    BitmapDataObject bitmap = new BitmapDataObject(data);
                                    new ObjectOutputStream(conn.connection.getOutputStream()).writeObject(bitmap);
                                    log("Sent bitmap!");
                                //}

							} catch (Exception e) {
								log("Error in sending bitmap: " + e.getMessage());
                                /* todo? we could remove the MurtConnection object from connections
                                 but this would require the use of a thread-safe datastructure
                                 and keep track of the last used identifier */
								listener.onDisconnect(conn);
                                return;
							}

							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
                                log("Thread interrupted, disconnecting...");
								listener.onDisconnect(conn);
								Thread.currentThread().interrupt();
                                return;
							}
						}

                        listener.onDisconnect(conn);
					}
				});

                try {
                    InputStream is = conn.connection.getInputStream();
                    Integer config = (Integer)new ObjectInputStream(is).readObject();
                    listener.onConnect(conn, config);
                } catch (IOException e) {
                    log(e.getMessage());
                } catch (ClassNotFoundException e) {
                    log(e.getMessage());
                }

				conn.setThread(t);
				t.start();

		}

	}

	public synchronized void stop() {
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				log(e.getMessage());
			}
		}

		for(MurtConnection conn : connections) {
			try {
				if(!conn.isClosed()) {
					conn.close();
				}
			} catch (IOException e) {
				log(e.getMessage());
			}
		}

		running = false;
		serverSocket = null;
	}

	public boolean isRunning() {
		return running;
	}

	protected abstract void log(String message);

}
