package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;

import java.util.Comparator;

/**
 * use {@link RankDemandWeightComparator}
 *
 * @author Michael Wittmann
 */
@Deprecated
public class RankPriorityComparator implements Comparator<TaxiRank> {


	@Override
	public int compare(TaxiRank o1, TaxiRank o2) {
		return Integer.compare(o1.getPriority(), o2.getPriority());
	}

}
