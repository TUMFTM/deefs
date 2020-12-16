package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;
import de.tum.mw.ftm.deefs.location.Position;

import java.util.Comparator;

/**
 * Compares the distance of a single source Point to a list of possible targets.
 * Orders them in ascending order.
 *
 * @author Michael Wittmann
 * @see Position#calcDist(Position)
 */
public class NearestCarComparator implements Comparator<Taxi> {
	private final Position pos;


	public NearestCarComparator(Position pos) {
		super();
		this.pos = pos;
	}

	@Override
	public int compare(Taxi o1, Taxi o2) {
		double dist1 = o1.getPosition().calcDist(pos);
		double dist2 = o2.getPosition().calcDist(pos);
		if (dist1 > dist2) return 1;
		else if (dist1 < dist2) return -1;
		else {
			if (o1.connectedToFacility() == o2.connectedToFacility()) {
				if (o1.connectedToFacility() instanceof TaxiRank) {
					TaxiRank rank = (TaxiRank) o1.connectedToFacility();
					return rank.getQueuePosition(o1) - rank.getQueuePosition(o2);
				} else {
					return 0;
				}
			} else return 0;
		}
	}
}
