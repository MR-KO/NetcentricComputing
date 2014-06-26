package nl.uva.netcentric.murt.protocol;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.murt.app.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public class AndroidMurtClient implements Runnable {

	private final MurtConnectionListener listener;
	private MurtConnection connection;
	private InetAddress host;
	private Thread thread;
	private Integer config;

	private NsdManager nsdManager;
	private NsdManager.ResolveListener resolveListener;
	private NsdManager.DiscoveryListener discoveryListener;
	private Socket serverConnection;

	private boolean connected;
	private int attempts = 3;

	public AndroidMurtClient(NsdManager nsdManager, MurtConnectionListener listener, Integer config) {
		Log.i(MurtConfiguration.TAG, "New AndroidMurtClient");
		this.nsdManager = nsdManager;
		this.listener = listener;
		this.config = config;

		if (!MurtConfiguration.DEBUG || !MurtConfiguration.USE_NSD) {
			log("Dynamic client mode");
			initializeResolveListener();
			initializeDiscoveryListener();

			nsdManager.discoverServices(MurtConfiguration.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
		} else {
			log("Static client mode using IP=" + MurtConfiguration.DEBUG_HOST + ":" + MurtConfiguration.DEBUG_PORT);

			try {
				host = InetAddress.getByName(MurtConfiguration.DEBUG_HOST);
			} catch (UnknownHostException e) {
				log(e.getMessage());
			}

			thread = new Thread(AndroidMurtClient.this);
			thread.start();
		}
	}

	public synchronized void stop() {
		nsdManager.stopServiceDiscovery(discoveryListener);

		try {
			connection.close();
			thread.interrupt();
		} catch (Exception e) {
			log("Error in stopping the client: " + e.getMessage());
		}
	}

	public void initializeDiscoveryListener() {

		// Instantiate a new DiscoveryListener
		discoveryListener = new NsdManager.DiscoveryListener() {

			//  Called as soon as service discovery begins.
			@Override
			public void onDiscoveryStarted(String regType) {
				Log.d(MurtConfiguration.TAG, "Service discovery started");
			}

			@Override
			public void onServiceFound(NsdServiceInfo service) {
				// A service was found!  Do something with it.
				Log.d(MurtConfiguration.TAG, "Service discovery success" + service);
				if (!service.getServiceType().equals(MurtConfiguration.SERVICE_TYPE)) {
					// Service type is the string containing the protocol and
					// transport layer for this service.
					Log.d(MurtConfiguration.TAG, "Unknown Service Type: " + service.getServiceType());
				} else if (service.getServiceName().contains(MurtConfiguration.SERVICE_NAME)) {
					if (connection == null) {
						nsdManager.resolveService(service, resolveListener);
					}
				}
			}

			@Override
			public void onServiceLost(NsdServiceInfo service) {
				// When the network service is no longer available.
				// Internal bookkeeping code goes here.
				Log.e(MurtConfiguration.TAG, "service lost" + service);
			}

			@Override
			public void onDiscoveryStopped(String serviceType) {
				Log.i(MurtConfiguration.TAG, "Discovery stopped: " + serviceType);
			}

			@Override
			public void onStartDiscoveryFailed(String serviceType, int errorCode) {
				Log.e(MurtConfiguration.TAG, "Discovery failed: Error code:" + errorCode);
				//mNsdManager.stopServiceDiscovery(this);
			}

			@Override
			public void onStopDiscoveryFailed(String serviceType, int errorCode) {
				Log.e(MurtConfiguration.TAG, "Discovery failed: Error code:" + errorCode);
				nsdManager.stopServiceDiscovery(this);
			}
		};
	}


	public void initializeResolveListener() {
		resolveListener = new NsdManager.ResolveListener() {

			@Override
			public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
				// Called when the resolve fails.  Use the error code to debug.
				Log.e(MurtConfiguration.TAG, "Resolve failed" + errorCode);
			}

			@Override
			public void onServiceResolved(NsdServiceInfo serviceInfo) {
				Log.i(MurtConfiguration.TAG, "Resolve Succeeded. " + serviceInfo);

				host = serviceInfo.getHost();

				Log.i(MurtConfiguration.TAG, "IP = " + host.getHostAddress());
				Log.i(MurtConfiguration.TAG, "Connecting to murt!");

				thread = new Thread(AndroidMurtClient.this);
				thread.start();
			}
		};
	}


	public void log(String message) {
		Log.i(MurtConfiguration.TAG, "" + message);
	}

	@Override
	public void run() {

		try {

			serverConnection = new Socket(host, MurtConfiguration.DEBUG_PORT);
			connected = true;
			MainActivity.toast("Connected to server", Toast.LENGTH_SHORT);

			connection = new MurtConnection(0, serverConnection);
			connection.setThread(thread);

			listener.onConnect(connection, 0);

			OutputStream os = serverConnection.getOutputStream();
			new ObjectOutputStream(os).writeObject(config);
			log("Sent device config!");

			while (!serverConnection.isClosed() && !Thread.currentThread().isInterrupted()) {

				Log.i(MurtConfiguration.TAG, "In if");

				try {
					InputStream is = serverConnection.getInputStream();
					BitmapDataObject bitmap = (BitmapDataObject)new ObjectInputStream(is).readObject();

					if(bitmap.bitmapBytes != null) {
						log("Calling onReceive...");
						listener.onReceive(bitmap.bitmapBytes);
					}

				} catch (Exception e) {
					Log.e(MurtConfiguration.TAG, "Error in client thread: " + e.getMessage());
					listener.onDisconnect(connection);
					stop();
				}

				Thread.sleep(30);

			}

		} catch (IOException e) {
			log(e.getMessage());
			MainActivity.toast("Failed to connect! (Attempt " + (3-attempts) + " of 3)", Toast.LENGTH_SHORT);
			while(!connected && --attempts >= 0) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					log(e1.getMessage());
				}
				run();
			}
			return;
		} catch (InterruptedException e) {
			log(e.getMessage());
		}

		listener.onDisconnect(connection);

		log("Stopped client...");
	}
}
