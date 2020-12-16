package de.tum.mw.ftm.deefs.comparators;

import de.tum.mw.ftm.deefs.elements.facilitiies.ChargingStation;
import de.tum.mw.ftm.deefs.elements.taxi.BEVTaxi;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;

import java.util.Comparator;


/**
 * Comparator to compare taxis at the same charging station according to their current SOC.
 * Orders Taxis in descending order based on their current SOC.
 *
 * @author Michael Wittmann
 */
public class TaxiAtChargingStationComparator implements Comparator<Taxi> {

    @Override
    public int compare(Taxi o1, Taxi o2) {
        if (o1.connectedToFacility() == o2.connectedToFacility()) {
            if (o1.connectedToFacility() instanceof ChargingStation) {
                return Float.compare(((BEVTaxi) o1).getSOC(), ((BEVTaxi) o2).getSOC());
            }
        }
        return 0;
    }
}