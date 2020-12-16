package de.tum.mw.ftm.deefs.elements.facilitiies;

import com.graphhopper.GHRequest;
import de.tum.mw.ftm.deefs.comparators.ChargingPossibilityFastestComparator;
import de.tum.mw.ftm.deefs.comparators.chains.CoarseNextChargingPossibilityComparatorChain;
import de.tum.mw.ftm.deefs.comparators.chains.NextChargingPossibilityComparatorChain;
import de.tum.mw.ftm.deefs.comparators.chains.NextRankComparatorChain;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.Connector;
import de.tum.mw.ftm.deefs.graphopper.extensions.MyGraphHopper;
import de.tum.mw.ftm.deefs.location.Position;
import de.tum.mw.ftm.deefs.utils.RandomNumber;

import java.util.*;
import java.util.stream.Collectors;


/**
 * List Element to manage different types of facilities in one field.
 * Contains a Map with taxi ranks and charging points, where taxi ranks are also included if they have charging capabilities.
 *
 * @author Michael Wittmann
 */
public class FacilityList {

	private final Map<Integer, TaxiRank> ranks;                        // container for all taxi ranks in the simulation scenario
	private final Map<Integer, ChargingPossibility> chargingPoints;   // container for all charging possibilities in the simulation scenario


	/**
	 * New instance of facility list. Different types of Facilities are managed automatically in this Object.
	 *
	 * @param facilities Facilities
	 * @see TaxiRank
	 * @see ChargingStation
	 */
	public FacilityList(List<Facility> facilities) {
		//putting facilities into right containers... 
		this.chargingPoints = new HashMap<>();
		for (Facility f : facilities) {
			if (f instanceof ChargingPossibility) {
				this.chargingPoints.put(f.getId(), (ChargingPossibility) f);
			}
		}
		this.ranks = new HashMap<>();
		for (Facility facility : facilities) {
			if (facility instanceof TaxiRank) {
				ranks.put(facility.getId(), (TaxiRank) facility);
			}
		}
	}


	/**
	 * Adds a Facility to the facility List.
	 *
	 * @param f Facility to be added
	 */
	public void add(Facility f) {
		if (f instanceof ChargingPossibility) {
			this.chargingPoints.put(f.getId(), (ChargingPossibility) f);
		}
		if (f instanceof TaxiRank) {
			ranks.put(f.getId(), (TaxiRank) f);
		}
	}

	/**
	 * @return All taxi ranks in this FacilityList
	 */
	public List<TaxiRank> getTaxiRanks() {
		return new ArrayList<>(ranks.values());
	}

	/**
	 * Filters all listed charging possibilities which are compatible to the given Connector
	 *
	 * @param ci Connector which shell be compatible
	 * @return list with all charging possibilities that are compatible to the given Connector
	 * @see Connector
	 */
	public List<ChargingPossibility> getCompatibleChargingPoints(ChargingInterface ci) {
		return chargingPoints.values().stream().filter(c -> c.isCompatible(ci)).collect(Collectors.toList());
	}


	/**
	 * @return All listed charging points
	 */
	public List<ChargingPossibility> getChargingPoints() {
		return new ArrayList<>(chargingPoints.values());
	}

	/**
	 * @return Map with all listed taxi ranks
	 */
	public Map<Integer, TaxiRank> getTaxiRanksMap() {
		return ranks;
	}

	/**
	 * @return List with all listed charging stations
	 */
//	@SuppressWarnings("unchecked")
	public List<Facility> getChargingStations() {
		List<Facility> chargingStations = new ArrayList<>();
		for (ChargingPossibility chargingPoint : chargingPoints.values()) {
			chargingStations.add((Facility) chargingPoint);
		}
		return chargingStations;
	}


	/**
	 * @return Map with all listed charging possibilities
	 */
	public Map<Integer, ChargingPossibility> getChargingPointsMap() {
		return chargingPoints;
	}


