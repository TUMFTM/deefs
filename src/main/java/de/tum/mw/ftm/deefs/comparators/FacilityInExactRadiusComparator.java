package de.tum.mw.ftm.deefs.comparators;

import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopper;
import de.tum.mw.ftm.deefs.elements.facilitiies.Facility;
import de.tum.mw.ftm.deefs.location.Position;

import java.util.Comparator;

/**
 * Orders Facilities if they are in Coarse radius around the reference point or not.
 * <p> <b>Attention:</b> Be aware that comparing objects on their exact distance causes route calculations. Make sure to pre-select the Facilities you want to
 * compare to avoid a lack of performance. If the exact distance is unimportant use {@link FacilityInCoarseRadiusComparator}.
 *
 * @author Michael Wittmann
 * @see GraphHopper
 */
public class FacilityInExactRadiusComparator implements Comparator<Facility> {

	private final float radius;        //accepted radius in m
	private final Position position;    //reference position for distance calculation
	private final GraphHopper hopper; //GraphHopper instance for route calculation

	/**
	 * New instance of Comparator
	 *
	 * @param position reference position for distance calculation
	 * @param radius   accepted radius in m
	 * @param hopper   GraphHopper instance for route calculation
	 */
	public FacilityInExactRadiusComparator(Position position, float radius, GraphHopper hopper) {
		this.position = position;
		this.radius = radius;
		this.hopper = hopper;
	}

	@Override
	public int compare(Facility o1, Facility o2) {
		double distance1;
		double distance2;
		//calc coarse distances for a first check 
		distance1 = o1.getPosition().calcDist(position);
		distance2 = o2.getPosition().calcDist(position);

		if (distance1 <= radius && distance2 <= radius) {
			//calc exact distances
			distance1 = hopper.route(new GHRequest(position.getLat(), position.getLon(), o1.getPosition().getLat(), o1.getPosition().getLon())).getDistance();
			distance2 = hopper.route(new GHRequest(position.getLat(), position.getLon(), o2.getPosition().getLat(), o2.getPosition().getLon())).getDistance();
			if (distance1 <= radius && distance2 <= radius) {
				return -(Double.compare(distance1, distance2));
			} else if (distance1 <= radius) {
				return Boolean.compare(distance1 <= radius, false);
			} else if (distance2 <= radius) {
				return Boolean.compare(false, distance2 <= radius);
			} else {
				return Boolean.compare(false, false);
			}
		} else if (distance1 <= radius) {
			distance1 = hopper.route(new GHRequest(position.getLat(), position.getLon(), o1.getPosition().getLat(), o1.getPosition().getLon())).getDistance();
			return Boolean.compare(distance1 <= radius, false);
		} else if (distance2 <= radius) {
			return Boolean.compare(false, distance2 <= radius);
		} else {
			return Boolean.compare(false, false);
		}
	}

}
