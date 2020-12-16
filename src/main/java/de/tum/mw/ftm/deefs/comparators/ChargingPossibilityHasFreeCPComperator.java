package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.facilitiies.ChargingPossibility;

import java.util.Comparator;


/**
 * Orders Charginpossibility based on the fact that they have free compatible charging points or not in ascending order (free, free, occupied/incompatible...).
 *
 * @author Michael Wittmann
 */
public class ChargingPossibilityHasFreeCPComperator implements Comparator<ChargingPossibility> {

	private final ChargingInterface ci; // required charging interface

	/**
	 * New comparator instance
	 *
	 * @param ci required charging interface
	 */
	public ChargingPossibilityHasFreeCPComperator(ChargingInterface ci) {
		this.ci = ci;
	}

	@Override
	public int compare(ChargingPossibility o1, ChargingPossibility o2) {
		return Boolean.compare(o1.hasFreeChargingPoints(ci), o2.hasFreeChargingPoints(ci));

	}
}
