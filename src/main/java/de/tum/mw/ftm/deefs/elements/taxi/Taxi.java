package de.tum.mw.ftm.deefs.elements.taxi;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.GPXEntry;
import de.tum.mw.ftm.deefs.Config;
import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.comparators.GPXEntryTimeComparator;
import de.tum.mw.ftm.deefs.comparators.chains.NextRankComparatorChain;
import de.tum.mw.ftm.deefs.elements.PhysicalElement;
import de.tum.mw.ftm.deefs.elements.facilitiies.Facility;
import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;
import de.tum.mw.ftm.deefs.events.CarLocationUpdateEvent;
import de.tum.mw.ftm.deefs.events.DemandEvent;
import de.tum.mw.ftm.deefs.events.Event;
import de.tum.mw.ftm.deefs.events.TaxiControlEvent;
import de.tum.mw.ftm.deefs.graphopper.extensions.GHUtils;
import de.tum.mw.ftm.deefs.graphopper.extensions.MyGraphHopper;
import de.tum.mw.ftm.deefs.location.Position;
import de.tum.mw.ftm.deefs.log.Trackpoint;

import java.util.List;
import java.util.Observer;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Abstract base class that represents a taxi and provides the basic behavior for this model.
 * From this class special vehicle concepts like electric taxis or hybrid taxis can be derived.
 *
 * @author Michael Wittmann
 */
public abstract class Taxi extends PhysicalElement implements Observer {


	// Possible vehicle states
	public static final int STATUS_LOGGED_OFF = 0;
	public static final int STATUS_AT_RANK = 1;
	public static final int STATUS_ON_WAY_TO_RANK = 2;

	public static final int STATUS_ON_WAY_TO_CUSTOMER = 3;
	public static final int STATUS_OCCUPIED = 4;

	public static final int STATUS_ON_WAY_BACK_HOME = 5;

	public static final int STATUS_ON_WAY_TO_CHARGING_POINT = 6;
	public static final int STATUS_AT_CHARGING_POINT = 7;
	public static final int STATUS_WAIT_FOR_CHARGING = 8;

	public static final String[] STATUS_NAMES = {"logged_off", "at_rank", "on_way_to_rank", "on_way_to_customer", "occupied", "on_way_back_home", "on_way_to_charging_point", "at_charging_point", "wait_for_charging"};
	protected final long min_time_active = Long.parseLong(Config.getProperty(Config.TAXI_MIN_TIME_ACTIVE, "14400000")); //minimumTime a taxi has to be active before it can be logged off in ms default: 4h
	protected final long min_time_inactive = Long.parseLong(Config.getProperty(Config.TAXI_MIN_TIME_INACTIVE, "28800000")); //minimumTime a taxi has to be inactive before it can be logged on again in ms default: 8h
	private final AtomicInteger track_counter;        //progressive track counter
	private final long max_time_active = Long.parseLong(Config.getProperty(Config.TAXI_MAX_TIME_ACTIVE, "32400000")); //maximumTime a car can be active in ms default: 9h
	protected String type;                        //Type of vehicle e.g. BEVTaxi or ICETaxi
	protected int status;                        //vehicle status
	protected PriorityQueue<GPXEntry> route;    //Route buffer, saves waypoints of planned route
	protected DemandEvent customer_ride = null;    //accepted customer ride
	protected Position home;                    //taxis home position
	protected MyGraphHopper hopper;                //GraphHopper instance used for routing operations
	protected AtomicInteger shift_counter;        //progressive shift_counter
	protected int track_id;                        //actual track_id
	protected long last_login = -1;                //time of last login in ms
	protected long last_logoff = -1;            //time of last log off in ms
	protected boolean logOffTriggered = false;    //marker to see if logoff was triggered
	private Event nextPlannedEvent;                //buffer for next planned event
	private Facility connectedToFacility = null;//connected facility
	private Integer targetFacility = 0;            //id of target facility at the end of the current ride


	/**
	 * Basic Constructor for a Taxi.
	 *
	 * @param id       unique id
	 * @param home     home position of this taxi
	 * @param hopper   instance of GraphHopper for routing
	 * @param scenario scenario the taxi operates in
	 */
	protected Taxi(int id, Position home, MyGraphHopper hopper, Scenario scenario) {
		super(id, scenario, home);
		this.type = "default_car";
		this.home = home;
		this.status = STATUS_LOGGED_OFF;

		this.hopper = hopper;
		this.route = new PriorityQueue<>(new GPXEntryTimeComparator());

		this.track_counter = new AtomicInteger();
		this.shift_counter = new AtomicInteger();
		this.track_id = 0;
	}

	/**
	 * @return Taxi's home position
	 */
	public Position getHome() {
		return this.home;
	}

	/**
	 * @return Vehicle Type, if set.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @return Time of last login in ms
	 */
	public long getLastLogin() {
		return last_login;
	}

