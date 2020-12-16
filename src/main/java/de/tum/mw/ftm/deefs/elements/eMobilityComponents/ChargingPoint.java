package de.tum.mw.ftm.deefs.elements.eMobilityComponents;

import de.tum.mw.ftm.deefs.Config;
import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.elements.evConcept.Battery;
import de.tum.mw.ftm.deefs.elements.facilitiies.ChargingStation;
import de.tum.mw.ftm.deefs.elements.facilitiies.Facility;
import de.tum.mw.ftm.deefs.elements.taxi.BEVTaxi;
import de.tum.mw.ftm.deefs.events.UpdateChargeEvent;
import de.tum.mw.ftm.deefs.log.EnergyStats;
import de.tum.mw.ftm.deefs.utils.Units;


/**
 * ChargingPoint describes a element, which can be used to recharge electric vehicles. Only one vehicle can be connected to a charging point at the same time.
 * ChargingPoint <b>does not</b> describe a Facility, as ChargingPoint is not physically localized. This class is designed to be used inside Facilities to provide a charging possibility.
 *
 * @author Michael Wittmann
 * @see ChargingStation
 * @see Facility
 * @see ChargingInterface
 */
public class ChargingPoint {
	private static final long UPDATE_INTERVALL = Long.parseLong(Config.getProperty(Config.CHARGINGPOINT_UPDATE_INTERVAL, "60000"));    //interval a UpdateChargeEvent is pushed in ms
	private static final long delta_t = Long.parseLong(Config.getProperty(Config.CHARGINGPOINT_CHARGING_CURVE_DELTA_T, "60000"));        // step size for calculation of charging curve in ms

	private final Scenario scenario;
	private final ChargingInterface chargingInterface;    //charging interface of the charging point
	private UpdateChargeEvent nextEvent = null;    //next scheduled event of this charging point
	private BEVTaxi connected_car = null;            //connected car
	private long connected_since = -1;                //time the car is connected in ms
	private Connector connector = null;                //used connector
	private int parent_facility_id = 0;                //id of facility this charging point belongs to

	/**
	 * Basic constructor. A ChargingPoint consists a defined ChargingInterface and a scenario.
	 *
	 * @param chargingInterface certain ChargingInterface the ChargingPoint offers
	 * @param scenario          scenario in which the ChargingPoint operates
	 */
	public ChargingPoint(ChargingInterface chargingInterface, Scenario scenario) {
		this.scenario = scenario;
		this.chargingInterface = chargingInterface;
	}

	/**
	 * This Method should be used when the car charges at home, as no charging points are modeled there right now.
	 * Calculations based on the description in {@link #chargeCar(long, long, BEVTaxi)}
	 *
	 * @param chargingTime time the car was at home for charing in ms
	 * @param car          car to be charged
	 */
	public static void chargeCarAtHome(long chargingTime, BEVTaxi car) {
		float energy;
		float p_max = car.getConcept().getChargingInterface().getHomeConnector().getPMax();
		Battery battery = car.getConcept().getBattery();
		long n = chargingTime / delta_t;
		long r = chargingTime - (n * delta_t);
		for (long i = 1; i <= n; i++) {
			energy = getEnergyIU(delta_t, p_max, battery);
			car.getConcept().getBattery().charge(energy);
			if (car.getSOC() == 100) return;
		}
		if (r != 0) {
			energy = getEnergyIU(r, p_max, battery);
			car.getConcept().getBattery().charge(energy);
		}
	}

