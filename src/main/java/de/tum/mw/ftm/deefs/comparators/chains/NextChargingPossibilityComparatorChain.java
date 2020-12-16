package de.tum.mw.ftm.deefs.comparators.chains;

import de.tum.mw.ftm.deefs.comparators.ChargingPossibilityQueueSizeComparator;
import de.tum.mw.ftm.deefs.comparators.FacilityExactDistanceComparator;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.graphopper.extensions.MyGraphHopper;
import de.tum.mw.ftm.deefs.location.Position;
import org.apache.commons.collections.comparators.ComparatorChain;

/**
 * Comparator Chain to determine the next chosen charging possibility.
 * <br> 2-Step sorting process:
 * <p> 1. Charging possibility queue size {@link ChargingPossibilityQueueSizeComparator}.
 * <p> 2. Exact distance to facility {@link FacilityExactDistanceComparator}.
 *
 * @author Michael Wittmann
 */
@SuppressWarnings("serial")
public class NextChargingPossibilityComparatorChain extends ComparatorChain {

    public NextChargingPossibilityComparatorChain(Position pos, ChargingInterface ci, float remainingRange, MyGraphHopper hopper) {
        super();
//		this.addComparator(new FacilityInCoarseRadiusComparator(pos, remainingRange));
//		this.addComparator(new FacilityInExactRadiusComparator(pos, remainingRange, hopper), true);
//		this.addComparator(new ChargingPossibilityHasFreeCPComperator(ci), true)
        this.addComparator(new ChargingPossibilityQueueSizeComparator());
        //TODO: Check if the compare of available max. power can be added here...
        this.addComparator(new FacilityExactDistanceComparator(pos, hopper));

    }
}
