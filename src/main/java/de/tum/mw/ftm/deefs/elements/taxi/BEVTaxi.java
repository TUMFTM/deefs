package de.tum.mw.ftm.deefs.elements.taxi;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.util.GPXEntry;
import de.tum.mw.ftm.deefs.Config;
import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingPoint;
import de.tum.mw.ftm.deefs.elements.evConcept.Battery;
import de.tum.mw.ftm.deefs.elements.evConcept.EVConcept;
import de.tum.mw.ftm.deefs.elements.facilitiies.*;
import de.tum.mw.ftm.deefs.events.CarLocationUpdateEvent;
import de.tum.mw.ftm.deefs.events.DemandEvent;
import de.tum.mw.ftm.deefs.events.FreeSpaceEvent;
import de.tum.mw.ftm.deefs.graphopper.extensions.MyGraphHopper;
import de.tum.mw.ftm.deefs.location.Position;
import de.tum.mw.ftm.deefs.log.DeniedRide;
import de.tum.mw.ftm.deefs.log.Trackpoint;

import java.util.Observable;

/**
 * Special Class of Taxi. This Class represents a electrified taxi.
 * In addition to the base class specific decision-processes are modeled here.
 *
 * @author Michael Wittmann
 */
public class BEVTaxi extends Taxi {

	//Lower limit of remaining range, a car must not undercut during its active time in m
	public final float REMANING_RANGE_MIN = Float.parseFloat(Config.getProperty(Config.BEVTAXI_REMAINING_RANGE_MIN, "15000"));
	//Limit defines, when a car starts to look for a charging station in m
	public final float REMANING_RANGE_RECHARGE = Float.parseFloat(Config.getProperty(Config.BEVTAXI_REMAINING_RANGE_RECHARGE, "30000"));
	//limit defines, from when on a taxi is able to stop a charging process beacause of a customer request in %
	public final float SOC_STOP_CHARGE_MIN = Float.parseFloat(Config.getProperty(Config.BEVTAXI_SOC_MIN_STOP_CHARGE, "70.0"));
	//limit defines, at which soc the charging process should be stopped in %.
	public final float SOC_STOP_CHARGE_MAX = Float.parseFloat(Config.getProperty(Config.BEVTAXI_SOC_MAX_STOP_CHARGE, "100.0"));
	//limit defining the minimum time a car must be connected to a charging station before quitting charging process in ms
	public final long MIN_TIME_CHARGING = Long.parseLong(Config.getProperty(Config.BEVTAXI_MIN_TIME_CHARGING, "1200000"));
	//limit defines the maximum distance a car takes extra way to reach a charging station with a faster connector in m
	public final float MAX_DISTANCE_BEST_CONNECTOR = Float.parseFloat(Config.getProperty(Config.BEVTAXI_MAX_DISTANCE_BEST_CONNECTOR, "4000"));

	private final EVConcept concept;

	/**
	 * New instance of BEV Taxi
	 *
	 * @param id       unique car id
	 * @param home     car's home position
	 * @param hopper   GraphHopper instance needed for route calculations
	 * @param scenario scenario the car operates in
	 * @param concept  evConcept
	 */
	protected BEVTaxi(int id, Position home, MyGraphHopper hopper,
					  Scenario scenario, EVConcept concept) {
		super(id, home, hopper, scenario);
		this.concept = concept;
		this.type = "BEV_TAXI";
		setPosition(home, 0);
	}

	@Override
	public boolean isFree() {
		return !isBusy() && this.status != STATUS_LOGGED_OFF;
	}

	@Override
	public boolean isBusy() {
		if (this.status == STATUS_OCCUPIED) return true;
		if (this.status == STATUS_ON_WAY_TO_CUSTOMER) return true;
		if (this.status == STATUS_ON_WAY_BACK_HOME) return true;
		if (this.status == STATUS_ON_WAY_TO_CHARGING_POINT) return true;
		return this.status == STATUS_WAIT_FOR_CHARGING;
	}


	@Override
	protected void startRideToNextRank(long start_time) {
		GHResponse route = findRouteToNextTaxiRank(start_time);
		if (canDrive(route.getDistance())) {
			setTrackId(getNewTrackId());
			setStatus(STATUS_ON_WAY_TO_RANK);
			setRoute(route, start_time);
			updatePosition();
		} else {
			startRideToNextChargingPoint(start_time);
		}
	}