	/**
	 * @return time of last log off in ms
	 */
	public long getLastLogOff() {
		return last_logoff;
	}

	/**
	 * @return actual shift count
	 */
	public int getShiftCounter() {
		return shift_counter.get();
	}

	/**
	 * Log on this car. The car will automatically drive to the next TaxiRank.
	 *
	 * @param logOnTime
	 * @return returns true if car was logged on successfully, false otherwise.
	 */
	public boolean logOn(long logOnTime) {
		if (this.status == STATUS_LOGGED_OFF) {        //check if vehicle was logged off before
			if ((logOnTime - last_logoff) > min_time_inactive || this.last_logoff == -1) {    //check if min_time_inactive is satisfied
				last_login = logOnTime;
				logOffTriggered = false;
				shift_counter.incrementAndGet();
				startRideToRank(scenario.getFacilities().getRandomRank(position, logOnTime), logOnTime);
				return true;
			}
		} else {
			System.err.println(String.format("Cannot log on Car: %d because it is already logged on!", this.id));
		}
		return false;
	}

	/**
	 * Forces the car to drive to his home position to log off.
	 *
	 * @param logOffTime
	 * @return <b>true</b> if log off is triggered, <b>false</b> otherwise
	 */
	public boolean triggerlogOff(long logOffTime) {
		if ((logOffTime - last_login) > min_time_active) {
			if (isFree() && this.status != STATUS_LOGGED_OFF) {
				if (quitCurrentTask(logOffTime)) {
					startRideHome(logOffTime);
					logOffTriggered = true;
					return true;
				}
			} else {
				System.err.printf("Cannot log off Car %d because it is busy or already logged of!%n", this.id);
				return false;
			}
		}
		return false;
	}

	/**
	 * Log off this car. The car will go to STATUS_LOGGED_OFF
	 *
	 * @param logOffTime simulation time in ms
	 */
	protected void logOff(long logOffTime) {
		logOffTriggered = false;
		setStatus(STATUS_LOGGED_OFF);
		setPosition(position, logOffTime);
		this.last_logoff = logOffTime;
		scenario.addEvent(new TaxiControlEvent(logOffTime));
	}

	/**
	 * @return get actual vehicle state
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Set the actual vehicle state
	 *
	 * @param status vehicle state
	 */
	protected void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return get actual vehicle state as readable string
	 */
	public String getStatusAsText() {
		return STATUS_NAMES[this.status];
	}

	/**
	 * Set connected facility
	 *
	 * @param f facility the car is connected to
	 */
	protected void setConnectedToFacility(Facility f) {
		this.targetFacility = 0;
		this.connectedToFacility = f;
	}

	/**
	 * @return id of target facility
	 */
	public Integer getTargetFacility() {
		return targetFacility;
	}

	/**
	 * Set target facility's id, which is planned to reach at the end of the current route
	 *
	 * @param targetFacility id of target facility at the end of the route
	 */
	public void setTargetFacility(Integer targetFacility) {
		this.targetFacility = targetFacility;
	}

	/**
	 * @return ID of connected facility, 0 if no facility is connected
	 */
	public int connectedToFacilityID() {
		if (this.connectedToFacility != null) {
			return this.connectedToFacility.getId();
		} else return 0;
	}

	/**
	 * @return The instacne of the facility the car is connected to
	 */
	public Facility connectedToFacility() {
		return this.connectedToFacility;
	}

	/**
	 * Set the vehicles position. By updating the position a log entry will be created automatically.
	 *
	 * @param position new position
	 * @param time     simulation time in ms
	 */
	protected void setPosition(Position position, long time) {
		float distance;
		//getting distance to last position. If it is a customer Ride take the original distance
		if (status == STATUS_OCCUPIED && route.size() == 0) {
			distance = (float) customer_ride.getDistance();
		} else {
			distance = (float) this.position.calcDist(position);
		}
		super.setPosition(position);
		//write log 
		logTrackpoint(new Trackpoint(this.id, getShiftCounter(), track_id, time, getStatusAsText(), position, distance, 0, connectedToFacilityID()));
	}

	/**
	 * Pushes a trackpoint to the data logger
	 *
	 * @param tp trackpoint to be logged
	 */
	protected void logTrackpoint(Trackpoint tp) {
		scenario.getDBLog().addTrackpoint(tp);
	}

	/**
	 * @return The accepted customer ride
	 */
	private DemandEvent getCustomerRide() {
		if (customer_ride != null) {
			return customer_ride;
		} else {
			System.err.println("CustomerRide was requested but is null Car:" + id);
			return null;
		}
	}

