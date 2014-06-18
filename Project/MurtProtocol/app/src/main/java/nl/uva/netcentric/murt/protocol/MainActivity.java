package nl.uva.netcentric.murt.protocol;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;


public class MainActivity extends Activity {

    // todo split in server and client service


    enum Mode {
        CLIENT, SERVER, NONE
    }

    private Mode mode = Mode.NONE;

    // Client and server config
    public static final String TAG = "murtprotocol";
    public static final String SERVICE_NAME = "MurtProtocol";
    public static final String SERVICE_TYPE = "_ipp._tcp.";
    private NsdManager nsdManager;

    // Client only
    private NsdManager.ResolveListener resolveListener;
    private NsdManager.DiscoveryListener discoveryListener;
    private AsyncTask clientTask;



    // Server only
    private NsdManager.RegistrationListener registrationListener;
    private ServerSocket serverSocket;
    private int localPort;
    private String serviceName;
    private NsdServiceInfo service;
    private AsyncTask serverTask;

    //todo keep track of connections
    private List<MurtConnection> connections;

    private TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow networking in UI thread, not needed anymore?
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);


        setContentView(R.layout.activity_main);

        t = (TextView) findViewById(R.string.murt);
    }


    // Init service registration and listening on some port
    public void initServer(View view) {
        cleanup();

        initializeRegistrationListener();

        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        localPort = serverSocket.getLocalPort();
        new ServerMurt().execute(serverSocket);
        registerService(localPort);

        Log.i(TAG, "Servermurt enabled");
    }

    // Starts discovering services and connects to a server when it find one
    public void initClient(View view) {
        cleanup();

        initializeResolveListener();
        initializeDiscoveryListener();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

        Log.i(TAG, "Clientmurt enabled");
    }


    private void cleanup() {
        if (mode == Mode.CLIENT) {
            cleanupClient();
        } else if (mode == Mode.SERVER) {
            cleanupServer();
        }
    }

    private void cleanupClient() {
        if(clientTask != null) {
            clientTask.cancel(true);
        }


        // todo more cleanup socket etc
    }

    private void cleanupServer() {
        if(serverTask != null) {
            serverTask.cancel(true);
        }

        // todo more cleanup, socket and stuff
    }


    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                serviceName = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                Log.e(TAG, "Service registration failed!");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.i(TAG, "Service unregistered!");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
                Log.e(TAG, "Service unregistration failed!");
            }
        };
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(serviceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + serviceName);
                } else if (service.getServiceName().contains(SERVICE_NAME)) {
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                //mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }


    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(serviceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }

                service = serviceInfo;
                int port = service.getPort();
                InetAddress host = service.getHost();

                // todo start async task

                clientTask = new ClientMurt().execute(host, port);

                Log.d(TAG, "IP = " + host.getHostAddress());

                Log.i(TAG, "Connecting to murt!");


            }
        };
    }


    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }


    @Override
    protected void onPause() {
        tearDown();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // todo if clien, if server
        //registerService(localPort);
        //nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    @Override
    protected void onDestroy() {
        tearDown();
        super.onDestroy();
    }

    // NsdHelper's tearDown method
    public void tearDown() {
        nsdManager.unregisterService(registrationListener);
        nsdManager.stopServiceDiscovery(discoveryListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class MurtConnection {

        private int identifier;

        private final int resX;
        private final int resY;

        public MurtConnection(int identifier, int resX, int resY) {
            this.identifier = identifier;
            this.resX = resX;
            this.resY = resY;
        }

    }


    private class ClientMurt extends AsyncTask<Object, Void, Void> {
        protected Void doInBackground(Object... params) {
            try {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(displayMetrics);
                int resX = displayMetrics.widthPixels;
                int resY = displayMetrics.heightPixels;

                Log.i(TAG, "resX = " + resX + ", resY = " + resY);

                // todo send config
                Socket s = new Socket(((InetAddress) params[0]).getHostAddress(), (Integer) params[1]);
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out.print(resX + "," + resY);
                out.flush();

                while(!isCancelled()) {
                    String line = in.readLine();
                    Log.i(TAG, "Server responded with: " + line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class ServerMurt extends AsyncTask<ServerSocket, Void, Void> {
        protected Void doInBackground(ServerSocket... params) {
            try {


                // todo read config...
                Log.i(TAG, "Listening on port " + localPort);

                while(!isCancelled()) {
                    Socket s = params[0].accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String input = in.readLine();
                    Log.i(TAG, "Read a line: " + input);

                    int resX = Integer.parseInt(input.split(",")[0]);
                    int resY = Integer.parseInt(input.split(",")[1]);

                    MurtConnection conn = new MurtConnection(connections.size(), resX, resY);
                    connections.add(conn);

                    t.setText(input);

                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.print("MURT");
                    out.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


}
