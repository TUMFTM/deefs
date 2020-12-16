package de.tum.mw.ftm.deefs.events;

import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingPoint;
import de.tum.mw.ftm.deefs.elements.taxi.BEVTaxi;

/**
 * Special Event, used to update the state of charge while charing a vehicle
 *
 * @author Michael Wittmann
 * @see Event
 * @see BEVTaxi
 * @see ChargingPoint
 */
public class UpdateChargeEvent extends Event {

	private final ChargingPoint cp; //charging point the vehicle is connected to
	private final long postedTime;  //time the last update was done in ms


	/**
	 * New instance of UpdateChargeEvent
	 *
	 * @param scheduledTime time the update should be triggered in ms
	 * @param postedTime    time the last update was done in ms
	 * @param cp            related charging point
	 */
	public UpdateChargeEvent(long scheduledTime, long postedTime, ChargingPoint cp) {
		super(scheduledTime);
		this.postedTime = postedTime;
		this.cp = cp;
	}

	/**
	 * @return time the last update was done
	 */
	public long getPostedTime() {
		return this.postedTime;
	}

	/**
	 * Calls the update method at the related charing point
	 *
	 * @see ChargingPoint
	 */
	public void updateSOC() {
		cp.updateCharge(getScheduledTime());
	}

}
