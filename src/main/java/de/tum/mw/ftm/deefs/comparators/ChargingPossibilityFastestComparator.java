package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.facilitiies.ChargingPossibility;

import java.util.Comparator;

/**
 * Orders charging possibilities in descending order comparing their best compatible connectors.
 *
 * @author Michael Wittmann
 */
public class ChargingPossibilityFastestComparator implements Comparator<ChargingPossibility> {

	private final ChargingInterface ci; // required charging interface

	/**
	 * New comparator instance
	 *
	 * @param ci required charging interface
	 */
	public ChargingPossibilityFastestComparator(ChargingInterface ci) {
		this.ci = ci;
	}

	@Override
	public int compare(ChargingPossibility o1, ChargingPossibility o2) {
		ConnectorFastestComparator comparator = new ConnectorFastestComparator();
		return comparator.compare(o1.bestConnector(ci), o1.bestConnector(ci));
	}


}
