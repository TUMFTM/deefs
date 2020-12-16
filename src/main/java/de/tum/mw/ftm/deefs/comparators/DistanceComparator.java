package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.PhysicalElement;
import de.tum.mw.ftm.deefs.location.Position;

import java.util.Comparator;

/**
 * Compares the distance of a single source Point to a list of possible target Points.
 *
 * @author Michael Wittmann
 * @see Position#calcDist(Position)
 */
public class DistanceComparator implements Comparator<PhysicalElement> {
	private final Position pos;


	/**
	 * New comparator instance
	 *
	 * @param pos reference position
	 */
	public DistanceComparator(Position pos) {
		super();
		this.pos = pos;
	}

	@Override
	public int compare(PhysicalElement o1, PhysicalElement o2) {
		double dist1 = o1.getPosition().calcDist(pos);
		double dist2 = o2.getPosition().calcDist(pos);
		return Double.compare(dist1, dist2);
	}

}
