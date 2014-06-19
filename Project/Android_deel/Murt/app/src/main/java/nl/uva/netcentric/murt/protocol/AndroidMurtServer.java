package nl.uva.netcentric.murt.protocol;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public class AndroidMurtServer extends AbstractMurtServer {

    private NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;
    private String serviceName;

    public AndroidMurtServer(NsdManager nsdManager, MurtConnectionListener listener, int port) {
        super(port, listener);
        this.nsdManager = nsdManager;
        initializeRegistrationListener();
        registerService(port);
    }

    @Override
    protected void log(String message) {
        Log.i(MurtConfiguration.TAG, message);
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(MurtConfiguration.SERVICE_NAME);
        serviceInfo.setServiceType(MurtConfiguration.SERVICE_TYPE);
        serviceInfo.setPort(port);

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
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
                log("Service registration failed!");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                log("Service unregistered!");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
                log("Service unregistration failed!");
            }
        };
    }


}
