package de.tum.mw.ftm.deefs.elements.evConcept;

import de.tum.mw.ftm.deefs.utils.Units;

/**
 * This Class represents a battery unit of an electric vehicle.
 *
 * @author Michael Wittmann
 */
public class Battery {
	private final float e_bat_max;    // Max. battery energy content in J
	private final float u_bat;        // Battery voltage in V
	private final float u_cell_n;    // nominal cell voltage in V
	private final float u_cell_ls;    // Ladeschlussspannung Cell
	private final float eta_l;        // charging efficiency
	private float e_bat;        // Remaining energy content in J


	/**
	 * Battery Constructor
	 *
	 * @param e_bat_max Battery capacity in <b>kWh</b>
	 * @param u_bat     Battery current in <b>V</b>
	 */
	public Battery(float e_bat_max, float u_bat, float u_cell_n, float u_cell_ls, float eta_l) {
		this.e_bat_max = (float) Units.kWhToJ(e_bat_max);
		this.e_bat = this.e_bat_max;
		this.u_bat = u_bat;
		this.u_cell_n = u_cell_n;
		this.u_cell_ls = u_cell_ls;
		this.eta_l = eta_l;
	}

	/**
	 * Battery Constructor
	 *
	 * @param e_bat_max Battery capacity in <b>kWh</b>
	 * @param u_bat     Battery current in <b>V</b>
	 * @param soc       State-Of-Charge in <b>%</b>
	 */
	public Battery(float e_bat_max, float u_bat, float u_cell_n, float u_cell_ls, float eta_l, float soc) {
		this(e_bat_max, u_bat, u_cell_n, u_cell_ls, eta_l);
		setSoc(soc);
	}

	/**
	 * Gives back the current SOC
	 *
	 * @return SOC in %
	 */
	public float getSoc() {
		return this.e_bat / e_bat_max * 100f;
	}

	/**
	 * Set the Battery SOC
	 *
	 * @param soc to be set in [0%;100%]
	 */
	private void setSoc(float soc) {
		if (soc > 100) this.e_bat = e_bat_max;
		else if (soc < 0) this.e_bat = 0f;
		else this.e_bat = soc * e_bat_max / 100f;
	}

	/**
	 * Gives back maximum capacity of the battery
	 *
	 * @return maximum capacity in <b>J</>
	 */
	public float getEBatMax() {
		return this.e_bat_max;
	}

	/**
	 * Gives back current capacity of the battery
	 *
	 * @return current capacity in <b>J</>
	 */
	public float getE_bat() {
		return e_bat;
	}

	/**
	 * Gives back the voltage of battery
	 *
	 * @return battery voltage <b>V</>
	 */
	public float getU_bat() {
		return u_bat;
	}

	/**
	 * Gives back the nominal voltage of a single cell
	 *
	 * @return nominal voltage in  <b>V</>
	 */
	public float getU_cell_n() {
		return u_cell_n;
	}

	/**
	 * Gives back the Ladeschlussspannung  of a single cell
	 *
	 * @return Ladeschlussspannung in  <b>V</>
	 */
	public float getU_cell_ls() {
		return u_cell_ls;
	}

	/**
	 * charging efficiency
	 *
	 * @return charging efficiency  <b>V</>
	 */
	public float getEta_l() {
		return eta_l;
	}

	/**
	 * Discharge the battery with a specified energy in J
	 *
	 * @param joule taken energy from the battery in <b>J</b>
	 */
	public void discharge(float joule) {
		if (joule >= 0) {
			float newCharge = e_bat - joule;
			if (newCharge < 0) e_bat = 0;
			else e_bat = newCharge;
		} else {
			throw new RuntimeException("Error while discharging Battery, negative Energies do not exist!");
		}
	}

	/**
	 * Calculates the estimated SOC after taking a certain amount of energy from the battery.
	 *
	 * @param joule possibly taken energy from the battery in <b>J</b>
	 * @return estimated SOC after taking the given Energy in %
	 */
	public float estimateNewSOC(float joule) {
		float newCharge = e_bat - joule;
		return newCharge / e_bat_max * 100f;
	}

	/**
	 * Charging the Battery by putting a certain amount of energy into the battery
	 * Charging considers the charging efficiency given by eta_l.<br>
	 * e_bat+= energy*eta_l;
	 *
	 * @param energy energy to put to the battery in <b>J</b>
	 */
	public void charge(float energy) {
		e_bat += energy * eta_l;
		if (e_bat > e_bat_max) {
			e_bat = e_bat_max;
			System.err.println("Warning: The Battery is already full charged. Make shure to terminate cahring if Battery is full");
		}
	}
}
