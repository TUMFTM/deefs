package de.tum.mw.ftm.deefs.log;

/**
 * Log class to store facility statistics
 *
 * @author Michael Wittmann
 */
public class FacilityStats {

	private final int facilityID;        //unique facility id
	private final int carID;            //car's id which is connected to facility
	private final long time;            //time of action in ms
	private final String action;        //action
	private final int connected_cars;    //count of connected cars at facility
	private final int waiting_cars;    //count of waiting cars at facility


	/**
	 * Creates a new log entry for facility statistics
	 *
	 * @param facilityID     unique facility id
	 * @param carID          unique car id
	 * @param time           time of action in ms
	 * @param action         action identifier
	 * @param connected_cars number of already connected cars at facility
	 * @param waiting_cars   number of already waiting cars at facility
	 */
	public FacilityStats(int facilityID, int carID, long time, String action, int connected_cars, int waiting_cars) {
		super();
		this.facilityID = facilityID;
		this.carID = carID;
		this.time = time;
		this.action = action;
		this.connected_cars = connected_cars;
		this.waiting_cars = waiting_cars;
	}

	/**
	 * @return Number of already waiting cars at facility
	 */
	public int getWaitingCars() {
		return waiting_cars;
	}

	/**
	 * @return unique facility id
	 */
	public int getFacilityID() {
		return facilityID;
	}

	/**
	 * @return unique car id
	 */
	public int getCarID() {
		return carID;
	}

	/**
	 * @return time of action in ms
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return action identifier
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @return number of already connected cars at facility
	 */
	public int getConnected_cars() {
		return connected_cars;
	}


}