	@Override
	protected GHResponse findRouteToNextTaxiRank(long time) {
		TaxiRank nextFreeRank = findNextRank(time);
		GHResponse wayToRank = findRoute(position, nextFreeRank.getPosition());
		setTargetFacility(nextFreeRank.getId());
		return wayToRank;
	}


	@Override
	public boolean logOn(long logOnTime) {
		if (this.status == STATUS_LOGGED_OFF) {        //check if vehicle was logged off before
			if ((logOnTime - last_logoff) > min_time_inactive || this.last_logoff == -1) {    //check if min_time_inactive is satisfied
				logOffTriggered = false;
				if (last_logoff != -1) {
					//update the SOC taking, the logged of time an charge with SchuKo connector
					ChargingPoint.chargeCarAtHome((logOnTime - last_logoff), this);
				}
				last_login = logOnTime;
				shift_counter.incrementAndGet();
				startRideToRank(scenario.getFacilities().getRandomRank(position, logOnTime), logOnTime);
				return true;
			}
		} else {
			System.err.printf("Cannot log on Car: %d because it is already logged on!%n", this.id);
		}
		return false;
	}


	/**
	 * Starts new ride to the next charging point. Charging point is selected according to rules defined in findNextChargingPoint().
	 *
	 * @param start_time simulation time in ms
	 */
	protected void startRideToNextChargingPoint(long start_time) {
		startRideToNextChargingPoint(findNextChargingPoint(), start_time);
	}

	/**
	 * Starts new ride to a certain charging point.
	 *
	 * @param start_time simulation time in ms
	 * @param cp         selected ChargingPoint
	 */
	protected void startRideToNextChargingPoint(ChargingPossibility cp, long start_time) {
		setTargetFacility(((Facility) cp).getId());
		setTrackId(getNewTrackId());
		setStatus(STATUS_ON_WAY_TO_CHARGING_POINT);
		setRoute(findRoute(position, ((Facility) cp).getPosition()), start_time);
		updatePosition();
	}

	/**
	 * Looking for the next available ChargingPoint. Selection Process is devided into 4 Steps:
	 * <p> 1. If remaining range is enough to search for the best connector in the specified Radius take next free one with the higest power
	 * <p> 2. If remaining range is smaller than the search Radius for the best Charging Possibility look for the next free charging position in range
	 * <p> 3. If no <b>free</b> charging station was found in range look for the best remaining one in range
	 * <p> 4. If still no charging point was found (may after a customer ride because of small deviations between calculated distance and real distance) search with remaining brutto range
	 *
	 * @return Selected charging point or <b>null</b> if no charging point was found in remaining range
	 */
	protected ChargingPossibility findNextChargingPoint() {
		ChargingPossibility nextCP = null;
		float remaining_range = getRemainingRangeNetto();
		//if remaining range is enough to search for the best connector in the specified Radius do it... 
		if (((remaining_range - MAX_DISTANCE_BEST_CONNECTOR) > 0) && (getTargetFacility() == 0)) {
			nextCP = findBestChargingPossibility(getPosition());
		}
		// if remaining range is smaller than the search Radius for the best Charging Possibility look
		// for the next free charging position in Range
		if (nextCP == null) {
			nextCP = findFreeChargingPossibilityInRange(position, remaining_range);
		}
		//if no free charging station was found in range look for the best remaining one in range
		if (nextCP == null) {
			nextCP = findClosestChargingPossibilityInRangeExact(getPosition(), remaining_range);
		}
		//if still no charging point was found(may after a customer ride because of small deviations between calculated distance and real distance)
		//search with brutto range
		if (nextCP == null) {
			nextCP = findClosestChargingPossibilityInRangeExact(getPosition(), getRemainingRangeBrutto());
		}
		return nextCP;
	}


	/**
	 * Looks for the next free charging station in a defined range.
	 * Search algorithm is implemented in {@link FacilityList#findFreeChargingPossibilityInRange(ChargingInterface, Position, float, MyGraphHopper, int)}
	 *
	 * @param position actual vehicle position
	 * @param range    search range
	 * @return selected charging station or <b>null</b> if no charging station was found
	 * @see FacilityList
	 */
	protected ChargingPossibility findFreeChargingPossibilityInRange(Position position, float range) {
		return scenario.getFacilities().findFreeChargingPossibilityInRange(getChargingInterface(), position, range, hopper, getTargetFacility());
	}

