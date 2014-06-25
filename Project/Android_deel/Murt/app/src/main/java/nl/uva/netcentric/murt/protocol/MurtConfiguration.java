package nl.uva.netcentric.murt.protocol;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public class MurtConfiguration {

	public static final boolean DEBUG = false;
	public static final int DEBUG_PORT = 11111;
	public static final String DEBUG_HOST = "192.168.0.102";

	public static final String TAG = "murtprotocol";
	public static final String SERVICE_NAME = "MurtProtocol";
	public static final String SERVICE_TYPE = "_ipp._tcp.";

    public static final boolean USE_NSD = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;

}