	/**
	 * Calculates the energy charged in the given timestep
	 * The amount of energy is calculated via an UI-charging model, introduced by [Str13].
	 * It is recommended to use a timestep of 60000ms which is a good compromise between accuracy and computation time. See [Wit15] for further details.
	 *
	 * @param timestep charging time in ms
	 * @param p_max    maximum power supported by charging point an vehicle
	 * @param battery  vehicle's battery
	 * @return amount of energy the car was charged in J
	 */
	public static float getEnergyIU(long timestep, float p_max, Battery battery) {
		float energy;
		float soc = battery.getSoc() / 100f;
		float u_cell_n = battery.getU_cell_n();
		float u_cell_ls = battery.getU_cell_ls();
		float e_bat_max = battery.getEBatMax();
		float e_bat = battery.getE_bat();
		float eta_l = battery.getEta_l();
		float i_ls = 0.006f / 1000f * p_max + 0.008f;
		float s = -0.008f / 1000f * p_max + 0.83f;
		if (soc < s) {
			energy = p_max * timestep / 1000f;
		} else {
			float p_ls = (u_cell_ls / u_cell_n * i_ls * (float) Units.JToKWh(e_bat_max)) * 1000f;
			float k_l = (float) ((1 - s) / Math.log(p_max / p_ls));
			float p = (float) (p_max * Math.exp((s - soc) / k_l));
			energy = p * timestep / 1000f;
		}
		float newCharge = e_bat + energy * eta_l;
		if (newCharge > e_bat_max) {
			energy = (e_bat_max - e_bat) / eta_l;
			return energy;
		} else {
			return energy;
		}

	}

	public void setParentFacility(int parent_facility_id) {
		this.parent_facility_id = parent_facility_id;
	}

	/**
	 * Checks if this ChargingPoint is available.
	 *
	 * @return <b>true</b> if this charging point is free or <b>false</b> if this charging point is occupied.
	 */
	public boolean isAvailable() {
		return connected_car == null;
	}

	/**
	 * Checks if the given Car is connected to the charging point.
	 *
	 * @param car to be checked
	 * @return <b>true</b> if the given car is connected to this charging point <b>false</b> otherwise
	 */
	@Deprecated
	public boolean isConnected(BEVTaxi car) {
		return car == connected_car;
	}

	/**
	 * Checks if this charging point is compatible to the given charging interface
	 *
	 * @param ci CharginInterface to be checked
	 * @return <b>true</b> if this charging point has a compatible charging interface to the one given, <b>false</b> otherwise.
	 */
	public boolean isCompatibleTo(ChargingInterface ci) {
		return chargingInterface.isCompatibleTo(ci);
	}

	/**
	 * Checks if this charging point is compatible to the car's given charging interface
	 *
	 * @param car car to be checked
	 * @return <b>true</b> if this charging point has a compatible charging interface to the car's given, <b>false</b> otherwise.
	 */
	@Deprecated
	public boolean isCompatibleTo(BEVTaxi car) {
		return this.isCompatibleTo(car.getChargingInterface());
	}

	/**
	 * Connects the given car to this charging point, if possible(a car can only be connected, if it is compatible and
	 * if there is no other car already connected to the charging point). Connecting a car to a charging point
	 * will automatically start the charging process after a specified plug in time based on the selected connector.
	 * The car's SOC will be updated in a fixed interval, defined in this class.
	 *
	 * @param car  car to connect
	 * @param time time at which the car is connected
	 * @return <b>true<b> if the car was connected successfully, <b>false</b> otherwise.
	 */
	public boolean connect(BEVTaxi car, long time) {
		if (isCompatibleTo(car.getChargingInterface()) && isAvailable()) {
			connected_car = car;
			connected_since = time;
			connector = getBestConnector(car.getChargingInterface());
			nextEvent = new UpdateChargeEvent(time + UPDATE_INTERVALL + connector.getPlugInTime(), time + connector.getPlugInTime(), this);
			scenario.addEvent(nextEvent);
			scenario.getDBLog().addEnergyStats(new EnergyStats(parent_facility_id, car.getId(), time, 0, 0, connector.getTypeAsString(), connector.getPMax()));
			return true;
		} else return false;
	}

	/**
	 * Returns the best available connector(according to their natural order) between the given car and this charging point.
	 *
	 * @param ci to check
	 * @return best available connector between given car and charging point
	 * @see ChargingInterface
	 * @see Connector
	 */
	public Connector getBestConnector(ChargingInterface ci) {
		return chargingInterface.getBestConnector(ci);
	}

