package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.Facility;
import de.tum.mw.ftm.deefs.location.Position;

import java.util.Comparator;

/**
 * Orders Facilities according to their coarse distance to the given reference position.
 *
 * @author Michael Wittmann
 * @see Position#calcDist(Position)
 */
public class FacilityCoarseDistanceComparator implements Comparator<Facility> {
	private final Position pos;

	public FacilityCoarseDistanceComparator(Position pos) {
		super();
		this.pos = pos;
	}

	@Override
	public int compare(Facility o1, Facility o2) {
		double dist1 = o1.getPosition().calcDist(pos);
		double dist2 = o2.getPosition().calcDist(pos);
		return Double.compare(dist1, dist2);
	}

}