	/**
	 * Looks for the closest compatible charging possibility which does not have a queue. Selection is made on selection rules defined in {@link CoarseNextChargingPossibilityComparatorChain}.
	 * In addition it is possible to exclude a Facility from the search process. This is necessary if an agent is already at a facility, but wants to find an other one.
	 * Without excluding he would always get the one he already is as result. This method calculates with linear distance between points. In fact there may be an other
	 * charging possibility which is closer when driving on the actual road network.
	 *
	 * @param ci                defines the compatible charging interface
	 * @param pos               actual vehicle position
	 * @param excluded_facility id of the facility which should be excluded in search process
	 * @return selected charging possibility or <b> null</b> if no possibility was found
	 * @see CoarseNextChargingPossibilityComparatorChain
	 */
	@SuppressWarnings("unchecked")
	public ChargingPossibility findClosestChargingPossibilityWithoutQueue(ChargingInterface ci, Position pos, int excluded_facility) {
		List<ChargingPossibility> collection = getCompatibleChargingPoints(ci);
		if (excluded_facility != 0) {
			collection = collection.stream().filter(c -> ((Facility) c).getId() != excluded_facility).collect(Collectors.toList());
		}
		collection = collection.stream().filter(c -> c.getQueueSize() == 0).collect(Collectors.toList());
		if (!collection.isEmpty()) {
			return Collections.min(collection, new CoarseNextChargingPossibilityComparatorChain(pos));
		} else {
			return null;
		}

	}


	/**
	 * Looks for the closest compatible charging possibility which has still space. Selection is made on selection rules defined in {@link CoarseNextChargingPossibilityComparatorChain}.
	 * In addition it is possible to exclude a Facility from the search process. This is necessary if an agent is already at a facility, but wants to find an other one.
	 * Without excluding he would always get the one he already is as result. This method calculates with linear distance between points. In fact there may be an other
	 * charging possibility which is closer when driving on the actual road network.
	 *
	 * @param ci                defines the compatible charging interface
	 * @param pos               actual vehicle position
	 * @param excluded_facility id of the facility which should be excluded in search process
	 * @return selected charging possibility or <b> null</b> if no possibility was found
	 * @see CoarseNextChargingPossibilityComparatorChain
	 */
	@SuppressWarnings("unchecked")
	public ChargingPossibility findClosestChargingFreePossibility(ChargingInterface ci, Position pos, int excluded_facility) {
		List<ChargingPossibility> collection = getCompatibleChargingPoints(ci);
		if (excluded_facility != 0) {
			collection = collection.stream().filter(c -> ((Facility) c).getId() != excluded_facility).collect(Collectors.toList());
		}
		collection = collection.stream().filter(c -> c.hasFreeChargingPoints(ci)).collect(Collectors.toList());
		if (!collection.isEmpty()) {
			return Collections.min(collection, new CoarseNextChargingPossibilityComparatorChain(pos));
		} else {
			return null;
		}

	}


