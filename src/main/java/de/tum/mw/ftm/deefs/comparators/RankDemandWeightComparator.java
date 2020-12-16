package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;

import java.util.Comparator;

/**
 * Comparator to compare taxi ranks according to their demand weight.
 * Orders taxis ranks in ascending order based on their demand weight.
 * See {@link TaxiRank#getDemandWeight(long)} for more details.
 *
 * @author Michael Wittmann
 */
public class RankDemandWeightComparator implements Comparator<TaxiRank> {

	private final long time;

	public RankDemandWeightComparator(long time) {
		this.time = time;
	}

	@Override
	public int compare(TaxiRank o1, TaxiRank o2) {
		return Float.compare(o1.getDemandWeight(time), o2.getDemandWeight(time));
	}

}
