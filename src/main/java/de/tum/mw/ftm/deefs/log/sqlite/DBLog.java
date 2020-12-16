package de.tum.mw.ftm.deefs.log.sqlite;

import de.tum.mw.ftm.deefs.Config;
import de.tum.mw.ftm.deefs.elements.facilitiies.ChargingStation;
import de.tum.mw.ftm.deefs.elements.facilitiies.Facility;
import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;
import de.tum.mw.ftm.deefs.elements.taxi.BEVTaxi;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;
import de.tum.mw.ftm.deefs.log.*;
import de.tum.mw.ftm.deefs.utils.StringUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * DataLogger. This class can be used to log simulation results to a output sqlite Database.
 * <p> A database with the given name from config.properties will be created automatically when creating a instance of this object.
 * <p> Log-data will be pushed to the Database in Bacthes. As longs as the defined batch size is not reached the data remains in local memory.
 * Make shure to flush the DBLog when finishing the simulation.
 *
 * @author Michael Wittmann
 */
public class DBLog {

	// Batch Size 
	private final int BATCH_SIZE = 1000;

	private final String path;

	//Database Fields
	private final String TABLE_FLEET = "fleet";
	private final String FLEET_CAR_ID = "car_id";
	private final String FLEET_TYPE = "type";
	private final String FLEET_CONCEPT_NAME = "concept_name";
	private final String FLEET_E_MEAN = "e_mean";
	private final String FLEET_E_BAT = "e_bat";
	private final String TABLE_FACILITY = "facility";
	private final String FACILITY_TYPE = "type";
	private final String FACILITY_ID = "facility_id";
	private final String FACILITY_LAT = "lat";
	private final String FACILITY_LON = "lon";
	private final String FACILITY_AREA = "area";
	private final String FACILITY_DESCRIPTION = "description";
	private final String TABLE_TRACKPOINT = "trackpoint";
	private final String TRACKPOINT_CAR_ID = "car_id";
	private final String TRACKPOINT_SHIFT_COUNT = "shift_count";
	private final String TRACKPOINT_TRACK_ID = "track_id";
	private final String TRACKPOINT_TIME = "time";
	private final String TRACKPOINT_STATUS = "status";
	private final String TRACKPOINT_FACILITY_ID = "facility_id";
	private final String TRACKPOINT_LAT = "lat";
	private final String TRACKPOINT_LON = "lon";
	private final String TRACKPOINT_DISTANCE = "distance";
	private final String TRACKPOINT_SOC = "soc";
	private final String TABLE_ENERGY_STATS = "energy";
	private final String ENERGY_STATS_FACILITY_ID = "facility_id";
	private final String ENERGY_STATS_CAR_ID = "car_id";
	private final String ENERGY_STATS_TIME = "time";
	private final String ENERGY_STATS_ENERGY = "energy";
	private final String ENERGY_STATS_POWER = "power";
	private final String ENERGY_STATS_CONNECTOR = "connector";
	private final String ENERGY_STATS_P_MAX = "p_max";
	private final String TABLE_FACILITY_STATS = "facility_stats";
	private final String FACILITY_STATS_FACILITY_ID = "facility_id";
	private final String FACILITY_STATS_TIME = "time";
	private final String FACILITY_STATS_CAR_ID = "car_id";
	private final String FACILITY_STATS_ACTION = "action";
	private final String FACILITY_STATS_CONNECTED_CARS = "connected_cars";
	private final String FACILITY_STATS_WAITING_CARS = "waiting_cars";
	private final String TABLE_DENIED_RIDES = "denied_rides";
	private final String DENIED_RIDES_ID = "track_id";
	private final String DENIED_RIDES_CAR_ID = "car_id";
	private final String DENIED_RIDES_TIME = "time";
	private final String DENIED_RIDES_DISTANCE_TO_CUSTOMER = "disctace_to_customer";
	private final String DENIED_RIDES_TRACK_DISTANCE = "track_distance";
	private final String DENIED_RIDES_REASON = "reason";
	private final String TABLE_CONTROLLER = "controller";
	private final String CONTROLLER_TIME = "time";
	private final String CONTROLLER_TYPE = "type";
	private final String CONTROLLER_N = "n";
	private final String TABLE_CONFIG = "config";
	private final String CONFIG_ATTRIBUTE = "attribute";
	private final String CONFIG_VALUE = "value";
	// Buffers
	private final List<Trackpoint> buffer_trackpoints;
	private final List<FacilityStats> buffer_facilityStats;
	private final List<EnergyStats> buffer_energyStats;
	private final List<DeniedRide> buffer_deniedRides;
	private final List<ControllerStats> buffer_controllerStats;
	// Sqlite Connection
	private Connection connection = null;


