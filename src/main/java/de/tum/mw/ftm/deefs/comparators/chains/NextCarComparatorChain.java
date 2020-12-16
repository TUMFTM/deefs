package de.tum.mw.ftm.deefs.comparators.chains;


import de.tum.mw.ftm.deefs.comparators.DistanceComparator;
import de.tum.mw.ftm.deefs.comparators.TaxiAtChargingStationComparator;
import de.tum.mw.ftm.deefs.comparators.TaxiAtRankComparator;
import de.tum.mw.ftm.deefs.location.Position;
import org.apache.commons.collections.comparators.ComparatorChain;

/**
 * Comparator Chain to determine the next car selected for dispatching process.
 * <br> 3-Step sorting process:
 * <p> 1. Coarse distance {@link DistanceComparator}.
 * <p> 2. Position at rank queue {@link TaxiAtRankComparator}.
 * <p> 3. SOC level if at charging station {@link TaxiAtChargingStationComparator}.
 *
 * @author Michael Wittmann
 */
@SuppressWarnings("serial")
public class NextCarComparatorChain extends ComparatorChain {

	public NextCarComparatorChain(Position pos) {
		super();
		this.addComparator(new DistanceComparator(pos));
		this.addComparator(new TaxiAtRankComparator());
		this.addComparator(new TaxiAtChargingStationComparator(), true);
	}
}