	/**
	 * Looks for the <b>closest free compatible</b> charging possibility in a defined search radius. Selection is made on rules defined in {@link NextChargingPossibilityComparatorChain}.
	 * In a first selection process a short-list of max. 3 free and compatible charging possibilities is selected. In a second step those are compared by their reals distance to the agent by calculating the routes via GraphHopper.
	 * In addition it is possible to exclude a Facility from the search process. This is necessary if an agent is already at a facility, but wants to find an other one.
	 *
	 * @param ci                defines the compatible charging interface
	 * @param pos               actual vehicle position
	 * @param range             max. search range
	 * @param hopper            GraphHopper instance for exact distance estimations.
	 * @param excluded_facility id of the facility which should be excluded in search process
	 * @return selected charging possibility or <b> null</b> if no possibility was found
	 * @see NextChargingPossibilityComparatorChain
	 */
	@SuppressWarnings("unchecked")
	public ChargingPossibility findFreeChargingPossibilityInRange(ChargingInterface ci, Position pos, float range, MyGraphHopper hopper, int excluded_facility) {
		List<ChargingPossibility> collection = getCompatibleChargingPoints(ci);
		if (excluded_facility != 0) {
			collection = collection.stream().filter(c -> ((Facility) c).getId() != excluded_facility).collect(Collectors.toList());
		}
		//Looking for free cps in coarse range
		collection = collection.stream().filter(c -> c.hasFreeChargingPoints(ci) && ((Facility) c).getPosition().calcDist(pos) <= range).collect(Collectors.toList());
		if (!collection.isEmpty()) {
			//sorting collection and choose just the 3 closest
			collection.sort(new CoarseNextChargingPossibilityComparatorChain(pos));
			if (collection.size() > 3) {
				collection.subList(3, collection.size()).clear();
			}
			//filter out the ones out of exact range
			collection = collection.stream().filter(c -> hopper.route(new GHRequest(pos.getLat(), pos.getLon(), ((Facility) c).getPosition().getLat(), ((Facility) c).getPosition().getLon())).getDistance() <= range).collect(Collectors.toList());
			if (!collection.isEmpty()) {
				return Collections.min(collection, new NextChargingPossibilityComparatorChain(pos, ci, range, hopper));
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Looks for the <b>closest compatible</b> charging possibility in a defined search radius. Selection is made on rules defined in {@link NextChargingPossibilityComparatorChain}.
	 * In a first selection process a short-list of max. 3 free and compatible charging possibilities is selected. In a second step those are compared by their reals distance to the agent by calculating the routes via GraphHopper.
	 * In addition it is possible to exclude a Facility from the search process. This is necessary if an agent is already at a facility, but wants to find an other one.
	 *
	 * @param ci                defines the compatible charging interface
	 * @param pos               actual vehicle position
	 * @param range             max. search range
	 * @param hopper            GraphHopper instance for exact distance estimations.
	 * @param excluded_facility id of the facility which should be excluded in search process
	 * @return selected charging possibility or <b> null</b> if no possibility was found
	 * @see NextChargingPossibilityComparatorChain
	 */
	@SuppressWarnings("unchecked")
	public ChargingPossibility findClosestChargingPossibilityInRange(ChargingInterface ci, Position pos, float range, MyGraphHopper hopper, int excluded_facility) {
		List<ChargingPossibility> collection = getCompatibleChargingPoints(ci);
		if (excluded_facility != 0) {
			collection = collection.stream().filter(c -> ((Facility) c).getId() != excluded_facility).collect(Collectors.toList());
		}
		//Looking for free cps in coarse range
		collection = collection.stream().filter(c -> ((Facility) c).getPosition().calcDist(pos) <= range).collect(Collectors.toList());
		if (!collection.isEmpty()) {
			//sorting collection and choose just the 3 closest
			collection.sort(new CoarseNextChargingPossibilityComparatorChain(pos));
			if (collection.size() > 3) {
				collection.subList(3, collection.size()).clear();
			}
			//filter out the ones out of exact range
			collection = collection.stream().filter(c -> hopper.route(new GHRequest(pos.getLat(), pos.getLon(), ((Facility) c).getPosition().getLat(), ((Facility) c).getPosition().getLon())).getDistance() <= range).collect(Collectors.toList());
			if (!collection.isEmpty()) {
				return Collections.min(collection, new NextChargingPossibilityComparatorChain(pos, ci, range, hopper));
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Looks for the <b>best compatible</b> charging possibility in a defined search radius. Selection is made on rules defined in {@link ChargingPossibilityFastestComparator}.
	 * In a first selection process a short-list of max. 3 free and compatible charging possibilities is selected. In a second step those are compared by their reals distance to the agent by calculating the routes via GraphHopper.
	 * In addition it is possible to exclude a Facility from the search process. This is necessary if an agent is already at a facility, but wants to find an other one.
	 *
	 * @param ci                defines the compatible charging interface
	 * @param pos               actual vehicle position
	 * @param range             max. search range
	 * @param excluded_facility id of the facility which should be excluded in search process
	 * @return selected charging possibility or <b> null</b> if no possibility was found
	 * @see ChargingPossibilityFastestComparator
	 */
	public ChargingPossibility findBestChargingPossibilityInRange(ChargingInterface ci, Position pos, float range, int excluded_facility) {
		List<ChargingPossibility> collection = getCompatibleChargingPoints(ci);
		if (excluded_facility != 0) {
			collection = collection.stream().filter(c -> ((Facility) c).getId() != excluded_facility).collect(Collectors.toList());
		}
		List<ChargingPossibility> shortRangePossibilites = collection.stream().
				filter(c -> c.hasFreeChargingPoints(ci) &&
						((Facility) c).getPosition().calcDist(pos) <= range).collect(Collectors.toList());
		if (!shortRangePossibilites.isEmpty()) {
			//TODO: Add new rules to find the fastest, closest and the one with max free spots.
			return Collections.max(shortRangePossibilites, new ChargingPossibilityFastestComparator(ci));
		} else {
			return null;
		}

	}


	/**
	 * Looks for the best rank according to the selection rules defined in the given comparator.
	 * The taxi rank with the lowest natural order will be chosen.
	 * * In addition it is possible to exclude a Facility from the search process. This is necessary if an agent is already at a facility, but wants to find an other one.
	 *
	 * @param comparator        comparator with decision rules
	 * @param excluded_facility id of the facility which should be excluded in search process
	 * @return selected rank or <b> null</b> if no possibility was found
	 */
	public TaxiRank getBestRank(Comparator<TaxiRank> comparator, int excluded_facility) {
		List<TaxiRank> collection = getTaxiRanks();
		if (excluded_facility != 0) {
			collection = collection.stream().filter(c -> c.getId() != excluded_facility).collect(Collectors.toList());
		}
		return Collections.min(collection, comparator);
	}


	/**
	 * Selects randomly one of 20 best taxi ranks according to {@link NextRankComparatorChain}
	 *
	 * @param pos  actual vehicle position
	 * @param time simulation time in ms
	 * @return a randomly chosen taxi rank
	 */
	@SuppressWarnings("unchecked")
	public TaxiRank getRandomRank(Position pos, long time) {
		List<TaxiRank> collection = getTaxiRanks();
		collection.sort(new NextRankComparatorChain(pos, time));
		if (collection.size() > 20) {
			collection.subList(20, collection.size()).clear();
		}
		return collection.get(RandomNumber.randInt(0, 19));
	}


	/**
	 * This method calculates the number of cars already waiting at a taxi rank in the given area
	 *
	 * @param area_id area to be checked
	 * @return number of waiting cars in area
	 */
	public int getCarsAtRankByArea(int area_id) {
		List<TaxiRank> collection = getTaxiRanks();
		collection = collection.stream().filter(r -> r.getPosition().getArea() == area_id).collect(Collectors.toList());
		int waiting_cars = 0;
		for (TaxiRank r : collection) {
			waiting_cars += r.getQueueSize();
		}
		return waiting_cars;

	}

	/**
	 * Gives access to a certain charging possibility by its id
	 *
	 * @param id id of the charging possibility
	 * @return related charging possibility or <b> null</b> if the given id does not exist
	 */
	public ChargingPossibility getChargingPossibility(int id) {
		return chargingPoints.get(id);
	}

	/**
	 * Gives access to a certain taxi rank by its id
	 *
	 * @param id id of the taxi rank
	 * @return related taxi rank or <b> null</b> if the given id does not exist
	 */
	public TaxiRank getRank(int id) {
		return ranks.get(id);
	}

	/**
	 * Returns a List with all listed Facilities in this Object.
	 *
	 * @return list with all listed Facilities in this Object.
	 */
	public List<Facility> getFacilities() {
		List<Facility> facilities = new ArrayList<>(ranks.size() + chargingPoints.size());
		facilities.addAll(getTaxiRanks());
		facilities.addAll(getChargingStations());
		return facilities;
	}
}