	/**
	 * Creates a new Instance of DBLog. A new Sqlite Database will be created. The database name and filepath can be configured in config.properties
	 */
	public DBLog() {
		File logDir = new File(Config.getProperty(Config.OUTPUT_FOLDER, "output/"));

		logDir.setWritable(true);
		if (!logDir.exists()) {
			try {
				logDir.mkdirs();
			} catch (SecurityException se) {

			}
		}
		path = String.format("%s%s_%s.db",
				Config.getProperty(Config.OUTPUT_FOLDER, "output/"),
				Config.getProperty(Config.DB_NAME, "result"),
				StringUtils.dateToStringFormatYYYYMMDD_HHMMSS(new Date()));
		connect();
		buffer_trackpoints = new ArrayList<>(BATCH_SIZE);
		buffer_facilityStats = new ArrayList<>(BATCH_SIZE);
		buffer_energyStats = new ArrayList<>(BATCH_SIZE);
		buffer_deniedRides = new ArrayList<>(BATCH_SIZE);
		buffer_controllerStats = new ArrayList<>(BATCH_SIZE);
		initialize();
		writeConfigs();
		closeConnection();
	}


	/**
	 * Initialize Database by creating tables
	 */
	private void initialize() {
		try {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(String.format("DROP TABLE IF EXISTS %s", TABLE_FLEET));
			stmt.executeUpdate(String.format("DROP TABLE IF EXISTS %s", TABLE_ENERGY_STATS));
			stmt.executeUpdate(String.format("DROP TABLE IF EXISTS %s", TABLE_FACILITY));
			stmt.executeUpdate(String.format("DROP TABLE IF EXISTS %s", TABLE_TRACKPOINT));
			stmt.executeUpdate(String.format("DROP TABLE IF EXISTS %s", TABLE_FACILITY_STATS));
			stmt.executeUpdate(String.format("DROP TABLE IF EXISTS %s", TABLE_DENIED_RIDES));
			stmt.executeUpdate(String.format("DROP TABLE IF EXISTS %s", TABLE_CONTROLLER));
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
							+ "%s INTEGER PRIMARY KEY,"
							+ "%s TEXT, "
							+ "%s TEXT, "
							+ "%s REAL,"
							+ "%s REAL);",
					TABLE_FLEET, FLEET_CAR_ID, FLEET_TYPE, FLEET_CONCEPT_NAME, FLEET_E_MEAN, FLEET_E_BAT));
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
							+ "%s INTEGER PRIMARY KEY,"
							+ "%s TEXT, "
							+ "%s REAL,"
							+ "%s REAL,"
							+ "%s INTEGER,"
							+ "%s TEXT);",
					TABLE_FACILITY, FACILITY_ID, FACILITY_TYPE, FACILITY_LAT, FACILITY_LON, FACILITY_AREA, FACILITY_DESCRIPTION));
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
							+ "%s INTEGER,"
							+ "%s INTEGER,"
							+ "%s INTEGER,"
							+ "%s INTEGER,"
							+ "%s TEXT,"
							+ "%s INTEGER,"
							+ "%s REAL,"
							+ "%s REAL,"
							+ "%s REAL,"
							+ "%s REAL);",
					TABLE_TRACKPOINT, TRACKPOINT_CAR_ID, TRACKPOINT_SHIFT_COUNT, TRACKPOINT_TRACK_ID, TRACKPOINT_TIME, TRACKPOINT_STATUS, TRACKPOINT_FACILITY_ID,
					TRACKPOINT_LAT, TRACKPOINT_LON, TRACKPOINT_DISTANCE, TRACKPOINT_SOC));
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
							+ "%s INTEGER,"
							+ "%s INTEGER,"
							+ "%s INTEGER, "
							+ "%s REAL,"
							+ "%s REAL,"
							+ "%s TEXT, "
							+ "%s REAL);",
					TABLE_ENERGY_STATS, ENERGY_STATS_FACILITY_ID, ENERGY_STATS_CAR_ID, ENERGY_STATS_TIME, ENERGY_STATS_ENERGY, ENERGY_STATS_POWER, ENERGY_STATS_CONNECTOR, ENERGY_STATS_P_MAX));
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
							+ "%s INTEGER,"
							+ "%s INTEGER,"
							+ "%s INTEGER,"
							+ "%s TEXT,"
							+ "%s INTEGER,"
							+ "%s INTEGER);",
					TABLE_FACILITY_STATS, FACILITY_STATS_FACILITY_ID, FACILITY_STATS_CAR_ID, FACILITY_STATS_TIME, FACILITY_STATS_ACTION, FACILITY_STATS_CONNECTED_CARS, FACILITY_STATS_WAITING_CARS));
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
							+ "%s INTEGER,"
							+ "%s INTEGER,"
							+ "%s INTEGER,"
							+ "%s REAL,"
							+ "%s REAL,"
							+ "%s TEXT);",
					TABLE_DENIED_RIDES, DENIED_RIDES_ID, DENIED_RIDES_CAR_ID, DENIED_RIDES_TIME, DENIED_RIDES_TRACK_DISTANCE, DENIED_RIDES_DISTANCE_TO_CUSTOMER, DENIED_RIDES_REASON));
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
							+ "%s INTEGER,"
							+ "%s TEXT,"
							+ "%s INTEGER);",
					TABLE_CONTROLLER, CONTROLLER_TIME, CONTROLLER_TYPE, CONTROLLER_N));
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
							+ "%s TEXT,"
							+ "%s TEXT);",
					TABLE_CONFIG, CONFIG_ATTRIBUTE, CONFIG_VALUE));
			connection.commit();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Writes all informations from config.properties-file into db
	 */
	private void writeConfigs() {
		List<Object> keys = new ArrayList<>(Config.getProperties().keySet());
		List<Object> values = new ArrayList<>(Config.getProperties().values());
		try {
			PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?)", TABLE_CONFIG));
			for (int i = 0; i < keys.size(); i++) {
				ps.setString(1, keys.get(i).toString());
				ps.setString(2, values.get(i).toString());
				ps.addBatch();
			}
			ps.executeBatch();
			connection.commit();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * writes the defined facilities to the db
	 *
	 * @param facilities facilites used in this simulation run
	 */
	public void addFacilities(List<Facility> facilities) {
		connect();
		try {
			PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?,?,?,?, ?)", TABLE_FACILITY));
			for (Facility facility : facilities) {
				ps.setInt(1, facility.getId());
				if (facility instanceof TaxiRank) {
					ps.setString(2, "RANK");
				} else if (facility instanceof ChargingStation) {
					ps.setString(2, "CHARGINGSTATION");
				}
				ps.setDouble(3, facility.getPosition().getLat());
				ps.setDouble(4, facility.getPosition().getLon());
				ps.setDouble(5, facility.getPosition().getArea());
				if (facility instanceof TaxiRank) {
					ps.setString(6, ((TaxiRank) facility).getAddress());
				} else {
					ps.setNull(6, java.sql.Types.CHAR);
				}
				ps.addBatch();
			}
			ps.executeBatch();
			connection.commit();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}


	/**
	 * writes the defined fleet to the db
	 *
	 * @param fleet vehicle fleet used for this simulation run
	 */
	public void addFleet(List<Taxi> fleet) {
		connect();
		try {
			PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?,?,?,?)", TABLE_FLEET));
			for (Taxi taxi : fleet) {
				ps.setInt(1, taxi.getId());
				ps.setString(2, taxi.getType());
				if (taxi instanceof BEVTaxi) {
					ps.setString(3, ((BEVTaxi) taxi).getConcept().getConceptName());
					ps.setFloat(4, ((BEVTaxi) taxi).getConcept().getVMean());
					ps.setFloat(5, ((BEVTaxi) taxi).getConcept().getBattery().getEBatMax());
				}
				ps.addBatch();
			}
			ps.executeBatch();
			connection.commit();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}


	/**
	 * Adds a new trackpoint to the log-buffer. Trackpoints will be flushed to db if defined batch size is reached or a flush is called manually.
	 *
	 * @param trackpoint Trackpoint to be added
	 * @see Trackpoint
	 */
	public void addTrackpoint(Trackpoint trackpoint) {
		if (buffer_trackpoints.size() == BATCH_SIZE) {
			flushTrackpoints();
		}
		buffer_trackpoints.add(trackpoint);
	}


	/**
	 * Flushes trackpoints from buffer to db
	 */
	private void flushTrackpoints() {
		connect();
		try {
			PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?,?,?,?,?,?,?,?,?)", TABLE_TRACKPOINT));
			for (Trackpoint trackpoint : buffer_trackpoints) {
				ps.setInt(1, trackpoint.getCar_id());
				ps.setInt(2, trackpoint.getShiftCount());
				ps.setInt(3, trackpoint.getTrack_id());
				ps.setLong(4, trackpoint.getTime());
				ps.setString(5, trackpoint.getStatus());
				ps.setInt(6, trackpoint.getFacilityId());
				ps.setDouble(7, trackpoint.getPosition().getLat());
				ps.setDouble(8, trackpoint.getPosition().getLon());
				ps.setFloat(9, trackpoint.getDistance());
				ps.setFloat(10, trackpoint.getSoc());
				ps.addBatch();

			}
			ps.executeBatch();
			connection.commit();
			ps.close();
			buffer_trackpoints.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}

	/**
	 * Adds a new FacilityStat to the log-buffer. FacilityStats will be flushed to db if defined batch size is reached or a flush is called manually.
	 *
	 * @param facilityStats FacilityStats to be added
	 * @see FacilityStats
	 */
	public void addFacilityStats(FacilityStats facilityStats) {
		if (buffer_facilityStats.size() == BATCH_SIZE) {
			flushFacilityStats();
		}
		buffer_facilityStats.add(facilityStats);
	}


	/**
	 * Flushes FacilityStats from buffer to db
	 */
	private void flushFacilityStats() {
		connect();
		try {
			PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?,?,?,?,?)", TABLE_FACILITY_STATS));
			for (FacilityStats facilityStats : buffer_facilityStats) {
				ps.setInt(1, facilityStats.getFacilityID());
				ps.setInt(2, facilityStats.getCarID());
				ps.setLong(3, facilityStats.getTime());
				ps.setString(4, facilityStats.getAction());
				ps.setInt(5, facilityStats.getConnected_cars());
				ps.setInt(6, facilityStats.getWaitingCars());
				ps.addBatch();

			}
			ps.executeBatch();
			connection.commit();
			ps.close();
			buffer_facilityStats.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}

	/**
	 * Adds a new energyStats to the log-buffer. EnergyStats will be flushed to db if defined batch size is reached or a flush is called manually.
	 *
	 * @param energyStats EnergyStats to be added
	 * @see EnergyStats
	 */
	public void addEnergyStats(EnergyStats energyStats) {
		if (buffer_energyStats.size() == BATCH_SIZE) {
			flushEnergyStats();
		}
		buffer_energyStats.add(energyStats);
	}

	/**
	 * Flushes EnergyStats from buffer to db
	 */
	private void flushEnergyStats() {
		connect();
		try {
			PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?,?,?,?,?,?)", TABLE_ENERGY_STATS));
			for (EnergyStats energyStats : buffer_energyStats) {
				ps.setInt(1, energyStats.getFacilityID());
				ps.setInt(2, energyStats.getCarID());
				ps.setLong(3, energyStats.getTime());
				ps.setFloat(4, energyStats.getEnergy());
				ps.setFloat(5, energyStats.getPower());
				ps.setString(6, energyStats.getConnector());
				ps.setFloat(7, energyStats.getPMax());
				ps.addBatch();
			}
			ps.executeBatch();
			connection.commit();
			ps.close();
			buffer_energyStats.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();

	}

	/**
	 * Adds a new DeniedRide to the log-buffer. DeniedRides will be flushed to db if defined batch size is reached or a flush is called manually.
	 *
	 * @param deniedRide DeniedRide to be added
	 * @see DeniedRide
	 */
	public void addDeniedRide(DeniedRide deniedRide) {
		if (buffer_deniedRides.size() == BATCH_SIZE) {
			flushDeniedRides();
		}
		buffer_deniedRides.add(deniedRide);
	}

	/**
	 * Flushes DeniedRides from buffer to db
	 */
	private void flushDeniedRides() {
		connect();
		try {
			PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?,?,?,?,?)", TABLE_DENIED_RIDES));
			for (DeniedRide deniedRide : buffer_deniedRides) {
				ps.setInt(1, deniedRide.getTrack_id());
				ps.setInt(2, deniedRide.getCar_id());
				ps.setLong(3, deniedRide.getTime());
				ps.setDouble(4, deniedRide.getTrack_distance());
				if (deniedRide.getDistance_to_customer() < 0) {
					ps.setNull(5, java.sql.Types.REAL);
				} else {
					ps.setDouble(5, deniedRide.getDistance_to_customer());
				}
				ps.setString(6, deniedRide.getReason());
				ps.addBatch();
			}
			ps.executeBatch();
			connection.commit();
			ps.close();
			buffer_deniedRides.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();

	}

	/**
	 * Adds a new ControllerStats to the log-buffer. ControllerStats will be flushed to db if defined batch size is reached or a flush is called manually.
	 *
	 * @param controllerStats ControllerStats to be added
	 * @see ControllerStats
	 */
	public void addControllerStats(ControllerStats controllerStats) {
		if (buffer_controllerStats.size() == BATCH_SIZE) {
			flushControllerStats();
		}
		buffer_controllerStats.add(controllerStats);
	}


	/**
	 * Flushes ControllerStats from buffer to db
	 */
	private void flushControllerStats() {
		connect();
		try {
			PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?,?,?)", TABLE_CONTROLLER));
			for (ControllerStats controllerStats : buffer_controllerStats) {
				ps.setLong(1, controllerStats.getTime());
				ps.setString(2, controllerStats.getAction());
				ps.setInt(3, controllerStats.getN());
				ps.addBatch();
			}
			ps.executeBatch();
			connection.commit();
			ps.close();
			buffer_controllerStats.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeConnection();
	}


	/**
	 * Flush all buffers to db
	 */
	public void flush() {
		flushFacilityStats();
		flushTrackpoints();
		flushEnergyStats();
		flushDeniedRides();
		flushControllerStats();
	}

	/**
	 * Close connection to sqlite database
	 */
	private void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connection = null;
		}
	}

	/**
	 * connect to sqlite database
	 */
	private void connect() {
		if (connection == null) {
			connection = DBConnection.getConnection(path);
		} else {
			throw new RuntimeException("Error while connecting to DB. There is already an open connection");
		}
	}
}
