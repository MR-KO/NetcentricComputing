package nl.uva.netcentric.murt.protocol;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public interface MurtConnectionListener {

    void onConnect(MurtConnection conn);

    void onSend(MurtConnection conn, byte[] data);

    void onReceive(byte[] data);

}
