package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;

import java.util.Comparator;

/**
 * Comparator to compare taxi ranks according to their free lots.
 * Orders Taxis in descending order based on their number of free lots.
 *
 * @author Michael Wittmann
 */
public class RankSpaceLeftComparator implements Comparator<TaxiRank> {
	@Override
	public int compare(TaxiRank o1, TaxiRank o2) {
		return Integer.compare(o1.getRemainingSpace(), o2.getRemainingSpace());
	}

}
