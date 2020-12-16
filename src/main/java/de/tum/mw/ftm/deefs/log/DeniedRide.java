package de.tum.mw.ftm.deefs.log;

/**
 * Log class to store denied rides and their reason of refusal
 *
 * @author Michael Wittmann
 */
public class DeniedRide {
	// Possible reasons a ride can be denied
	public static final String REASON_SOC_TOO_LOW = "SOC_TOO_LOW";        // The remaining SOC is not high enough to fulfill the ride (There must always be a reachable charging station after the ride=
	public static final String REASON_CHARGING = "CHARGING";            // The selected car is charging right now and has not reached the SOC which allows the agent to accept the ride
	public static final String REASON_NO_ROUTE_FOUND = "NO_ROUTE_FOUND";    // No route to customer or on the customer ride it self could be calculated
	public static final String REASON_BUSY = "BUSY";                        // The car is in a busy state (waiting, on way back home etc.)
	public static final String REASON_NO_FREE_CAR = "NO_FREE_CAR_FOUND";    // Final Reason a ride was denied.
	public static final String REASON_NO_REACHABLE_CHARGING_STATION_FOUND = "NO_REACHABLE_CHARGING_STATION_FOUND"; // The agent would be able to fulfill the customer request but the remaining SOC is not high enough to reach a charging station after the ride
	private final int car_id;        // id of the car which denied the ride
	private final int track_id;    //	id of the denied track
	private final long time;        // scheduled time of the track
	private final String reason;    // reason the track was denied by the agent
	private final double distance_to_customer;    // if already calculated the distance from agent to customer
	private final double track_distance;            // original track distance


	/**
	 * Create a new log entry for a denied ride
	 *
	 * @param car_id               ID of the car that denied the ride
	 * @param track_id             ID of the track which was denied
	 * @param time                 scheduled time of the track
	 * @param track_distance       original track distance in m
	 * @param distance_to_customer distance from agent to customer in m if calculated
	 * @param reason               reason the ride was denied (use static class variables)
	 */
	public DeniedRide(int car_id, int track_id, long time, double track_distance, double distance_to_customer, String reason) {
		super();
		this.car_id = car_id;
		this.track_id = track_id;
		this.time = time;
		this.reason = reason;
		this.track_distance = track_distance;
		this.distance_to_customer = distance_to_customer;
	}

	/**
	 * Returns the id of the car which denied the ride
	 *
	 * @return car_id
	 */
	public int getCar_id() {
		return car_id;
	}

	/**
	 * Returns the track id of the denied track
	 *
	 * @return track id
	 */
	public int getTrack_id() {
		return track_id;
	}

	/**
	 * Returns the time the ride was scheduled
	 *
	 * @return scheduled time in ms
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Returns the reason as text the ride was denied
	 *
	 * @return reason of deny
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Returns the distance from agent to customer of calculated
	 *
	 * @return distance to customer in m
	 */
	public double getDistance_to_customer() {
		return distance_to_customer;
	}

	/**
	 * Returns the original track distance of the denied track
	 *
	 * @return original track distance in m
	 */
	public double getTrack_distance() {
		return track_distance;
	}
}
