package de.tum.mw.ftm.deefs.comparators;

import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopper;
import de.tum.mw.ftm.deefs.elements.facilitiies.Facility;
import de.tum.mw.ftm.deefs.graphopper.extensions.MyGraphHopper;
import de.tum.mw.ftm.deefs.location.Position;

import java.util.Comparator;


/**
 * Orders Facilities according to their exact distance to the given reference position.
 * <p> <b>Attention:</b> Be aware that comparing objects on their exact distance causes route calculations. Make sure to pre-select the Facilities you want to
 * compare to avoid a lack of performance. If the exact distance is unimportant use {@link FacilityCoarseDistanceComparator}.
 *
 * @author Michael Wittmann
 * @see GraphHopper
 */
public class FacilityExactDistanceComparator implements Comparator<Facility> {

	private final Position position;    //reference position
	private final GraphHopper hopper; //GraphHopper instance for route calculations


	/**
	 * New Comparator instance
	 *
	 * @param pos    reference position
	 * @param hopper GraphHopper instance for route calculations
	 */
	public FacilityExactDistanceComparator(Position pos, MyGraphHopper hopper) {
		this.position = pos;
		this.hopper = hopper;
	}

	@Override
	public int compare(Facility o1, Facility o2) {
		double dist1 = hopper.route(new GHRequest(position.getLat(), position.getLon(), o1.getPosition().getLat(), o1.getPosition().getLon())).getDistance();
		double dist2 = hopper.route(new GHRequest(position.getLat(), position.getLon(), o2.getPosition().getLat(), o2.getPosition().getLon())).getDistance();
		return Double.compare(dist1, dist2);
	}

}
