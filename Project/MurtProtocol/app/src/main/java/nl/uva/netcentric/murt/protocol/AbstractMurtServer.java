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
 *
 * Implements the Murt Protocol but keeps details abstract.
 * Listeners are notified when a MurtClient connects and are able to send data.
 *
 */
public abstract class AbstractMurtServer implements Runnable {

    protected ServerSocket serverSocket;
    protected final int port;
    protected List<MurtConnectionListener> connListeners;
    protected List<MurtConnection> connections;

    private boolean running;

    public AbstractMurtServer(int port) {
        this.port = port;
        connListeners = new ArrayList<MurtConnectionListener>();
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

        while(running) {
            try {
                Socket s = serverSocket.accept();
                log("Accepted connection");

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String input = in.readLine();
                log("Read a line: " + input);

                int resX = Integer.parseInt(input.split(",")[0]);
                int resY = Integer.parseInt(input.split(",")[1]);

                MurtConnection conn = new MurtConnection(connections.size(), s,  resX, resY);
                connections.add(conn);

                for(MurtConnectionListener l : connListeners) {
                    l.onConnect(conn);

                    // sending data, todo maybe in seperate threads?
                    for(MurtConnection c : connections) {
                        byte[] data = null;
                        l.onSend(c, data);

                        if(data != null) {

                            // send length of array
                            PrintWriter out = new PrintWriter(c.connection.getOutputStream(), true);
                            out.println(data.length);
                            out.flush();
                            log("Sent bytearray length = " + data.length);

                            // Send byteArray
                            OutputStream output = c.connection.getOutputStream();
                            output.write(data);
                            output.flush();
                            log("Sent bytearray!");
                        }
                    }
                }

            } catch (IOException e) {
                log(e.getMessage());
            }

        }

    }

    public void addConnectionListener(MurtConnectionListener l) {
        connListeners.add(l);
    }

    public void stop() {
        if(serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
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
