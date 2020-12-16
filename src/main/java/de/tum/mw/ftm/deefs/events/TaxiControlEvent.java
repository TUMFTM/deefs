package de.tum.mw.ftm.deefs.events;

import de.tum.mw.ftm.deefs.elements.TaxiController;
import de.tum.mw.ftm.deefs.elements.taxi.BEVTaxi;

/**
 * Special Event, used to trigger the TaxiController to check the actual count of active vehicles.
 * This event should be triggered when a car logs off autonomous beacause shift duration is overwritten.
 *
 * @author Michael Wittmann
 * @see Event
 * @see TaxiController
 * @see BEVTaxi
 */
public class TaxiControlEvent extends Event {

	public TaxiControlEvent(long scheduledTime) {
		super(scheduledTime);
	}
}
