package de.tum.mw.ftm.deefs.events;

import de.tum.mw.ftm.deefs.location.Position;

/**
 * Special Event class representing a customer demand
 *
 * @author Michael Wittmann
 */
public class DemandEvent extends Event {


	private final int track_id;  // original isarfunk track id
	private final double distance; // original track distance in m
	private final long duration;    // original track duration in ms
	private final Position start;    // original pick-up position
	private final Position target; // original drop-off position


	/**
	 * New instance of DemandEvent
	 *
	 * @param track_id      original israfunk track id
	 * @param scheduledTime original time the track was started in ms
	 * @param start         original pick-up position
	 * @param target        original drop-off position
	 * @param distance      original track distance in m
	 * @param duration      original track duration in ms
	 */
	public DemandEvent(int track_id, long scheduledTime, Position start,
					   Position target, double distance, long duration) {
		super(scheduledTime);
		this.track_id = track_id;
		this.distance = distance;
		this.start = start;
		this.target = target;
		this.duration = duration;
	}

	/**
	 * @return orignal isarfunk track id
	 */
	public int getTrack_id() {
		return track_id;
	}

	/**
	 * @return original track distance in m
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @return original pick-up position
	 */
	public Position getStart() {
		return start;
	}

	/**
	 * @return original track duration in ms
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @return original drop-off position
	 */
	public Position getTarget() {
		return target;
	}
}
