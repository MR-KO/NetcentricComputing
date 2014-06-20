package nl.uva.netcentric.murt.protocol;

/**
 * Created by Sjoerd on 18-6-2014.
 */
public abstract class AbstractMurtClient {

	private final MurtConnectionListener listener;
	private final String config;

	public AbstractMurtClient(MurtConnectionListener listener, String config) {
		this.listener = listener;
		this.config = config;
		connect();
	}

	protected abstract MurtConnection connect();

	protected abstract void log(String message);

}
