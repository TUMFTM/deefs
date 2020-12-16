package de.tum.mw.ftm.deefs.utils;

/**
 * Helper Class to convert units
 *
 * @author Michael Wittmann
 */
public class Units {


	public static final int TIME_SECOND = 1;
	public static final int TIME_MINUTE = TIME_SECOND * 60;
	public static final int TIME_HOUR = TIME_MINUTE * 60;
	public static final int TIME_DAY = TIME_HOUR * 24;

	/**
	 * Converts kWh into J
	 *
	 * @param kWh
	 * @return J
	 */
	public static double kWhToJ(double kWh) {
		return kWh * 1000 * TIME_HOUR;
	}

	/**
	 * Converts J to kWh
	 *
	 * @param J
	 * @return kWh
	 */
	public static double JToKWh(double J) {
		return J / 1000 / TIME_HOUR;
	}

	/**
	 * Converts kWh/100km to J/m
	 *
	 * @param kWh_per_100km kWh/100km
	 * @return J/m
	 */
	public static double kWh_per_100km_To_J_per_m(double kWh_per_100km) {
		return 36 * kWh_per_100km;
	}


	/**
	 * Converts J/m to kWh/100km
	 *
	 * @param J_per_m J/m
	 * @return kWh/100km
	 */
	public static double J_per_m_To_kWh_per_100km(double J_per_m) {
		return J_per_m / 36;
	}


}
