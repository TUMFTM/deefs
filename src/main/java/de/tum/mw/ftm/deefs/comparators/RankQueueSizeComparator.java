package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;

import java.util.Comparator;

/**
 * Comparator to compare taxi rank according to their queue size.
 * Orders taxis ranks in ascending order based on their queue size.
 *
 * @author Michael Wittmann
 */
public class RankQueueSizeComparator implements Comparator<TaxiRank> {

	@Override
	public int compare(TaxiRank o1, TaxiRank o2) {
		return Integer.compare(o1.getQueueSize(), o2.getQueueSize());
	}

}
