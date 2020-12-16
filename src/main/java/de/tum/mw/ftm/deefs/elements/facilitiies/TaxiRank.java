package de.tum.mw.ftm.deefs.elements.facilitiies;

import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;
import de.tum.mw.ftm.deefs.location.Position;
import de.tum.mw.ftm.deefs.log.FacilityStats;

import java.util.Calendar;
import java.util.LinkedList;

/**
 * Represents a taxi rank  in the behavior model. To create a instance of TaxiRank use {@link FacilityFactory}.
 *
 * @author Michael Wittmann
 * @see FacilityFactory
 * @see Facility
 */
public class TaxiRank extends Facility {

	// priorities are not used anymore...
	@Deprecated
	public static final int PRIORITY_MOST_POPULAR = 2;
	@Deprecated
	public static final int PRIORITY_POPULAR = 1;
	@Deprecated
	public static final int PRIORITY_STANDARD = 0;
	@Deprecated
	public static final int PRIORITY_UNPOPULAR = -1;
	@Deprecated
	public static final int PRIORITY_MOST_UNPOPULAR = -2;

	@Deprecated
	private final int priority;
	private final LinkedList<Taxi> queue;
	private String description = "-";
	private String address = "-";
	private float demand_21_03 = 0; //Average demand per hour from 21:00:00 - 03:00:00
	private float demand_03_09 = 0; //Average demand per hour from 03:00:00 - 09:00:00
	private float demand_09_15 = 0; //Average demand per hour from 09:00:00 - 15:00:00
	private float demand_15_21 = 0; //Average demand per hour from 15:00:00 - 21:00:00
//	private Map<Integer, CarAtFacilityLog> log;

	/**
	 * Basic Constructor
	 *
	 * @param id       the rank's id
	 * @param position the rank's position
	 * @param scenario scenario the rank operates in
	 * @param capacity the rank's capacity
	 */
	protected TaxiRank(int id, Position position, Scenario scenario, int capacity, int priority) {
		super(id, scenario, position, capacity);
		this.queue = new LinkedList<>();
		this.priority = priority;
//		this.log = new HashMap<Integer, CarAtFacilityLog>();
	}

	/**
	 * Extended Constructor
	 *
	 * @param id          the rank's id
	 * @param position    the rank's position
	 * @param scenario    scenario the rank operates in
	 * @param capacity    the rank's capacity
	 * @param adress      the rank's address
	 * @param description additional rank description
	 */
	protected TaxiRank(int id, Position position, Scenario scenario, int capacity, int priority, String adress, String description, float demand1,
					   float demand2, float demand3, float demand4) {
		this(id, position, scenario, capacity, priority);
		this.address = adress;
		this.description = description;
		this.demand_21_03 = demand1;
		this.demand_03_09 = demand2;
		this.demand_09_15 = demand3;
		this.demand_15_21 = demand4;
	}

	@Deprecated
	public static int getPriority(String tag) {
		switch (tag) {
			case "MOST_POPULAR":
				return PRIORITY_MOST_POPULAR;
			case "POPULAR":
				return PRIORITY_POPULAR;
			case "STANDARD":
				return PRIORITY_STANDARD;
			case "UNPOPULAR":
				return PRIORITY_UNPOPULAR;
			case "MOST_UNPOPULAR":
				return PRIORITY_MOST_UNPOPULAR;
			default:
				return PRIORITY_STANDARD;
		}
	}

	/**
	 * Returns the estimated demand at time time
	 *
	 * @param time simulation time in ms
	 * @return estimated customer demand for taxi ranks in this area
	 */
	public float getDemand(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 3) {
			return demand_21_03;
		} else if (hour < 9) {
			return demand_03_09;
		} else if (hour < 15) {
			return demand_09_15;
		} else if (hour < 21) {
			return demand_15_21;
		} else {
			return demand_21_03;
		}
	}

	/**
	 * Returns the weight which is used to choose the taxi rank with the highest expectable customer demand.
	 * Therefore a relative weight is calculated based on the average demand in the area, and the number of taxis already waiting in this area.
	 * <p> The weight is calculated: expected demand in time period / Number of waiting cars in this area
	 *
	 * @param time actual time in ms
	 * @return demand weight
	 */
	public float getDemandWeight(long time) {
		return getDemand(time) / (scenario.getFacilities().getCarsAtRankByArea(this.position.getArea()) + 1);
	}

	/**
	 * Gives a description of this rank if available
	 *
	 * @return rank description if available, if not "-"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gives the address of this rank if available
	 *
	 * @return rank address if available, if not "-"
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Gives the actual position of the given car in the queue
	 *
	 * @param o1 car to check
	 * @return position of car o1 in the queue, -1 if the car is not in the queue
	 */
	public int getQueuePosition(Taxi o1) {
		return queue.indexOf(o1);
	}

	/**
	 * Returns the number of vehicles connected to this taxi rank
	 *
	 * @return the number of vehicles connected to this taxi rank
	 */
	public int getQueueSize() {
		return queue.size();
	}

	@Override
	public boolean hasSpace() {
		return queue.size() < getCapacity();
	}

	@Override
	public int getRemainingSpace() {
		return getCapacity() - queue.size();
	}

	@Override
	public boolean checkInCar(Taxi car, long time) {
		if (hasSpace()) {
			scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_CHECKIN, queue.size() + 1, 0));
//			Log.rankStats(this, String.format("%d\tLogin Car: %d\n",time, car.getId()));
			return queue.offerLast(car);
		} else {
			scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_CHECKIN_DENIED, queue.size(), 0));
			return false;
		}
	}

	@Override
	public boolean checkOutCar(Taxi car, long time) {
		scenario.getDBLog().addFacilityStats(new FacilityStats(this.id, car.getId(), time, ACTION_CHECKOUT, queue.size() - 1, 0));
//		Log.rankStats(this, String.format("%d\tLogout Car: %d\n",time, car.getId()));
		return queue.remove(car);
	}

	@Override
	public String toString() {
		return String.format("RankID: %d Description: %s", this.id, description);
	}

	@Deprecated
	public int getPriority() {
		return priority;
	}


}
