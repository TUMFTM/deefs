package de.tum.mw.ftm.deefs.elements;

import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.comparators.chains.NextCarComparatorChain;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;
import de.tum.mw.ftm.deefs.events.DemandEvent;
import de.tum.mw.ftm.deefs.log.DeniedRide;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;


/**
 * The TaxiAgency is responsible for holding the vehicle fleet and to assign customer requests to a car.
 *
 * @author Michael Wittmann
 */
public class TaxiAgency {

	private final List<Taxi> fleet;
	private final Scenario scenario;

	public TaxiAgency(Scenario scenario) {
		fleet = new ArrayList<>();
		this.scenario = scenario;
	}


	/**
	 * Alternative Constructor. A preallocation for fleetList will be done. Recommended for big fleet sizes.
	 *
	 * @param fleetSize initial fleet size
	 * @param scenario
	 */
	public TaxiAgency(Scenario scenario, int fleetSize) {
		this.scenario = scenario;
		fleet = new ArrayList<>(fleetSize);
	}

	/**
	 * @return a List of all cars with status: STAUS_FREE or STATUS_AT_RANK
	 */
	public List<Taxi> getFree_taxis() {
		return fleet.stream().filter(Taxi::isFree).collect(Collectors.toList());
	}

	/**
	 * @return a List of all cars with status: STATUS_OCCUPIED or STATUS_ON_WAY_TO_CUSTOMER
	 */
	public List<Taxi> getBusy_taxis() {
		return fleet.stream().filter(Taxi::isBusy).collect(Collectors.toList());
	}


	/**
	 * @return a List of all cars with status: STATUS_LOGGED_OFF
	 */
	public List<Taxi> getInactive_taxis() {
		return fleet.stream().filter(Taxi::isLoggedOff).collect(Collectors.toList());
	}

	/**
	 * @return a List of all cars they are not logged off.
	 */
	public List<Taxi> getActive_taxis() {
		return fleet.stream().filter(c -> !c.isLoggedOff() && !c.isOnWayBackHome()).collect(Collectors.toList());
	}

	/**
	 * @return the actual fleet
	 */
	public List<Taxi> getFleet() {
		return fleet;
	}

	/**
	 * Try to find a car which can serve the customer demand. If a car was found the job will be assigned to it.
	 *
	 * @param e DemandEvent
	 * @return <b>true</b> if the request was assigned to a car successfully. <b>false</b> otherwise.
	 */
	public boolean tryToPlaceCustomerRequest(DemandEvent e) {

		@SuppressWarnings("unchecked")
		PriorityQueue<Taxi> cars = new PriorityQueue<Taxi>(new NextCarComparatorChain(e.getStart()));
		cars.addAll(getFree_taxis());
		while (!cars.isEmpty()) {
			Taxi car = cars.poll();
			if (car.tryToPlaceAssignment(e)) return true;
		}
		scenario.getDBLog().addDeniedRide(new DeniedRide(0, e.getTrack_id(), e.getScheduledTime(), e.getDistance(), -1, DeniedRide.REASON_NO_FREE_CAR));
		return false;
	}

	/**
	 * Adding a car to the vehicle fleet
	 *
	 * @param car instance of car to be added
	 */
	public void addCar(Taxi car) {
		fleet.add(car);
	}

}