	/**
	 * Sets the informations of an accepted customer ride.
	 *
	 * @param e Customer DemandEvent
	 */
	protected void setCustomerRide(DemandEvent e) {
		if (customer_ride == null) {
			customer_ride = e;
		} else {
			System.err.println("Error, tried to overwrite an existing customer ride Car:" + id);
		}
	}

	/**
	 * Indicates whether a taxi is basically able to accept customer requests or not.
	 *
	 * @return <b>true</b> if a taxi is basically able to accept a customer request <b>false</b> otherwise.
	 */
	public abstract boolean isFree();

	/**
	 * Indicates whether a taxi is busy, and can not perform further actions.
	 *
	 * @return <b>true</b> if a taxi is busy <b>false</b> otherwise.
	 */
	public abstract boolean isBusy();

	/**
	 * @return <b>true</b> if the taxi is actually logged of, <b>false</b> otherwise.
	 */
	public boolean isLoggedOff() {
		return this.status == STATUS_LOGGED_OFF;
	}

	/**
	 * @return <b>true</b> if the taxi is actually on its way back home, <b>false</b> otherwise.
	 */
	public boolean isOnWayBackHome() {
		return this.status == STATUS_ON_WAY_BACK_HOME;
	}

	/**
	 * Set the track_id, make sure to use unique ids. To get a new id use getNewTrackId().
	 *
	 * @param track_id new track id
	 */
	protected void setTrackId(int track_id) {
		this.track_id = track_id;
	}

	/**
	 * @return Incremented unique track id for this car
	 */
	protected int getNewTrackId() {
		return track_counter.incrementAndGet();
	}

	/**
	 * Starting a ride to the next taxi rank.
	 *
	 * @param start_time simulation time in ms.
	 */
	protected void startRideToNextRank(long start_time) {
		setTrackId(getNewTrackId());
		setStatus(STATUS_ON_WAY_TO_RANK);
		setRoute(findRouteToNextTaxiRank(start_time), start_time);
		updatePosition();
	}

	/**
	 * Starting a ride to a certain taxi rank
	 *
	 * @param rank       target rank
	 * @param start_time simulation time in ms
	 */
	protected void startRideToRank(TaxiRank rank, long start_time) {
		setTargetFacility(rank.getId());
		setTrackId(getNewTrackId());
		setStatus(STATUS_ON_WAY_TO_RANK);
		setRoute(findRoute(position, rank.getPosition()), start_time);
		updatePosition();
	}

	/**
	 * Starting a new ride to a customer
	 */
	protected void startRideToCustomer() {
		setTrackId(getNewTrackId());
		setStatus(STATUS_ON_WAY_TO_CUSTOMER);
		setRoute(findRoute(position, getCustomerRide().getStart()), getCustomerRide().getScheduledTime());
		updatePosition();
	}

	/**
	 * Starting a new customer Ride
	 *
	 * @param startTime startTime of the ride
	 */
	protected void startCustomerRide(long startTime) {
		DemandEvent e = getCustomerRide();
		setTrackId(e.getTrack_id());
		setStatus(STATUS_OCCUPIED);

		GPXEntry entry = new GPXEntry(e.getStart().getLat(), e.getStart().getLon(), startTime);
		route.add(entry);
		entry = new GPXEntry(e.getTarget().getLat(), e.getTarget().getLon(), startTime + e.getDuration());
		route.add(entry);
		updatePosition();
	}


	/**
	 * Starting a new ride back to the home position
	 *
	 * @param startTime simulation time in ms
	 */
	protected void startRideHome(long startTime) {
		setTrackId(getNewTrackId());
		setStatus(STATUS_ON_WAY_BACK_HOME);
		setRoute(findRoute(position, home), startTime);
		updatePosition();
	}


	/**
	 * Looks for the next available taxi rank, according to selection process defined in NextRankComparatorChain.
	 *
	 * @param time simulation time in ms
	 * @return selected taxi rank
	 * @see NextRankComparatorChain
	 */
	@SuppressWarnings("unchecked")
	protected TaxiRank findNextRank(long time) {
		return scenario.getFacilities().getBestRank(new NextRankComparatorChain(position, time), getTargetFacility());
	}


	/**
	 * Calculating the route to the next available taxi rank
	 *
	 * @param time simulation time in ms
	 * @return GHResponse with route informations
	 */
	protected abstract GHResponse findRouteToNextTaxiRank(long time);

	/**
	 * Performing login actions when a taxi rank is reached
	 *
	 * @param time simulation time in ms
	 * @return <b>true</b> if the car was successfully logged in at the rank, <b>false</b> otherwise.
	 */
	protected abstract boolean logInAtRank(long time);


	/**
	 * Performing logout actions when a taxi leaves a taxi rank
	 *
	 * @param time simulation time in ms
	 * @return <b>true</b> if the taxi was successfully logged of at the rank, <b>false</b> otherwise.
	 */
	protected abstract boolean logOutAtRank(long time);

