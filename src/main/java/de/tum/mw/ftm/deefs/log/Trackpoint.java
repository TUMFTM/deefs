package de.tum.mw.ftm.deefs.log;

import de.tum.mw.ftm.deefs.location.Position;

import java.util.concurrent.TimeUnit;


/**
 * Log class to store vehicle movements
 *
 * @author Michael Wittmann
 */

public class Trackpoint implements Comparable<Trackpoint> {
	private final int car_id;        //unique car id
	private final int track_id;    //vehicles track id
	private final long time;        //time the position was reached
	private final String status;    //internal vehicle status
	private final Position position; //vehicle position
	private final int facility_id; //id of connected facility (if connected)
	private final float distance;    //driven distance since last trackpoint in m
	private final float soc;        //actual vehicle soc in %
	private final int shift_count; //vehicles shift count


	/**
	 * Create a new trackpoint log entry
	 *
	 * @param car_id      unique car id
	 * @param shift_count car's shift count
	 * @param track_id    car's track id
	 * @param time        time the position was reached in ms
	 * @param status      actual vehicle status
	 * @param position    actual position
	 * @param distance    distance driven since last trackpoint
	 * @param soc         actual soc in %
	 * @param facility_id if connected to a facility the unique facility id
	 */
	public Trackpoint(int car_id, int shift_count, int track_id, long time, String status, Position position,
					  float distance, float soc, int facility_id) {
		super();
		this.car_id = car_id;
		this.track_id = track_id;
		this.time = time;
		this.status = status;
		this.position = position;
		this.distance = distance;
		this.soc = soc;
		this.facility_id = facility_id;
		this.shift_count = shift_count;
	}

	/**
	 * @return the vehicle's shift count
	 */
	public int getShiftCount() {
		return shift_count;
	}

	/**
	 * @return the vehicle's unique id
	 */
	public int getCar_id() {
		return car_id;
	}

	/**
	 * @return the vehcile's track count
	 */
	public int getTrack_id() {
		return track_id;
	}

	/**
	 * @return the time the actual position was reached in ms
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return the vehicle's status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the vehicle's position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * @return the distance driven since the last trackpoint in m
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * @return the vehicle's SOC in %
	 */
	public float getSoc() {
		return soc;
	}

	/**
	 * @return the unique id of the facility the vehicle is connected to (if connected)
	 */
	public int getFacilityId() {
		return facility_id;
	}


	/*
	 * Compares two trackpoints by time and if time is equal by track_id
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Trackpoint o) {
		if (this.time < o.time) return -1;
		else if (this.time > o.time) return 1;
		else {
			return Integer.compare(this.track_id, o.track_id);
		}
	}

	/**
	 * Parses trackpoint informations into all readable CSV-String
	 * <p>track_id,time,status,facility_id,lat,lon,distance,soc
	 *
	 * @return comma separated informations as String
	 */
	public String getCSVString() {
		return track_id + ","
				+ time + ","
				+ String.format("%02d-%02d:%02d:%02d",
				TimeUnit.MILLISECONDS.toDays(time),
				TimeUnit.MILLISECONDS.toHours(time) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(time)),
				TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
				TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))) + ","
				+ status + ","
				+ facility_id + ","
				+ position.getLat() + ","
				+ position.getLon() + ","
				+ distance + ","
				+ soc + "\n";
	}
}
