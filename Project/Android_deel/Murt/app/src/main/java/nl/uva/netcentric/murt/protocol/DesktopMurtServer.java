package nl.uva.netcentric.murt.protocol;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public class DesktopMurtServer extends AbstractMurtServer {


	public DesktopMurtServer(int port) {
		super(port, new MurtConnectionListener() {

			@Override
			public void onReceive(byte[] data) {

			}

			@Override
			public void onConnect(MurtConnection conn) {
				System.out.println("MurtConnectionListener onConnect");
			}

			@Override
			public void onDisconnect(MurtConnection conn) {
				System.out.println("MurtConnectionListener onDisconnect");
			}

			@Override
			public byte[] onSend(MurtConnection conn) {
				System.out.println("MurtConnectionListener onSend");
				return null;
			}
		});
	}

	public static void main(String[] args) {
		new DesktopMurtServer(MurtConfiguration.DEBUG_PORT);
	}

	@Override
	protected void log(String message) {
		System.out.println(message);
	}
}
