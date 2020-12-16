package de.tum.mw.ftm.deefs.events;

import de.tum.mw.ftm.deefs.elements.taxi.BEVTaxi;

/**
 * Special Event which is triggered if a car reached its defined maximum soc at a charging station
 *
 * @author Michael Wittmann
 */
public class FullChargedEvent extends Event {

	private final BEVTaxi car; // Instance of charging car

	/**
	 * New instance of FullChargedEvent
	 *
	 * @param scheduledTime time the car reached its maximum SOC in ms
	 * @param car           related car
	 */
	public FullChargedEvent(long scheduledTime, BEVTaxi car) {
		super(scheduledTime);
		this.car = car;
	}


	/**
	 * Forces the related car from disconnect from the charging station
	 *
	 * @see BEVTaxi
	 */
	public void disconnect() {
		car.nextAction(getScheduledTime());
	}

}