	/**
	 * Looks for the closest charging station in a defined range
	 * Search algorithm is implemented in {@link FacilityList#findClosestChargingPossibilityInRange(ChargingInterface, Position, float, MyGraphHopper, int)}
	 *
	 * @param position actual vehicle position
	 * @param range    search range
	 * @return selected charging station or <b>null</b> if no charging station was found
	 * @see FacilityList
	 */
	protected ChargingPossibility findClosestChargingPossibilityInRangeExact(Position position, float range) {
		return scenario.getFacilities().findClosestChargingPossibilityInRange(getChargingInterface(), position, range, hopper, getTargetFacility());
	}

	/**
	 * Looks for the closest charging station without queue
	 * Search algorithm is implemented in {@link FacilityList#findClosestChargingPossibilityWithoutQueue(ChargingInterface, Position, int)}
	 *
	 * @param position actual vehicle position
	 * @return selected charging station or <b>null</b> if no charging station was found
	 * @see FacilityList
	 */
	protected ChargingPossibility findClosestChargingPossibilityWithoutQueueCoarse(Position position) {
		return scenario.getFacilities().findClosestChargingPossibilityWithoutQueue(getChargingInterface(), position, getTargetFacility());
	}

	/**
	 * Looks for the next free Charging position in a defined range (coarse calculation)
	 * Search algorithm is implemented in {@link FacilityList#findClosestChargingFreePossibility(ChargingInterface, Position, int)}
	 *
	 * @param position actual vehicle position
	 * @return selected charging station or <b>null</b> if no charging station was found
	 * @see FacilityList
	 */
	protected ChargingPossibility findClosestChargingFreePossibilityCoarse(Position position) {
		return scenario.getFacilities().findClosestChargingFreePossibility(getChargingInterface(), position, getTargetFacility());
	}

	/**
	 * Looks for the best charging station within the defined search radius.
	 * Search algorithm is implemented in {@link FacilityList#findBestChargingPossibilityInRange(ChargingInterface, Position, float, int)}
	 *
	 * @param position actual vehicle position
	 * @return selected charging station or <b>null</b> if no charging station was found
	 * @see FacilityList
	 */
	protected ChargingPossibility findBestChargingPossibility(Position position) {
		return scenario.getFacilities().findBestChargingPossibilityInRange(getChargingInterface(), position, MAX_DISTANCE_BEST_CONNECTOR, getTargetFacility());
	}


	/**
	 * This method manages the login procedure, when reaching a charging point.
	 * If the charing station denies the login, the agent will try to reach an other charging station. In case the remaining range is not high enough, to
	 * reach an other station, the car will check into the queue and will wait for a free space.
	 *
	 * @param time simulation time in ms
	 * @see ChargingStation#checkInCar(Taxi, long)
	 * @see ChargingStation#loginToQueue(BEVTaxi, long)
	 */
	private void logInAtChargingPoint(long time) {
		ChargingPossibility chargingPossibility = scenario.getFacilities().getChargingPossibility(getTargetFacility());

		if (chargingPossibility instanceof ChargingStation) {
			ChargingStation chargingStation = (ChargingStation) chargingPossibility;
			setPosition(chargingStation.getPosition(), time);
			if (chargingStation.checkInCar(this, time)) {
				setTrackId(getNewTrackId());
				setConnectedToFacility(chargingStation);
				setStatus(STATUS_AT_CHARGING_POINT);
				setPosition(chargingStation.getPosition(), time);
			} else {
				//if the charging possibility denies the login look for an other free charging station
				ChargingPossibility cp = findFreeChargingPossibilityInRange(position, getRemainingRangeNetto());
				if (cp != null) {
					//if one found startRideToNextChargingPossibility
					startRideToNextChargingPoint(cp, time);
				} else {
					//if no other reachable chargingPossibility was found login at queue
					setTrackId(getNewTrackId());
					chargingStation.loginToQueue(this, time);
					setConnectedToFacility(chargingStation);
					setTargetFacility(chargingStation.getId());
					setStatus(STATUS_WAIT_FOR_CHARGING);
					setPosition(chargingStation.getPosition(), time);
				}
			}
		} else {
			throw new RuntimeException("No valid charging point");
		}
	}


	/**
	 * This method manages the logout procedure when leaving a charging station.
	 *
	 * @param time simulation time in ms
	 * @return <b>true</b> if the car was successfully logged out, <b>false</b> otherwise
	 */
	private boolean logOutAtChargingPoint(long time) {
		if (connectedToFacility() instanceof ChargingStation) {
			if (connectedToFacility().checkOutCar(this, time)) {
				setConnectedToFacility(null);
				return true;
			}
		}
		return false;
	}

