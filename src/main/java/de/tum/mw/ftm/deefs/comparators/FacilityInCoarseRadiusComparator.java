package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.Facility;
import de.tum.mw.ftm.deefs.location.Position;

import java.util.Comparator;

/**
 * Orders Facilities if they are in Coarse radius around the reference point or not.
 *
 * @author Michael Wittmann
 * @see Position#calcDist(Position)
 */
public class FacilityInCoarseRadiusComparator implements Comparator<Facility> {

	private final float radius;        //radius to be checked in m
	private final Position position;  //reference position

	/**
	 * New instance of Comparator
	 *
	 * @param position reference position for distance calculation
	 * @param radius   accepted radius in m
	 */
	public FacilityInCoarseRadiusComparator(Position position, float radius) {
		this.position = position;
		this.radius = radius;
	}

	@Override
	public int compare(Facility o1, Facility o2) {
		return Boolean.compare(o1.getPosition().calcDist(position) <= radius, o2.getPosition().calcDist(position) <= radius);

	}

}
