package de.tum.mw.ftm.deefs.comparators.chains;

import de.tum.mw.ftm.deefs.comparators.FacilityCoarseDistanceComparator;
import de.tum.mw.ftm.deefs.location.Position;
import org.apache.commons.collections.comparators.ComparatorChain;

/**
 * Comparator Chain to determine the next chosen charging possibility.
 * <br> 1-Step sorting process:
 * <p> 1. coarse distance {@link FacilityCoarseDistanceComparator}.
 *
 * @author Michael Wittmann
 */
@SuppressWarnings("serial")
public class CoarseNextChargingPossibilityComparatorChain extends ComparatorChain {

    public CoarseNextChargingPossibilityComparatorChain(Position pos) {
        this.addComparator(new FacilityCoarseDistanceComparator(pos));
    }

}
