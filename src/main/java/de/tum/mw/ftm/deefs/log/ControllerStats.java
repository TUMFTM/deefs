package de.tum.mw.ftm.deefs.log;

/**
 * Log class to watch the function of the taxi controller
 *
 * @author Michael Wittmann
 */
public class ControllerStats {

	public static final String TYPE_TARGET = "TARGET";
	public static final String TYPE_VALUE = "VALUE";

	private final long time;  // time a new value was set
	private final int n;
	private String action;    // value identifier

	public ControllerStats(long time, String type, int n) {
		super();
		this.time = time;
		switch (type) {
			case TYPE_TARGET:
				this.action = type;
				break;
			case TYPE_VALUE:
				this.action = type;
				break;
			default:
				System.err.println("Innvalid action used in ControllerStats");
		}
		this.n = n;
	}

	/**
	 * Returns the number of vehicles recorded in this stat. Depending on the related action tag the number of active vehicles or the number
	 * of desired vehicles is returned
	 *
	 * @return number of vehicles
	 */
	public int getN() {
		return n;
	}

	/**
	 * Returns the time this stat belongs to
	 *
	 * @return time in ms
	 */
	public long getTime() {
		return time;
	}


	/**
	 * Returns action Tag which defines weather the value represents the number of active or desired vehicles
	 *
	 * @return action tag (TARGET or VALUE)
	 */
	public String getAction() {
		return action;
	}


}
