package de.tum.mw.ftm.deefs;

import de.tum.mw.ftm.deefs.demand.DemandList;
import de.tum.mw.ftm.deefs.elements.TaxiAgency;
import de.tum.mw.ftm.deefs.elements.TaxiController;
import de.tum.mw.ftm.deefs.elements.facilitiies.FacilityFactory;
import de.tum.mw.ftm.deefs.elements.facilitiies.FacilityList;
import de.tum.mw.ftm.deefs.elements.taxi.TaxiFactory;
import de.tum.mw.ftm.deefs.events.*;
import de.tum.mw.ftm.deefs.log.sqlite.DBLog;
import de.tum.mw.ftm.deefs.utils.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


/**
 * Container Class, holding the whole simulation-environment together.
 *
 * @author Michael Wittmann
 */
public class Scenario {


	private final PriorityQueue<Event> taskList;         // Event Queue. Events will be ordered by their natural Order
	private final List<Event> deniedEvents;             // Stores denied events, for further usage
	private final DBLog dbLog;                         // DBlogger to write simulation results to DB.
	private TaxiController controller;             // TaxiController controls the count of active Taxis during Simulation
	private TaxiAgency agency;                     // TaxiAgency holds the fleet, and manages dispatching process
	private FacilityList facilities;             // FacilityList hodls all facilities for the simulation (TaxiRanks, and ChargingStations)
	private ProgressBar progressBar;


	public Scenario() {

		// initializing basic elements
		dbLog = new DBLog();
		taskList = new PriorityQueue<>();
		deniedEvents = new ArrayList<>();
	}

	/**
	 * Adds an Event to the event queue. Events will be added by their natural order
	 *
	 * @param e Event to be added
	 */
	public void addEvent(Event e) {
		taskList.add(e);
	}

	/**
	 * Removes an Event from the event queue.
	 *
	 * @param e Event to be removed
	 */
	public void removeEvent(Event e) {
		taskList.remove(e);
	}


	/**
	 * Returns the Instance of Taxi Agency
	 *
	 * @return
	 */
	public TaxiAgency getAgency() {
		return this.agency;
	}


	/**
	 * Returns a List with all Facilities available in the Scenario
	 *
	 * @return
	 */
	public FacilityList getFacilities() {
		return this.facilities;
	}


	/**
	 * Returns the instance of DBLog
	 *
	 * @return
	 */
	public DBLog getDBLog() {
		return this.dbLog;
	}


	/**
	 * Call this method to initializes the simulation-scenario, by reading the informations given in the input files.
	 * Following Elements will be setted up:
	 * <p>TaxiController
	 * <br>TaxiAgency (including the fleet)
	 * <br>Facilities
	 */
	protected void initialize() {
		//Setting up Taxi Controller first. 
		controller = new TaxiController(this);
		controller.addInitialEvents(Config.getProperty(Config.CONTROLLER_INPUT_FILE));

		//Setting up agency
		agency = new TaxiAgency(this);

		//Next adding facilities
		FacilityFactory facilityFactory = new FacilityFactory(this);
		facilities = facilityFactory.getFacilitiesFromXML(Config.getProperty(Config.FACILITY_INPUT_FILE));

		//Next adding demand events
		taskList.addAll(DemandList.getEventList(Config.getProperty(Config.DEMAND_INPUT_FILE)));

		//Next adding vehicle Fleet
		TaxiFactory taxiFactory = new TaxiFactory(this, agency);
		taxiFactory.getTaxiFromXML(Config.getProperty(Config.FLEET_INPUT_FILE));
	}


	/**
	 * Starts the simulation.
	 * <p> Make shure that initialize was called first
	 */
	public void run() {

		// write initial informations to resultsDB
		dbLog.addFacilities(facilities.getFacilities());
		dbLog.addFleet(agency.getFleet());

		System.out.println("Number of Events: " + taskList.size());

		// Initialize Progessbar with number of initial events
		progressBar = new ProgressBar((int) taskList.stream().filter(c -> c instanceof DemandEvent).count());

		// start handling tasks
		handleTasks();

		// display unserved events in console
		System.out.println("Unserved Events:" + deniedEvents.size());

		// finally flush Log
		dbLog.flush();
	}


	/**
	 * Main Routine of the simulation. This Method will run as long there are unserved events.
	 * Depending of the type of event a certain action will be performed
	 */
	private void handleTasks() {
		// work until tasklist is empty
		while (!taskList.isEmpty()) {
			Event e = taskList.poll();
			if (e instanceof CarLocationUpdateEvent) {
				((CarLocationUpdateEvent) e).updateCar();
			} else if (e instanceof UpdateChargeEvent) {
				((UpdateChargeEvent) e).updateSOC();
			} else if (e instanceof DemandEvent) {
				if (!agency.tryToPlaceCustomerRequest((DemandEvent) e)) {
					deniedEvents.add(e);
				}
				progressBar.incrementProgress();
			} else if (e instanceof FullChargedEvent) {
				((FullChargedEvent) e).disconnect();
			} else if (e instanceof TaxiControlNewTargetCountEvent) {
				controller.setNTarget(((TaxiControlNewTargetCountEvent) e).getNCars(), e.getScheduledTime());
			} else if (e instanceof TaxiControlEvent) {
				controller.controlActiveTaxiCount(e.getScheduledTime());
			}
		}
	}


}
