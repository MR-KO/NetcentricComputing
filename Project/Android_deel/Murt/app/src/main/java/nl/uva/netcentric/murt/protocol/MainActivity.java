package nl.uva.netcentric.murt.protocol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
/*
    @Override
    public void onConnect(nl.uva.netcentric.murt.protocol.MurtConnection conn) {
        // do something with new connection
    }

    @Override
    public void onSend(nl.uva.netcentric.murt.protocol.MurtConnection conn, byte[] data) {
        // fill data[] with image
    }

    enum Mode {
        CLIENT, SERVER, NONE
    }

    private Mode mode = Mode.NONE;

    private NsdManager nsdManager;

    // todo Client only
    //private NsdManager.ResolveListener resolveListener;
    //private NsdManager.DiscoveryListener discoveryListener;
    //private AsyncTask clientTask;
    //private Socket serverConnection;




    // Server only
    private NsdManager.RegistrationListener registrationListener;
    private String serviceName;
    private NsdServiceInfo service;
    private AsyncTask serverTask;

    public static ImageHandler handler;
    public static Bitmap image;

    //todo keep track of connections
    private List<MurtConnection> connections = new ArrayList<MurtConnection>();

    private TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        setContentView(R.layout.activity_main);

        t = (TextView) findViewById(R.string.murt);

        handler = new ImageHandler();
        Drawable drawable = getResources().getDrawable(R.drawable.prepare);
        handler.open(drawable);
        image = handler.getImage();
    }


    // Init service registration and listening on some port
    public void initServer(View view) {
        cleanup();

        initializeRegistrationListener();


        serverTask = new ServerMurt().execute(serverSocket);
        registerService(PORT);

        Log.i(TAG, "Servermurt enabled");
    }

    // Starts discovering services and connects to a server when it find one
    public void initClient(View view) {
        cleanup();

        initializeResolveListener();
        initializeDiscoveryListener();

        Log.i(TAG, ""+(nsdManager == null));
        Log.i(TAG, "" + (discoveryListener == null));

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

        Log.i(TAG, "Clientmurt enabled");
    }


    private void cleanup() {
        if (mode == Mode.CLIENT) {
            cleanupClient();
        } else if (mode == Mode.SERVER) {
            cleanupServer();
        }

        mode = Mode.NONE;
    }

    private void cleanupClient() {
        if(clientTask != null) {
            clientTask.cancel(true);
        }

        try {
            serverConnection.close();
            serverConnection = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    private void cleanupServer() {
        if(serverTask != null) {
            serverTask.cancel(true);
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        nsdManager.unregisterService(registrationListener);
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
                    if(mode == Mode.NONE) {
                        nsdManager.resolveService(service, resolveListener);
                    }
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
                InetAddress host = service.getHost();

                // todo start async task

                clientTask = new ClientMurt().execute(host);

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

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }


    @Override
    protected void onPause() {
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
        super.onDestroy();
        cleanup();
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

                serverConnection = new Socket(((InetAddress) params[0]).getHostAddress(), PORT);
                Log.i(TAG, "Created socket... sending shit...");
                PrintWriter out = new PrintWriter(serverConnection.getOutputStream(), true);

                BufferedReader in = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
                out.println(resX + "," + resY);
                out.flush();
                Log.i(TAG, "Sent shit!");

                while(!isCancelled()) {
                    InputStream is = serverConnection.getInputStream();
                    int dataSize = Integer.parseInt(in.readLine());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] content = new byte[ 2048 ];
                    int bytesRead = -1;
                    int count = 0;
                    while( ( bytesRead = is.read( content ) ) != -1 ) {
                        baos.write( content, 0, bytesRead );
                        count += bytesRead;
                        Log.i(TAG, "Read " + bytesRead);
                    } // while

                    Log.i(TAG, "Data size = " + dataSize + ", read " + count);
                    Bitmap breceived = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, dataSize);
                }

            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
                Log.i(TAG, "IOEXCEPTION");
            }

            Log.i(TAG, "Cancelled!");

            return null;
        }
    }

    private class ServerMurt extends AsyncTask<Integer, Void, Void> {
        protected Void doInBackground(Integer... params) {

            new AndroidMurtServer(nsdManager, params[0]).run();

            return null;
        }
    }
*/

}
