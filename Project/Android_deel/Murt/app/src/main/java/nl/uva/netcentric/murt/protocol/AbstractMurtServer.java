package nl.uva.netcentric.murt.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String input = in.readLine();

				log("Read a line: " + input);

				int resX = Integer.parseInt(input.split(",")[0]);
				int resY = Integer.parseInt(input.split(",")[1]);

				final MurtConnection conn = new MurtConnection(connections.size(), s, resX, resY);
				connections.add(conn);

				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						while (!conn.isClosed() && !Thread.currentThread().isInterrupted()) {
							byte[] data = listener.onSend(conn);

							try {

								// send length of array
								PrintWriter out = new PrintWriter(conn.connection.getOutputStream(), true);
								out.println(data.length);
								out.flush();
								log("Sent bytearray length = " + data.length);

								// Send byteArray
								OutputStream output = conn.connection.getOutputStream();
								output.write(data);
								output.flush();
								log("Sent bytearray!");

								break;

							} catch (IOException e) {
								log(e.getMessage());
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

			} catch (IOException e) {
				log(e.getMessage());
			}

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
