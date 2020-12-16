package de.tum.mw.ftm.deefs.comparators.chains;

import de.tum.mw.ftm.deefs.comparators.FacilityHasSpaceCompartaor;
import de.tum.mw.ftm.deefs.comparators.RankDemandWeightComparator;
import de.tum.mw.ftm.deefs.comparators.RankSpaceLeftComparator;
import de.tum.mw.ftm.deefs.location.Position;
import org.apache.commons.collections.comparators.ComparatorChain;

/**
 * Comparator Chain to determine the next chosen taxi rank.
 * <br> 3-Step sorting process:
 * <p> 1. Facilities that have empty space {@link FacilityHasSpaceCompartaor}.
 * <p> 2. Rank demand weight {@link RankDemandWeightComparator}.
 * <p> 3. Rank space left {@link RankSpaceLeftComparator}.
 *
 * @author Michael Wittmann
 */
@SuppressWarnings("serial")
public class NextRankComparatorChain extends ComparatorChain {

    /**
     * New comparator instance.
     *
     * @param pos  reference position
     * @param time simulation time in ms
     */
    public NextRankComparatorChain(Position pos, long time) {
        super();
        this.addComparator(new FacilityHasSpaceCompartaor(), true);
        this.addComparator(new RankDemandWeightComparator(time), true);
        this.addComparator(new RankSpaceLeftComparator(), true);
//		this.addComparator(new RankQueueSizeComparator());
//		this.addComparator(new RankPriorityComparator(), true);
//		this.addComparator(new FacilityCoarseDistanceComparator(pos));
    }
}
