package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.taxi.Taxi;

import java.util.Comparator;

/**
 * Comparator to compare taxis according to their last log off.
 * Orders Taxis in descending order based on their time logged off.
 *
 * @author Michael Wittmann
 */
public class TaxiLastLogOffComparator implements Comparator<Taxi> {

	@Override
	public int compare(Taxi o1, Taxi o2) {
		return Long.compare(o1.getLastLogOff(), o2.getLastLogOff());
	}

}
