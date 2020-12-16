package de.tum.mw.ftm.deefs.elements.eMobilityComponents;

import de.tum.mw.ftm.deefs.Config;

/**
 * The Connector-class represents the type of connection an electric car can use to recharge his battery.
 * Actually 5 Types of connectors are defined:
 * </br>TYPE_SCHUKO
 * </br>TYPE_TYPE2
 * </br>TYPE_CCS
 * </br>TYPE_CHADEMO
 * </br>TYPE_SUPERCHARGER
 * In addition a connector specifies the maximum power which can be used.
 *
 * @author Michael Wittmann
 */
public class Connector implements Comparable<Connector> {


	public static final int TYPE_SCHUKO = 1;
	public static final int TYPE_TYPE2 = 2;
	public static final int TYPE_CCS = 3;
	public static final int TYPE_CHADEMO = 4;
	public static final int TYPE_SUPERCHARGER = 5;
	private static final String[] TYPE_NAMES = {"SCHUKO", "TYP2", "CCS", "CHADEMO", "SUPERCHARGER"};
	private final long plug_in_time;  // time one needs to connect a car with a charging point
	private final float p_max;    //maximum supported charging power of this connector in W
	private int type;


	/**
	 * Constructor
	 *
	 * @param type  specified Type. Use the static fields from this class to set this attribute
	 * @param p_max maximal supported power in <b>Watt</b>.
	 */
	public Connector(int type, float p_max) {
		setType(type);
		this.p_max = p_max;
		plug_in_time = Long.parseLong(Config.getProperty(Config.CONNECTOR_PLUG_IN_TIME, "180000"));
	}

	/**
	 * Returns the specified time needed to plug in the car to the chargingpoint
	 *
	 * @return
	 */
	public long getPlugInTime() {
		return plug_in_time;
	}

	/**
	 * Returns the specified type of the connector.
	 *
	 * @return type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type of the connector. One of the class-defined connectors must be used.
	 *
	 * @param type
	 */
	private void setType(int type) {
		if (type == TYPE_SCHUKO ||
				type == TYPE_TYPE2 ||
				type == TYPE_CCS ||
				type == TYPE_CHADEMO ||
				type == TYPE_SUPERCHARGER) {
			this.type = type;
		} else {
			throw new RuntimeException("Connector Unknown!");
		}
	}

	/**
	 * Checks if the two given Connectors are compatible to each other
	 *
	 * @param c Connector to check.
	 * @return <b>true</b> if the to connectors match <b>false</b> otherwise
	 */
	public boolean isCompatible(Connector c) {
		return this.type == c.type;
	}

	/**
	 * Gives back the maximal supported power
	 *
	 * @return maximal power in <b>Watt</b>
	 */
	public float getPMax() {
		return p_max;
	}

	/**
	 * Returns the highest common power supported together with c
	 *
	 * @param c Connector to check
	 * @return highest commomn power in <b>Watt</b> supported together with c. Returns <b>0.0</b> if both Connectors are not compatible.
	 */
	public float getPMax(Connector c) {
		if (type == c.type) {
			return Math.max(p_max, c.p_max);
		} else return 0f;
	}

	/**
	 * Gives you the Name of the connector's type.
	 *
	 * @return the connector's type as readable text.
	 */
	public String getTypeAsString() {
		return TYPE_NAMES[type - 1];
	}


	@Override
	public String toString() {
		return String.format("%s(%.2f W)", getTypeAsString(), getPMax());
	}

	@Override
	public int compareTo(Connector o) {
		return Float.compare(this.p_max, o.p_max);
	}

}
