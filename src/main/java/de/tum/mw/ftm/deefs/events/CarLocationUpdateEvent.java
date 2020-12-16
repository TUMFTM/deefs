package de.tum.mw.ftm.deefs.events;

import de.tum.mw.ftm.deefs.elements.taxi.Taxi;

/**
 * Special type of event used to update the position of a car
 *
 * @author Michael Wittmann
 * @see Event
 */
public class CarLocationUpdateEvent extends Event {

	private final Taxi taxi; // instacne of car that should be updated

	/**
	 * New instance CarLocationUpdateEvent
	 *
	 * @param scheduledTime time the next position update should be fulfilled
	 * @param taxi          instance of taxi which should be updated
	 */
	public CarLocationUpdateEvent(long scheduledTime, Taxi taxi) {
		super(scheduledTime);
		this.taxi = taxi;
	}

	/**
	 * Calls the internal update function of a car.
	 * By calling this function the next position stored in the route buffer will be set as actual position, and if necessary a new CarLocationUptadeEvent will be created
	 *
	 * @see Taxi
	 */
	public void updateCar() {
		taxi.updatePosition();
	}


}
