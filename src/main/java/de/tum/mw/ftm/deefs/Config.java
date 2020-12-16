package de.tum.mw.ftm.deefs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Helper class to get project properties from configuration file
 *
 * @author Michael Wittmann
 * @see Properties
 */
public class Config {
	public final static String FILENAME = "config.properties";

	//Property Keys

	//INPUT-FILES
	public static final String CONTROLLER_INPUT_FILE = "controller_input_file";
	public static final String FACILITY_INPUT_FILE = "facility_input_file";
	public static final String DEMAND_INPUT_FILE = "demand_input_file";
	public static final String FLEET_INPUT_FILE = "fleet_input_file";

	//GRAPHHOPPER
	public static final String GRAPHHOPPER_FOLDER_GRAPH = "graphhopper_folder_graph";
	public static final String GRAPHHOPPER_OSM_FILE = "graphhopper_osm_file";

	//OUTPUTS
	public static final String OUTPUT_FOLDER = "output_folder";
	public static final String DB_NAME = "db_name";

	// CHARGINGPOINT
	public static final String CHARGINGPOINT_UPDATE_INTERVAL = "chargingpoint_update_interval";
	public static final String CHARGINGPOINT_CHARGING_CURVE_DELTA_T = "chargingpoint_charging_curve_delta_t";

	// CONNECTOR 
	public static final String CONNECTOR_PLUG_IN_TIME = "connector_plug_in_time";

	// TAXI
	public static final String TAXI_MAX_TIME_ACTIVE = "taxi_max_time_active";
	public static final String TAXI_MIN_TIME_ACTIVE = "taxi_min_time_active";
	public static final String TAXI_MIN_TIME_INACTIVE = "taxi_min_time_inactive";

	// BEVTAXI
//	public static final String BEVTAXI_SOC_MIN = "bevtaxi_soc_min";
//	public static final String BEVTAXI_SOC_RECHARGE = "bevtaxi_soc_recharge";

	public static final String BEVTAXI_REMAINING_RANGE_MIN = "bevtaxi_remaining_range_min";
	public static final String BEVTAXI_REMAINING_RANGE_RECHARGE = "bevtaxi_remaining_range_recharge";


	public static final String BEVTAXI_SOC_MAX_STOP_CHARGE = "bevtaxi_soc_max_stop_charge";
	public static final String BEVTAXI_SOC_MIN_STOP_CHARGE = "bevtaxi_soc_min_stop_charge";
	public static final String BEVTAXI_MIN_TIME_CHARGING = "bevtaxi_min_time_charging";
	public static final String BEVTAXI_MAX_DISTANCE_BEST_CONNECTOR = "bevtaxi_max_distance_best_connector";

	public static final String DEBUGMODE = "debugMode";


	/**
	 * Looks for Properties in config.properties.
	 * If configuration file was not found. The Application gets terminated.
	 *
	 * @return Properties from configuration file.
	 */
	public static Properties getProperties() {
		Properties properties = new Properties();
		BufferedInputStream stream;
		if (!fileExists()) {
			System.out.println("Could not find config.properties\nApplication closed\n");
			System.exit(1);
		}
		try {
			stream = new BufferedInputStream(new FileInputStream(FILENAME));
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}

	/**
	 * Searches for the property with the specified key in File <b>config.properties</b>.
	 * If the key is not found in this property list, the default property list, and its defaults,
	 * recursively, are then checked. The method returns null if the property is not found.
	 * If the configuration file was not found, the Application will terminate.
	 *
	 * @param key the property key.
	 * @return the value in this property list with the specified key value.
	 */
	public static String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	/**
	 * Searches for the property with the specified key in File <b>config.properties</b>.
	 * If the key is not found in this property list, the default property list, and its defaults,
	 * recursively, are then checked. The method returns null if the property is not found.
	 * If the configuration file was not found, the Application will terminate.
	 *
	 * @param key          the property key.
	 * @param defaultValue a default value.
	 * @return the value in this property list with the specified key value.
	 */
	public static String getProperty(String key, String defaultValue) {
		return getProperties().getProperty(key, defaultValue);
	}


	/**
	 * Checks if configuration file exists.
	 *
	 * @return <b>true</b> if configuration file exists, <b>false</b> otherwise
	 */
	private static boolean fileExists() {
		File props = new File(FILENAME);
		return props.exists();
	}

	/**
	 * Checks debug mode was activated in config file
	 *
	 * @return <b>true</b> if debug tag was set, <b>false</b> otherwise
	 * temporally out of order...
	 */
	public static boolean isDebugMode() {
		String debugMode = getProperties().getProperty(DEBUGMODE, "FALSE");
		debugMode = debugMode.toLowerCase();
		switch (debugMode) {
			case "1":
				return true;
			case "true":
				return true;
			case "on":
				return true;
			default:
				return false;
		}

	}
}
