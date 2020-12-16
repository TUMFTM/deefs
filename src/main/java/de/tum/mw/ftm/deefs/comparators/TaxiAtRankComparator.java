package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;

import java.util.Comparator;

/**
 * Comparator to compare taxis according to their queue position at a taxi rank.
 * Orders Taxis in ascending order based on their queue position.
 *
 * @author Michael Wittmann
 */
public class TaxiAtRankComparator implements Comparator<Taxi> {

    @Override
    public int compare(Taxi o1, Taxi o2) {
        if (o1.connectedToFacility() == o2.connectedToFacility()) {
            if (o1.connectedToFacility() instanceof TaxiRank) {
                TaxiRank rank = (TaxiRank) o1.connectedToFacility();
                return rank.getQueuePosition(o1) - rank.getQueuePosition(o2);
            }
        }
        return 0;
    }
}