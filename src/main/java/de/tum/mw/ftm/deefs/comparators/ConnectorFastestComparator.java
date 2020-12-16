package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.eMobilityComponents.Connector;

import java.util.Comparator;

/**
 * Comparator class to find the fastest(most powerful) Connector.
 *
 * @author Michael Wittmann
 * @see Connector
 */
public class ConnectorFastestComparator implements Comparator<Connector> {

	@Override
	public int compare(Connector o1, Connector o2) {
		return Float.compare(o1.getPMax(), o2.getPMax());
	}

}
