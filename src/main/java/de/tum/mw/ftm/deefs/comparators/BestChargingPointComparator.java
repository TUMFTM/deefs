package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingPoint;

import java.util.Comparator;


/**
 * Comparator class, to compare different possible charging points for a certain vehicle.
 * Charging points will be compared by the maximum power they support in combination with the given vehicle.
 * <br><b>Notice:</b> The comparator can not filter charging points which are <b>incompatible</b> to the given car.
 * As the maximum power of an incompatible charging point is 0 the comparator will still work, but if you think about a set of charging points which are all incompatible
 * using this comparator wouldn't affect anything. To avoid this issue always look for a set of compatible charging points before using this comparator.
 *
 * @author Michael Wittmann
 */
public class BestChargingPointComparator implements Comparator<ChargingPoint> {

	private final ChargingInterface ci;

	/**
	 * Constructor
	 *
	 * @param ci car for which the best charging point should be found
	 */
	public BestChargingPointComparator(ChargingInterface ci) {
		this.ci = ci;
	}

	@Override
	public int compare(ChargingPoint o1, ChargingPoint o2) {
		return Float.compare(o1.getBestConnector(ci).getPMax(), o2.getBestConnector(ci).getPMax());
	}


}
