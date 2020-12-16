package de.tum.mw.ftm.deefs.graphopper.extensions;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import de.tum.mw.ftm.deefs.Config;

/**
 * Adopted GraphHopper class. By using this class instead of GraphHopper, all setups are done automatically when creating an instance of this class.
 *
 * @author Michael Wittmann
 */
public class MyGraphHopper extends GraphHopper {

	public MyGraphHopper() {
		super();
		this.forServer();    // define perfomrance settings
		this.setInMemory();    // keep graph in memory for better performance
		this.setMinNetworkSize(200, 200);    // define minimum network and one way network size (Suggestion from Graphhopper)
		this.setOSMFile(Config.getProperty(Config.GRAPHHOPPER_OSM_FILE));    // set path to OSM input file

		this.setGraphHopperLocation(Config.getProperty(Config.GRAPHHOPPER_FOLDER_GRAPH)); // set path to graph folder
		this.setEncodingManager(new EncodingManager("car"));    // set up encoding manager

		// now this can take minutes if it imports or a few seconds for loading
		// of course this is dependent on the area you import
		this.importOrLoad();
	}

}
