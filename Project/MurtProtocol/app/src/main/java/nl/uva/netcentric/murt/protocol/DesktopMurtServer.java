package nl.uva.netcentric.murt.protocol;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public class DesktopMurtServer extends AbstractMurtServer implements MurtConnectionListener {


    public DesktopMurtServer(int port) {
        super(port);
        addConnectionListener(this);
    }

    @Override
    protected void log(String message) {
        System.out.println(message);
    }

    @Override
    public void onConnect(MurtConnection conn) {
        log("MurtConnectionListener onConnect");
    }

    @Override
    public void onSend(MurtConnection conn, byte[] data) {
        log("MurtConnectionListener onSend");
        data = new byte[] {1, 3, 3, 7};
    }

    public static void main(String[] args) {
        new DesktopMurtServer(MurtConfiguration.DEBUG_PORT).run();
    }
}
