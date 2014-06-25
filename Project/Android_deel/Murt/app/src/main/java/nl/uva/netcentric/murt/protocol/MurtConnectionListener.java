package nl.uva.netcentric.murt.protocol;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public interface MurtConnectionListener {

	void onConnect(MurtConnection conn, Integer config);

	void onDisconnect(MurtConnection conn);

	byte[] onSend(MurtConnection conn);

	void onReceive(byte[] data);

}
