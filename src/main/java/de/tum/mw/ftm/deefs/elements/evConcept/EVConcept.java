package de.tum.mw.ftm.deefs.elements.evConcept;

import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.utils.Units;

/**
 * EVConcept stands for ElectricVehicleConcept.It is a Class to collect all components that specify a electric vehicle. Such as battery and charging interface.
 * In the future there will be more components to get better results.
 *
 * @author Michael Wittmann
 */
public class EVConcept {
	private final Battery battery;
	private final ChargingInterface chargingInterface;
	private final float e_mean;        //mean consumption in J/m
	private final String conceptName;


	/**
	 * Basic Constructor of EVConcept
	 *
	 * @param battery           specified battery
	 * @param chargingInterface specified chargingInterface
	 * @param v_mean            mean consumption in kWh/100km
	 * @param conceptName       name of the concept
	 */
	public EVConcept(Battery battery, ChargingInterface chargingInterface, float v_mean, String conceptName) {
		this.battery = battery;
		this.chargingInterface = chargingInterface;
		this.e_mean = (float) Units.kWh_per_100km_To_J_per_m(v_mean);
		this.conceptName = conceptName;
	}

	@Deprecated
	public static EVConcept getTestConcept() {
		return new EVConcept(new Battery(18.8f, 355.2f, 3.6f, 4.2f, 0.9f, 100f),
				new ChargingInterface(2800, 7400, 50000, 0, 0),
				20.6f,
				"default");
	}

	/**
	 * Gives the Concept's Battery
	 *
	 * @return concept's battery
	 */
	public Battery getBattery() {
		return this.battery;
	}

	/**
	 * Gives the Concept's ChargingInterface
	 *
	 * @return concept's charging interface
	 */
	public ChargingInterface getChargingInterface() {
		return this.chargingInterface;
	}

	/**
	 * Gives the mean consumption of the specified concept
	 *
	 * @return mean consumption in <b>J/m</b>
	 */
	public float getVMean() {
		return this.e_mean;
	}

	/**
	 * Gives the concept's name
	 *
	 * @return concept's name
	 */
	public String getConceptName() {
		return this.conceptName;
	}

	/**
	 * Calculates the energy needed in J for a given distance in m
	 *
	 * @param distance in <b>m</b>
	 * @return needed energy in <b>J</b>
	 */
	public float getNeededEnergy(float distance) {
		return this.e_mean * distance;
	}

	@Override
	public String toString() {
		return conceptName;
	}
}
