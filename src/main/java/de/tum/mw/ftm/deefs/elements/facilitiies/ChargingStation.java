package de.tum.mw.ftm.deefs.elements.facilitiies;

import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.comparators.BestChargingPointComparator;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingPoint;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.Connector;
import de.tum.mw.ftm.deefs.elements.taxi.BEVTaxi;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;
import de.tum.mw.ftm.deefs.events.FreeSpaceEvent;
import de.tum.mw.ftm.deefs.location.Position;
import de.tum.mw.ftm.deefs.log.FacilityStats;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a charging Station in the behavior model.
 * A Charging station is a collection of one or more charging points an has a physical location
 * To create a instance of ChargingPoint use {@link FacilityFactory}.
 * Every charging station consists of at least one charging point.
 *
 * @author Michael Wittmann
 * @see FacilityFactory
 * @see Facility
 * @see ChargingPoint
 */
public class ChargingStation extends Facility implements ChargingPossibility {

	//Charging action identifiers
	public static String ACTION_CHECKIN_TO_QUEUE = "CHECK_IN_TO_QUEUE";
	public static String ACTION_ABORT_WAITING = "ABORT_WAITING";
	private final List<ChargingPoint> chargingPoints;            //list with available charging points
	private final LinkedList<BEVTaxi> queue;                    //waiting vehicle queue
	private final Map<BEVTaxi, ChargingPoint> chargingCars;    //list with all charging cars


	/**
	 * New instance of ChargingStation. Notice, there must be at minimum one chariging point at each ChargingStation
	 *
	 * @param id
	 * @param position
	 * @param chargingPoints
	 * @param scenario
	 */
	public ChargingStation(int id, Position position, List<ChargingPoint> chargingPoints, Scenario scenario) {
		super(id, scenario, position, chargingPoints.size());
		if (!chargingPoints.isEmpty()) {
			this.chargingPoints = chargingPoints;
			for (ChargingPoint chargingPoint : chargingPoints) {
				chargingPoint.setParentFacility(this.getId());
			}
			chargingCars = new HashMap<>(chargingPoints.size());
			queue = new LinkedList<>();
		} else {
			throw new RuntimeException("Error while creating an instance of ChargingStation: there must be at least one charging point!");
		}
	}

	@Override
	public boolean hasSpace() {
		return chargingCars.size() < this.getCapacity();
	}

	@Override
	public int getRemainingSpace() {
		return chargingCars.size() - chargingPoints.size();
	}

	@Override
	public boolean loginToQueue(BEVTaxi car, long time) {
		scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_CHECKIN_TO_QUEUE, chargingCars.size(), (queue.size() + 1)));
		return queue.add(car);
	}

	@Override
	public boolean abortWaiting(BEVTaxi car, long time) {
		scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_ABORT_WAITING, chargingCars.size(), (queue.size() - 1)));
		return queue.remove(car);
	}

	@Override
	public boolean checkInCar(Taxi car, long time) {
		if (car instanceof BEVTaxi) {
			//check if there are empty compatible spots
			List<ChargingPoint> cps = chargingPoints.stream().filter(c -> c.isAvailable() && c.isCompatibleTo(((BEVTaxi) car).getChargingInterface())).collect(Collectors.toList());
			if (cps.isEmpty()) {
				scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_CHECKIN_DENIED, chargingCars.size(), queue.size()));
				return false;
			}
			//choose best pssoible connector
			ChargingPoint cp = Collections.max(cps, new BestChargingPointComparator(((BEVTaxi) car).getChargingInterface()));
			if (cp.connect((BEVTaxi) car, time)) {
				chargingCars.put((BEVTaxi) car, cp);
				scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_CHECKIN, chargingCars.size(), queue.size()));
				return true;
			}
		} else {
			System.err.println("Car must be type of BEV to connect to a charging station");
		}
		scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_CHECKIN_DENIED, chargingCars.size(), queue.size()));
		return false;
	}

	@Override
	public boolean checkOutCar(Taxi car, long time) {
		if (chargingCars.get(car).disconnect(time)) {
			chargingCars.remove(car);
			scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_CHECKOUT, chargingCars.size(), queue.size()));
			if (!queue.isEmpty()) {
				//Notify the the car waiting longest
				BEVTaxi taxi = queue.poll();
				this.addObserver(taxi);
				setChanged();
				notifyObservers(new FreeSpaceEvent(time));
				this.deleteObserver(taxi);
			}

			return true;
		}
		return false;
	}

	@Override
	public int numberOfAvailableCP() {
		return getRemainingSpace();
	}

	@Override
	public boolean hasFreeChargingPoints(ChargingInterface ci) {
		List<ChargingPoint> cps = chargingPoints.stream().filter(c -> c.isAvailable() && c.isCompatibleTo(ci)).collect(Collectors.toList());
		return !cps.isEmpty();
	}

	@Override
	public Connector bestConnector(ChargingInterface ci) {
		List<ChargingPoint> cps = chargingPoints.stream().filter(c -> c.isAvailable() && c.isCompatibleTo(ci)).collect(Collectors.toList());
		if (cps.isEmpty()) {
			return null;
		} else {
			return Collections.max(cps, new BestChargingPointComparator(ci)).getBestConnector(ci);
		}
	}

	@Override
	public boolean isCompatible(ChargingInterface ci) {
		List<ChargingPoint> cps = chargingPoints.stream().filter(c -> c.isCompatibleTo(ci)).collect(Collectors.toList());
		return !cps.isEmpty();
	}

	@Override
	public boolean mayTerminateCharging(BEVTaxi car, long time) {
		if (chargingCars.containsKey(car)) {
			return chargingCars.get(car).mayDisconnect(time);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("ID:%d CP%d(%d)", getId(), getRemainingSpace(), getCapacity());
	}

	@Override
	public int getQueueSize() {
		return queue.size();
	}
}