	/**
	 * Agent will stop waiting for the charging process.
	 *
	 * @param time simulation time in ms
	 * @return <b>true</b> if the waiting process was aborted successfully, <b>false</b> otherwise
	 * @see ChargingStation#abortWaiting(BEVTaxi, long)
	 */
	private boolean abortWaitingForChargingStation(long time) {
		if (connectedToFacility() instanceof ChargingStation) {
			if (((ChargingStation) connectedToFacility()).abortWaiting(this, time)) {
				setConnectedToFacility(null);
				return true;
			}
		}
		return false;
	}


	@Override
	protected boolean logInAtRank(long time) {
		TaxiRank rank = scenario.getFacilities().getRank(getTargetFacility());
		setPosition(rank.getPosition(), time);
		if (rank.checkInCar(this, time)) {
			setTrackId(getNewTrackId());
			setStatus(STATUS_AT_RANK);
			setConnectedToFacility(rank);
			setPosition(rank.getPosition(), time);
			return true;
		} else {
			startRideToNextRank(time);
			return false;
		}

	}

	@Override
	protected boolean logOutAtRank(long time) {
		if (connectedToFacility().checkOutCar(this, time)) {
			setPosition(connectedToFacility().getPosition(), time);
			setConnectedToFacility(null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void startRideHome(long logOffTime) {
		if (canDriveHome()) {
			setTrackId(getNewTrackId());
			setStatus(STATUS_ON_WAY_BACK_HOME);
			setRoute(findRoute(position, home), logOffTime);
			updatePosition();
		} else {
			startRideToNextChargingPoint(logOffTime);
		}
	}

	/**
	 * Checks if the remaining range is enough to reach the home destination.
	 *
	 * @return <b>true</b> if the remaining range is high enough to reach home,<b>false</b> otherwise
	 */
	private boolean canDriveHome() {
		if (position.calcDist(home) < getRemainingRangeBrutto()) {

			GHResponse routeHome = findRoute(position, home);
			return routeHome.getDistance() < getRemainingRangeBrutto();
		}
		return false;
	}

	@Override
	public void nextAction(long time) {

		scenario.getDBLog().flush();
		switch (this.status) {
			case STATUS_ON_WAY_TO_CUSTOMER:
				setPosition(customer_ride.getStart(), time);
				startCustomerRide(time);
				break;
			case STATUS_OCCUPIED:
				customer_ride = null;
				if (maxTimeActiveIsExceeded(time)) {
					setStatus(STATUS_ON_WAY_TO_RANK);
					triggerlogOff(time);
				} else if (shouldRecharge()) {
					startRideToNextChargingPoint(time);
				} else {
					startRideToNextRank(time);
				}
				break;
			case STATUS_ON_WAY_TO_RANK:
				if (maxTimeActiveIsExceeded(time)) {
					triggerlogOff(time);
				} else {
					logInAtRank(time);
				}
				break;
			case STATUS_ON_WAY_TO_CHARGING_POINT:
				logInAtChargingPoint(time);
				break;
			case STATUS_AT_CHARGING_POINT:
				if (maxTimeActiveIsExceeded(time)) {
					triggerlogOff(time);
				} else {
					logOutAtChargingPoint(time);
					startRideToNextRank(time);
				}
				break;
			case STATUS_ON_WAY_BACK_HOME:
				logOff(time);
				break;
			default:
				System.err.println("Invalid state at route end! Car:" + id);
				break;
		}

	}


	@Override
	public void updatePosition() {
		GPXEntry entry = route.poll();
		setPosition(new Position(entry.lat, entry.lon), entry.getMillis());
		if (status == STATUS_ON_WAY_TO_RANK) {
			// check if vehicle should look for a charging station nearby
			if (shouldRecharge()) {
				abortRide(entry.getMillis());
				setTrackId(getNewTrackId());
				setStatus(STATUS_ON_WAY_TO_CHARGING_POINT);
				ChargingPossibility nextCP = findNextChargingPoint();
				if (nextCP == null) {
					scenario.getDBLog().flush();
				}
				setTargetFacility(((Facility) nextCP).getId());
				setRoute(hopper.route(new GHRequest(position.getLat(), position.getLon(), ((Facility) nextCP).getPosition().getLat(), ((Facility) nextCP).getPosition().getLon())), entry.getMillis());
			}
		}
		if (route.size() > 0) {
			placeNewEvent(new CarLocationUpdateEvent(route.peek().getMillis(), this));
		} else {
			//Route is finished get next Action
			nextAction(entry.getMillis());
		}
	}

	/* (non-Javadoc)
	 * @see taxiBehaviorModel.elements.taxi.Taxi#setPosition(taxiBehaviorModel.location.Position, long)
	 * In case of an electric car SOC updates are done in this step too.
	 */
	@Override
	protected void setPosition(Position position, long time) {
		float distance;
		//getting distance to last position. If it is a customer Ride take the original distance
		if (status == STATUS_OCCUPIED && route.size() == 0) {
			distance = (float) customer_ride.getDistance();
		} else {
			distance = (float) this.position.calcDist(position);
		}
		super.setPosition(position);
		if (distance > 0) {
			updateSOC(distance);
		}
		logTrackpoint(new Trackpoint(this.id, getShiftCounter(), track_id, time, getStatusAsText(), position, distance, concept.getBattery().getSoc(), connectedToFacilityID()));
	}


	/**
	 * Updates the vehicles SOC, calculating the needed energy by the driven distance
	 *
	 * @param distance driven distance in m
	 * @see EVConcept#getNeededEnergy(float)
	 */
	private void updateSOC(float distance) {
		concept.getBattery().discharge(concept.getNeededEnergy(distance));
	}


	@Override
	protected boolean isPossibleToServeCustomerDemand(DemandEvent e) {

		if (isFree() && !maxTimeActiveIsExceeded(e.getScheduledTime())) {
			//Check if car is at charging point and if the car may terminate the charging process
			if (status == STATUS_AT_CHARGING_POINT) {
				if (!((ChargingPossibility) connectedToFacility()).mayTerminateCharging(this, e.getScheduledTime()) || getSOC() < SOC_STOP_CHARGE_MIN) {
					scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), -1, DeniedRide.REASON_CHARGING));
					return false;
				}
			}
			if (getRemainingRangeNetto() == 0) {
				scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), -1, DeniedRide.REASON_SOC_TOO_LOW));
				return false;
			}

			//check if taxi can drive track distance
			double distance = e.getDistance();
			if (!canDrive(distance)) {
				scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), -1, DeniedRide.REASON_SOC_TOO_LOW));
				return false;
			}

			//check if the customer ride is routeable
			if (!findRoute(e.getStart(), e.getTarget()).hasErrors()) {
				//check if soc is high enough for way to customer and customer track
				GHResponse wayToCustomer = findRoute(position, e.getStart());
				if (!wayToCustomer.hasErrors()) {
					distance += wayToCustomer.getDistance();
					if (!canDrive(distance)) {
						scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), wayToCustomer.getDistance(), DeniedRide.REASON_SOC_TOO_LOW));
						return false;
					}

					Facility nextCp = (Facility) findClosestChargingFreePossibilityCoarse(e.getTarget());
					if (nextCp != null) {
						GHResponse wayToCP = findRoute(e.getTarget(), nextCp.getPosition());
						if (!wayToCP.hasErrors()) {
							distance += wayToCP.getDistance();
							if (canDrive(distance)) {
								return true;
							} else {
								scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), wayToCustomer.getDistance(), DeniedRide.REASON_NO_REACHABLE_CHARGING_STATION_FOUND));
								return false;
							}
						} else {
							scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), wayToCustomer.getDistance(), DeniedRide.REASON_NO_ROUTE_FOUND));
							return false;
						}
					} else {
						scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), wayToCustomer.getDistance(), DeniedRide.REASON_NO_REACHABLE_CHARGING_STATION_FOUND));
						return false;
					}
				} else {
					scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), wayToCustomer.getDistance(), DeniedRide.REASON_NO_ROUTE_FOUND));
					return false;
				}
			} else {
				scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), -1, DeniedRide.REASON_NO_ROUTE_FOUND));
				return false;
			}
		}
		scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), -1, DeniedRide.REASON_BUSY));
		return false;
	}

	/**
	 * Checks if the remaining SOC is high enough to drive the given distance
	 *
	 * @param distance distance to drive in m
	 * @return <b>true </b> if car is able to drive the given distance, <b>false</b> otherwise
	 */
	private boolean canDrive(double distance) {
		return (getRemainingRangeNetto() - distance) > 0;
	}

	/**
	 * Calculates the remaining netto range of the car based on current charge and VMean<br>
	 * remainingRagne = E_Bat/VMean - REMANING_RANGE_MIN;
	 *
	 * @return the remaining netto range in m
	 */
	private float getRemainingRangeNetto() {
		float remainingRange = (concept.getBattery().getE_bat() / concept.getVMean()) - REMANING_RANGE_MIN;
		return remainingRange > 0 ? remainingRange : 0;

	}

	/**
	 * Calculates the remaining range of the car based on current charge and VMean<br>
	 * remainingRagne = E_Bat/VMean ;
	 *
	 * @return the remaining brutto range in m
	 */
	private float getRemainingRangeBrutto() {
		return (concept.getBattery().getE_bat() / concept.getVMean());
	}

	/**
	 * Checks if the actual SOC is below the defined recharge-trigger-value
	 *
	 * @return <b>true</b> if SOC is lower than REMAINING_RANGE_RECHARGE, <b>false</b> otherwise.
	 */
	public boolean shouldRecharge() {
		return (concept.getBattery().getE_bat() / concept.getVMean()) < REMANING_RANGE_RECHARGE;
	}


	/**
	 * Returns the actual SOC.
	 *
	 * @return the actual SOC in %
	 * @see Battery#getSoc()
	 */
	public float getSOC() {
		return concept.getBattery().getSoc();
	}

	/**
	 * Returns the vehicle's ChargingInterface
	 *
	 * @return the vehicles ChargingInterface
	 */
	public ChargingInterface getChargingInterface() {
		return concept.getChargingInterface();
	}


	/**
	 * Returns the vehcle's EVConept
	 *
	 * @return the vehcle's EVConept
	 */
	public EVConcept getConcept() {
		return concept;
	}

	/**
	 * Charges the battery with the given amount of energy. Creates also a log entry.
	 *
	 * @param time   simulation time in ms
	 * @param energy energy to be charged in J
	 * @see Battery#charge(float)
	 */
	public void chargeCar(long time, float energy) {
		concept.getBattery().charge(energy);
		if (!logOffTriggered && maxTimeActiveIsExceeded(time)) {
			triggerlogOff(time);
		}

		logTrackpoint(new Trackpoint(this.id, getShiftCounter(), track_id, time, getStatusAsText(), position, 0, concept.getBattery().getSoc(), connectedToFacilityID()));
	}


	@Override
	protected boolean quitCurrentTask(long time) {
		switch (this.status) {
			case STATUS_AT_RANK:
				return logOutAtRank(time);
			case STATUS_ON_WAY_TO_RANK:
				return abortRide(time);
			case STATUS_AT_CHARGING_POINT:
				return logOutAtChargingPoint(time);
			case STATUS_WAIT_FOR_CHARGING:
				return abortWaitingForChargingStation(time);
			default:
				return false;
		}
	}

	@Override
	public boolean triggerlogOff(long logOffTime) {
		if ((logOffTime - last_login) > min_time_active) {
			switch (this.status) {
				case STATUS_AT_RANK:
					logOffTriggered = true;
					if (quitCurrentTask(logOffTime)) {
						startRideHome(logOffTime);
						return true;
					}
					logOffTriggered = false;
					break;
				case STATUS_ON_WAY_TO_RANK:
					if (quitCurrentTask(logOffTime)) {
						startRideHome(logOffTime);
						logOffTriggered = true;
						return true;
					}
					logOffTriggered = false;
					break;
				case STATUS_AT_CHARGING_POINT:
					logOffTriggered = true;
					if (canDriveHome()) {
						if (quitCurrentTask(logOffTime)) {
							startRideHome(logOffTime);
							return true;
						}
					}
					logOffTriggered = false;
					break;
				case STATUS_WAIT_FOR_CHARGING:
					logOffTriggered = true;
					if (canDriveHome()) {
						if (quitCurrentTask(logOffTime)) {
							startRideHome(logOffTime);
							logOffTriggered = true;
							return true;
						}
					}
					logOffTriggered = false;
					break;
				default:
					return false;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 * Waiting car gets notified by ChargingStation if there is a free space. Login procedure is started when notified.
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof ChargingStation) {
			if (arg instanceof FreeSpaceEvent) {
				logInAtChargingPoint(((FreeSpaceEvent) arg).getScheduledTime());
			}
		}

	}

	@Override
	public String toString() {
		return String.format("CarId: %d Model: %s Status: %s SOC: %.2f", this.id, this.type, STATUS_NAMES[status], getSOC());
	}
}
