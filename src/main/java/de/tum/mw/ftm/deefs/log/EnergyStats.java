package de.tum.mw.ftm.deefs.log;

/**
 * Log class to store energy statistics
 *
 * @author Michael Wittmann
 */
public class EnergyStats {
	private final int facility_id; // unique facility id
	private final int car_id;        // id of the car connected to the facility
	private final long time;        // actual time
	private final float power;    // actual charging power in W
	private final float energy;    // charged energy in J
	private final String connector;    // used Connector
	private final float p_max;    // max. supported power for charging process


	/**
	 * Create a new log entry for EnergyStats
	 *
	 * @param faility_id id of the regarding facility
	 * @param car_id     id of the car charging at facility
	 * @param time       time in ms
	 * @param power      used power in W
	 * @param energy     charged energy in J
	 * @param connector  used connector
	 * @param p_max      max. supported power for charging process
	 */
	public EnergyStats(int faility_id, int car_id, long time, float power, float energy, String connector, float p_max) {
		super();
		this.facility_id = faility_id;
		this.car_id = car_id;
		this.time = time;
		this.power = power;
		this.energy = energy;
		this.connector = connector;
		this.p_max = p_max;
	}


	/**
	 * Returns the unique id if the car charging at the facility
	 *
	 * @return id
	 */
	public int getCarID() {
		return car_id;
	}

	/**
	 * Returns the unique id of the regarding facility
	 *
	 * @return id
	 */
	public int getFacilityID() {
		return facility_id;
	}

	/**
	 * Returns time
	 *
	 * @return time in ms
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Returns the actual power the car is charging with
	 *
	 * @return power in W
	 */
	public float getPower() {
		return power;
	}

	/**
	 * Returns the amount of energy which was charged in this time step
	 *
	 * @return charged energy in J
	 */
	public float getEnergy() {
		return energy;
	}


	/**
	 * Returns the connector used for the charging process
	 *
	 * @return used connector
	 */
	public String getConnector() {
		return connector;
	}

	/**
	 * Returns the max. supported power for the charging process
	 *
	 * @return max. power in W
	 */
	public float getPMax() {
		return p_max;
	}


}