	/**
	 * Update the vehicle's actual position to the next value stored in the route buffer.
	 * If route is finished it calls nextAction() to choose the next action.
	 */
	public void updatePosition() {
		//updating position
		GPXEntry entry = route.poll();
		setPosition(new Position(entry.lat, entry.lon), entry.getMillis());
		if (route.size() > 0) {
			placeNewEvent(new CarLocationUpdateEvent(route.peek().getMillis(), this));
		} else {
			//Route is finished get next Action
			nextAction(entry.getMillis());
		}
	}

	/**
	 * Pushes a new event to the scenario's taskList.
	 *
	 * @param e Event
	 */
	protected void placeNewEvent(Event e) {
		this.nextPlannedEvent = e;
		scenario.addEvent(e);
	}

	/**
	 * Defines the next action that should be performed according to the actual vehicle state.
	 *
	 * @param time simulation time in ms
	 */
	public abstract void nextAction(long time);


	/**
	 * Calculates a new route from Position A to B. Calculations are based on GraphHopper API.
	 *
	 * @param from start position for the route
	 * @param to   target position for the route
	 * @return GHResponse containing rout information
	 * @see GraphHopper
	 * @see MyGraphHopper
	 */
	protected GHResponse findRoute(Position from, Position to) {
		GHRequest req = new GHRequest(from.getLat(), from.getLon(), to.getLat(), to.getLon()).
				setWeighting("fastest").
				setVehicle("car");
		return hopper.route(req);
	}

	/**
	 * Sets a new Route to the route buffer. Therefore GHRespons will be converted to a List of GPX points, consisting out of lat/lon-values and arrival times.
	 * <p>
	 * A new route can only be added if, the route buffer is empty.
	 * If on wants to overwrite an exiting route the ride has to be aborted first.
	 *
	 * @param response   Routing response
	 * @param start_time time the ride starts in ms
	 */
	protected void setRoute(GHResponse response, long start_time) {
		List<GPXEntry> route = GHUtils.getGPXList(response, start_time);
		if (this.route.isEmpty()) {
			if (route != null) {
				this.route.addAll(route);
			} else {
				System.err.println("Cannot set a route which is null!");
			}
		} else {
			System.err.println("Cannot set new Route because route is not empty!");
		}
	}

	/**
	 * Try to place assignment to the car. The car can deny the job, if for instance the SOC is two low or no route can be found to the target.
	 *
	 * @param e Demand Event with customer request
	 * @return <b>true</b> if ride was assigned to the car <b>false</b> otherwise
	 */
	public boolean tryToPlaceAssignment(DemandEvent e) {
		if (isPossibleToServeCustomerDemand(e)) {
			placeAssignment(e);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Checks if Car is able to serve the customer demand. Also checks if a route from the actual position to the customer pickup_loc can be found.
	 *
	 * @return <b>true</b> if car is able, <b>false</b> otherwise
	 */
	protected abstract boolean isPossibleToServeCustomerDemand(DemandEvent e);

	/**
	 * Places the customer assignment to the car.
	 *
	 * @param e Customer request
	 */
	protected void placeAssignment(DemandEvent e) {
		setCustomerRide(e);
		if (!quitCurrentTask(e.getScheduledTime())) {
			throw new RuntimeException("Error placing Assignment previous tast could't be stopped.");
		}
		if (this.position.equals(e.getStart())) {
			startCustomerRide(e.getScheduledTime());
		} else {
			startRideToCustomer();
		}
	}


	/**
	 * Defines what to do if a Car leaves a present task, like waiting at rank or charging etc...
	 *
	 * @param time time the task should be left.
	 * @return <b>true</b> if the current task could be stopped successfully, <b>false</b> otherwise.
	 */
	abstract protected boolean quitCurrentTask(long time);

	/**
	 * Method to abort a planned ride, if a customer request occurs.
	 * LocationUpdateEvents are removed from Scenario and Route is cleaned up.
	 *
	 * @return <b>true</b> if the ride was successfully stopped, <b>false</b> otherwise.
	 */
	protected boolean abortRide(long time) {
		if (nextPlannedEvent != null) {
			scenario.removeEvent(nextPlannedEvent);
			nextPlannedEvent = null;
		}
		setPosition(position, time);
		targetFacility = 0;
		if (route.size() > 0) route.clear();
		return true;
	}

	/**
	 * Checks if the maximum time active is exceeded at time time
	 *
	 * @param time actual time
	 * @return <b>true</b> if maximum time active is exceeded, <b>false</b> otherwise
	 */
	public boolean maxTimeActiveIsExceeded(long time) {
		return (time - last_login) > max_time_active;
	}

	@Override
	public String toString() {

		return String.format("CarId: %d Model: %s Status: %s", this.id, this.type, STATUS_NAMES[status]);
	}
}
