package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.taxi.Taxi;

import java.util.Comparator;

/**
 * Comparator to compare taxis according to their last log in.
 * Orders Taxis in ascending order based on their time logged in.
 *
 * @author Michael Wittmann
 */
public class TaxiLastLoginComparator implements Comparator<Taxi> {

	@Override
	public int compare(Taxi o1, Taxi o2) {
		return Long.compare(o1.getLastLogin(), o2.getLastLogin());
	}

}
