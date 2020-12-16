package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.ChargingPossibility;

import java.util.Comparator;

/**
 * Orders ChargingPossibilities ascending by their queue size.
 *
 * @author Michael Wittmann
 */
public class ChargingPossibilityQueueSizeComparator implements Comparator<ChargingPossibility> {

	@Override
	public int compare(ChargingPossibility o1, ChargingPossibility o2) {
		return Integer.compare(o1.getQueueSize(), o2.getQueueSize());
	}
}