	/**
	 * Checks if it is possible to disconnect the actual connected car from this charging point.
	 * Depending on the defined MIN_TIME_CHARGING it may be denied to disconnect the car.
	 * This fact represents the circumstance that a minimum time must be calculated to connect and disconnect the car to/from the charging point.
	 *
	 * @param time time at which the car should be disconnected.
	 * @return <b>true</b> if it is possible to disconnect the car, <b>false</b> otherwise or if no car is connected to the charging point
	 */
	public boolean mayDisconnect(long time) {
		if (connected_car != null) {
			return (time - connected_since) > connected_car.MIN_TIME_CHARGING;
		}
		return false;
	}

	/**
	 * Disconnects the actual connected car from this charging point. The charging process will be stopped automatically,
	 * and future scheduled UpdateChargeEvents will be removed from the event queue.
	 *
	 * @param time to be disconnected
	 * @return <b>true</b> if the car was successfully disconnected, <b>false</b> otherwise or if no car was connected
	 */
	public boolean disconnect(long time) {
		if (connected_car != null) {
			if (mayDisconnect(time)) {
				if (nextEvent != null) {
					if (nextEvent.getPostedTime() < time) {
						//needed beacaue posted time has an offset of connectors plugintime, so if a disconnect occurs in this time a negative energy would be calculatetd
						chargeCar(nextEvent.getPostedTime(), time, connected_car);
					}
					scenario.removeEvent(nextEvent);
					nextEvent = null;
				}
				connected_car = null;
				connected_since = -1;
				return true;
			}
		}
		return false;
	}

	/**
	 * Gives the time at which the car was connected to the charging point
	 *
	 * @return time at which the car was connected to the charging point, or -1 if no car is connected.
	 */
	public long getTimeConnectedSince() {
		return this.connected_since;
	}

	/**
	 * Updates the charge of the connected car and pushes new update events to the event queue according to the defined interval.
	 */
	public void updateCharge(long time) {
		if (connected_car != null) {
			chargeCar(nextEvent.getPostedTime(), nextEvent.getScheduledTime(), connected_car);
			//TODO: Fix this dirty fix ;-)
			//Dirty quick fix. because cars now can stop charging when their max_time is exceeded.
			if (connected_car != null) {
				if (connected_car.getSOC() < connected_car.SOC_STOP_CHARGE_MAX) {
					nextEvent = new UpdateChargeEvent(nextEvent.getScheduledTime() + UPDATE_INTERVALL, nextEvent.getScheduledTime(), this);
					scenario.addEvent(nextEvent);
				} else {
					nextEvent = null;
					connected_car.nextAction(time);
				}
			}
		}
	}

	/**
	 * Calculates the energy charged in the given time interval, and updates the vehicles charge.
	 * The amount of energy is calculated via an UI-charging model, introduced by [Str13]. The numeric timestep used for the calculation can be defined in delta_t.
	 * It is recommended to use a timestep of 60000ms which is a good compromise between accuracy and computation time. See [Wit15] for further details.
	 * Charging actions will be logged in EnergyStats data-logger.
	 *
	 * @param timeBegin start time of charging interval in ms
	 * @param timeEnd   end time of charging interval in ms
	 * @param car       connected car
	 * @return amount of energy the car was charged in J
	 */
	private float chargeCar(long timeBegin, long timeEnd, BEVTaxi car) {
		float energy_sum = 0;
		float energy;
		float p_max = connector.getPMax();
		Battery battery = car.getConcept().getBattery();
		long chargingTime = timeEnd - timeBegin;

		long n = chargingTime / delta_t;
		long r = chargingTime - (n * delta_t);

		for (long i = 1; i <= n; i++) {
			energy = getEnergyIU(delta_t, p_max, battery);
			car.chargeCar(timeBegin + i * delta_t, energy);
			scenario.getDBLog().addEnergyStats(new EnergyStats(parent_facility_id, car.getId(), timeBegin + i * delta_t, energy / delta_t * 1000, energy, connector.getTypeAsString(), connector.getPMax()));
			energy_sum += energy;
		}
		if (r != 0) {
			energy = getEnergyIU(r, p_max, battery);
			car.chargeCar(timeEnd, energy);
			scenario.getDBLog().addEnergyStats(new EnergyStats(parent_facility_id, car.getId(), timeEnd, energy / delta_t * 1000, energy, connector.getTypeAsString(), connector.getPMax()));
			energy_sum += energy;
		}
		return energy_sum;
	}
}
