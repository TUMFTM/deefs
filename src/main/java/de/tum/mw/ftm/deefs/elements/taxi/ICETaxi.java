package de.tum.mw.ftm.deefs.elements.taxi;

import com.graphhopper.GHResponse;
import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.comparators.chains.NextRankComparatorChain;
import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;
import de.tum.mw.ftm.deefs.events.DemandEvent;
import de.tum.mw.ftm.deefs.graphopper.extensions.MyGraphHopper;
import de.tum.mw.ftm.deefs.location.Position;
import de.tum.mw.ftm.deefs.log.DeniedRide;

import java.util.Observable;


/**
 * Special Class of Taxi. This Class represents a conventional taxi with internal combustion engine.
 * In addition to the base class specific decision-processes are modeled here.
 *
 * @author Michael Wittmann
 */
public class ICETaxi extends Taxi {


	/**
	 * New instance of a taxi with internal combustion engine
	 *
	 * @param id       unique car id
	 * @param home     car's home position
	 * @param hopper   instance of GraphHopper needed for routing
	 * @param scenario scenario the car operates in
	 */
	protected ICETaxi(int id, Position home, MyGraphHopper hopper, Scenario scenario) {
		super(id, home, hopper, scenario);
		this.type = "ICE_TAXI";
		setPosition(home, 0);
	}


	/**
	 * Indicates weather the car is able to fulfill customer requests
	 *
	 * @return <b> true</b> if the car is in state:
	 * <p> STATUS_AT_RANK or STATUS_ON_WAY_TO_RANK,
	 * <p> <b>false</b> otherwise.
	 */
	@Override
	public boolean isFree() {
		return this.status == STATUS_AT_RANK || this.status == STATUS_ON_WAY_TO_RANK;
	}

	/**
	 * Indicates whether the car is <b>not</b> able to fulfill customer requests
	 *
	 * @return <b> true</b> if the car is in state:
	 * <p> STATUS_OCCUPIED or STATUS_ON_WAY_TO_CUSTOMER or STATUS_ON_WAY_BACK_HOME,
	 * <p> <b>false</b> otherwise.
	 */
	@Override
	public boolean isBusy() {
		return this.status == STATUS_OCCUPIED || this.status == STATUS_ON_WAY_TO_CUSTOMER ||
				this.status == STATUS_ON_WAY_BACK_HOME;
	}

	/**
	 * Calculates the route to the next taxi rank. Selection is made according to defined rules in {@link NextRankComparatorChain}.
	 *
	 * @param time simulation time in ms
	 * @return GHResponse containing route information
	 */
	@Override
	protected GHResponse findRouteToNextTaxiRank(long time) {
		TaxiRank nextFreeRank = findNextRank(time);
		setTargetFacility(nextFreeRank.getId());
		return findRoute(position, nextFreeRank.getPosition());
	}

	/**
	 * Performing login actions when connecting to a taxi rank
	 *
	 * @param time simulation time in ms
	 * @return <b> true </b> if the car was successfully connected, <b> false </b> otherwise.
	 */
	@Override
	protected boolean logInAtRank(long time) {
		TaxiRank rank = scenario.getFacilities().getRank(getTargetFacility());
		setPosition(rank.getPosition(), time);
		if (rank.checkInCar(this, time)) {
			setTrackId(getNewTrackId());
			setConnectedToFacility(rank);
			setStatus(STATUS_AT_RANK);
			setPosition(rank.getPosition(), time);
			return true;
		} else {
			startRideToNextRank(time);
			return false;
		}
	}

	/**
	 * Performing log out actions when disconnecting from a taxi rank
	 *
	 * @param time simulation time in ms
	 * @return <b> true </b> if the car was successfully disconnected, <b> false </b> otherwise.
	 */
	@Override
	protected boolean logOutAtRank(long time) {
		if (connectedToFacility().checkOutCar(this, time)) {
			setPosition(connectedToFacility().getPosition(), time);
			setConnectedToFacility(null);
			return true;
		}
		return false;

	}

	/**
	 * Chooses, depending on the actual state of the vehicle, the next action that should be performed.
	 *
	 * @param time simulation time in ms
	 */
	@Override
	public void nextAction(long time) {
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
			case STATUS_ON_WAY_BACK_HOME:
				logOff(time);
				break;
			default:
				System.err.println("Invalid state at route end! Car:" + id);
				break;
		}

	}

	/**
	 * Checks if Car is able to serve the customer demand. Also checks if a route from the actual position to the customer pickup_loc can be found.
	 *
	 * @return <b>true</b> if car is able, <b>false</b> otherwise
	 */
	@Override
	protected boolean isPossibleToServeCustomerDemand(DemandEvent e) {
		if (isFree()) {
			if (!findRoute(e.getStart(), e.getTarget()).hasErrors()) {
				return true;
			} else {
				scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), -1, DeniedRide.REASON_NO_ROUTE_FOUND));
				return false;
			}
		} else {
			scenario.getDBLog().addDeniedRide(new DeniedRide(this.id, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), -1, DeniedRide.REASON_BUSY));
			return false;
		}
	}


	@Override
	protected boolean quitCurrentTask(long time) {
		switch (this.status) {
			case STATUS_AT_RANK:
				return logOutAtRank(time);
			case STATUS_ON_WAY_TO_RANK:
				return abortRide(time);
			default:
				return false;
		}

	}


	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}


}
