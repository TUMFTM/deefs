package de.tum.mw.ftm.deefs.comparators;

import com.graphhopper.util.GPXEntry;

import java.util.Comparator;

/**
 * Compares two GPX Entries by their timestamp in ascending order.
 *
 * @author Michael Wittmann
 */
public class GPXEntryTimeComparator implements Comparator<GPXEntry> {


	@Override
	public int compare(GPXEntry o1, GPXEntry o2) {
		return Long.compare(o1.getMillis(), o2.getMillis());
	}

}
