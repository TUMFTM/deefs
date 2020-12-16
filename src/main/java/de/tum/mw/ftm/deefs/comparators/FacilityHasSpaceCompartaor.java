package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.Facility;

import java.util.Comparator;


/**
 * Compares Facilities based on their availability of space in ascending order (free, free, occupied)...
 *
 * @author Michael Wittmann
 */
public class FacilityHasSpaceCompartaor implements Comparator<Facility> {

	@Override
	public int compare(Facility o1, Facility o2) {
		return Boolean.compare(o1.hasSpace(), o2.hasSpace());
	}

}
