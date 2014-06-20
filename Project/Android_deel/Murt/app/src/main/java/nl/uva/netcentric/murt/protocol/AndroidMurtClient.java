package nl.uva.netcentric.murt.protocol;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public class AndroidMurtClient implements Runnable {

	private final MurtConnectionListener listener;
	private final String config;
	private MurtConnection connection;
	private InetAddress host;

	private NsdManager nsdManager;
	private NsdManager.ResolveListener resolveListener;
	private NsdManager.DiscoveryListener discoveryListener;
	private Socket serverConnection;

	public AndroidMurtClient(NsdManager nsdManager, MurtConnectionListener listener, String config) {
		Log.i(MurtConfiguration.TAG, "New AndroidMurtClient");
		this.nsdManager = nsdManager;
		this.listener = listener;
		this.config = config;

		initializeResolveListener();
		initializeDiscoveryListener();

		nsdManager.discoverServices(MurtConfiguration.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

		// TODO: Put this back at the bottom of onServiceResolved
		Thread t = new Thread(AndroidMurtClient.this);
		t.start();
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


			}
		};
	}


	public void log(String message) {
		Log.i(MurtConfiguration.TAG, message);
	}

	@Override
	public void run() {

		try {
//			serverConnection = new Socket(host, MurtConfiguration.DEBUG_PORT);
			serverConnection = new Socket("82.156.32.8", MurtConfiguration.DEBUG_PORT);

			connection = new MurtConnection(0, serverConnection, 0, 0);

			listener.onConnect(connection);

			Log.i(MurtConfiguration.TAG, "Created socket... sending shit...");
			PrintWriter out = new PrintWriter(serverConnection.getOutputStream(), true);

			BufferedReader in = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
			out.println(config);
			out.flush();
			Log.i(MurtConfiguration.TAG, "Sent shit!");

			int attemps = 10;

			while (attemps > 0 && !serverConnection.isClosed() && !Thread.currentThread().isInterrupted()) {
				Log.i(MurtConfiguration.TAG, "In if");
				InputStream is = serverConnection.getInputStream();

				String dataLength = in.readLine();

				if (dataLength == null) {
					attemps--;
					continue;
				}

				Log.i(MurtConfiguration.TAG, "datalength != null, it is " + dataLength);

				int dataSize = Integer.parseInt(dataLength);
				Log.i(MurtConfiguration.TAG, "Data size = " + dataSize);
				byte[] data = new byte[dataSize];
				is.read(data, 0, dataSize);

				/*ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] content = new byte[2048];
				int bytesRead = -1;
				int count = 0;
				while ((bytesRead = is.read(content)) > 0) {
					Log.i(MurtConfiguration.TAG, "Read " + bytesRead + " bytes" + ", total=" + count);
					baos.write(content, 0, bytesRead);
					count += bytesRead;
				}*/

				Log.i(MurtConfiguration.TAG, "Calling onReceive...");
				//listener.onReceive(baos.toByteArray());
				listener.onReceive(data);
				break;
			}

			Log.i(MurtConfiguration.TAG, "Out if!");
		} catch (IOException e) {
			Log.i(MurtConfiguration.TAG, e.getMessage());
		}
	}
}
