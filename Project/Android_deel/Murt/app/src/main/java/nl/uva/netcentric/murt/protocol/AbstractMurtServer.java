package nl.uva.netcentric.murt.protocol;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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

				final MurtConnection conn = new MurtConnection(connections.size(), s, 0, 0);
				connections.add(conn);

				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						while (!conn.isClosed() && !Thread.currentThread().isInterrupted()) {

							try {

//								BitmapDataObject data = new BitmapDataObject(listener.onSend(conn));
//								OutputStream temp = conn.connection.getOutputStream();
//								new ObjectOutputStream(temp).writeObject(data);
								byte[] data = listener.onSend(conn);
								BitmapDataObject bitmap = new BitmapDataObject(data);
								log("data == null? " + (data == null));
								new ObjectOutputStream(conn.connection.getOutputStream()).writeObject(bitmap);

								log("Sent bytearray!");
								conn.close();
								listener.onDisconnect(conn);
								break;

							} catch (Exception e) {
								log("Error in sending bytearray: " + e.getMessage());
								listener.onDisconnect(conn);

								try {
									conn.close();
								} catch (IOException e1) {
									log(e1.getMessage());
								}
							}

							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								listener.onDisconnect(conn);
								Thread.currentThread().interrupt();
							}
						}
					}
				});

				conn.setThread(t);
				t.start();

				listener.onConnect(conn);

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
