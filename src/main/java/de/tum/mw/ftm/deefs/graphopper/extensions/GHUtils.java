package de.tum.mw.ftm.deefs.graphopper.extensions;

import com.graphhopper.GHResponse;
import com.graphhopper.util.GPXEntry;

import java.util.List;

/**
 * Helper class providing static methods to process GraphHopper data types
 *
 * @author Michael Wittmann
 */
public class GHUtils {

	/**
	 * This method converts a GHResponse to a list of trackpoints.
	 * Thus the arrival time is just calculated relatively by GraphHopper, the start time must be added up on the forecasted times.
	 * Addiontal informations list instruction list, deliver by GraphHopper are going to be discarded
	 *
	 * @param ghResponse GraphHopper Response
	 * @param start_time time the ride starts in ms
	 * @return List with GPX entries containing all waypoints and arrival times on this route
	 * @see GHResponse
	 */
	public static List<GPXEntry> getGPXList(GHResponse ghResponse, long start_time) {
		if (!ghResponse.hasErrors()) {
			List<GPXEntry> gpxList = ghResponse.getInstructions().createGPXList();
			for (GPXEntry gpxEntry : gpxList) {
				gpxEntry.setMillis(gpxEntry.getMillis() + start_time);
			}
			return gpxList;
		} else {

			return null;
		}
	}

}
