package de.tum.mw.ftm.deefs.events;

import de.tum.mw.ftm.deefs.elements.TaxiController;


/**
 * Special Event, used to update the desired number of active cars in the TaxiController.
 *
 * @author Michael Wittmann
 * @see Event
 * @see TaxiController
 */
public class TaxiControlNewTargetCountEvent extends Event {

	private final int n_cars; //new target value to be set

	/**
	 * New instacne of TaxiControlNewTargetCountEvent
	 *
	 * @param scheduledTime time a new targetcount should be set in ms
	 * @param n_cars        traget count of active taxis
	 */
	public TaxiControlNewTargetCountEvent(long scheduledTime, int n_cars) {
		super(scheduledTime);
		this.n_cars = n_cars;
	}

	/**
	 * @return target count of active taxis
	 */
	public int getNCars() {
		return this.n_cars;
	}

}
