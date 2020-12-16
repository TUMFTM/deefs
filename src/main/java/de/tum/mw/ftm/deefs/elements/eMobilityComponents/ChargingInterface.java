package de.tum.mw.ftm.deefs.elements.eMobilityComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * ChargingInterface describes a collection of possible Connectors. It delivers methods to find the best common Connector of two ChargingInterfaces
 *
 * @author Michael Wittmann
 * @see Connector
 */
public class ChargingInterface {
	private final List<Connector> connectors;

	/**
	 * Basic Constructor. A Charging interface with no content will be created. Connectors have to be added manually via addConnector.
	 */
	public ChargingInterface() {
		connectors = new ArrayList<>();
	}


	/**
	 * Using this constructor, the ChargingInterface will automatically be filled with Connectors of the specified type and power.
	 * If <b>0</b> is chosen as input, <b>no connector</b> of the specified type will be created.
	 *
	 * @param p_schuko       max. power of Connector Schuko in <b>Watt</b>
	 * @param p_typ2         max. power of Connector Typ2 in <b>Watt</b>
	 * @param p_ccs          max. power of Connector CCS in <b>Watt</b>
	 * @param p_chademo      max. power of Connector Chademo in <b>Watt</b>
	 * @param p_supercharger max. power of Connector Supercharger in <b>Watt</b>
	 * @see Connector
	 */
	public ChargingInterface(float p_schuko, float p_typ2, float p_ccs, float p_chademo, float p_supercharger) {
		this();
		if (p_schuko > 0) {
			Connector c = new Connector(Connector.TYPE_SCHUKO, p_schuko);
			addConnector(c);
		}
		if (p_typ2 > 0) {
			Connector c = new Connector(Connector.TYPE_TYPE2, p_typ2);
			addConnector(c);
		}
		if (p_ccs > 0) {
			Connector c = new Connector(Connector.TYPE_CCS, p_ccs);
			addConnector(c);
		}
		if (p_chademo > 0) {
			Connector c = new Connector(Connector.TYPE_CHADEMO, p_chademo);
			addConnector(c);
		}
		if (p_supercharger > 0) {
			Connector c = new Connector(Connector.TYPE_SUPERCHARGER, p_supercharger);
			addConnector(c);
		}
	}

	/**
	 * Use this method to manually add a Connector.
	 *
	 * @param c Connector to add
	 * @see Connector
	 */
	public void addConnector(Connector c) {
		connectors.add(c);
	}

	/**
	 * Checks if the two given ChargingInterfaces have compatible connectors
	 *
	 * @param cf ChargingInterface to check
	 * @return <b>true</b> if both ChargingInterfaces have Connectors that are compatible to each other
	 */
	public boolean isCompatibleTo(ChargingInterface cf) {
		for (Connector c1 : connectors) {
			for (Connector c2 : cf.connectors) {
				if (c1.isCompatible(c2)) return true;
			}
		}
		return false;
	}

	/**
	 * Returns the connector which should be used for low power charging
	 * during inactive times to save the energy network from overload.
	 *
	 * @return the connector which should be used for low power charging
	 */
	public Connector getHomeConnector() {
		return Collections.min(connectors);
	}

	/**
	 * Returns the best Connector (according to their natural order) the two given ChargingInterfaces have in common.
	 *
	 * @param cf ChargingInterface to check
	 * @return best connector both ChargingInterfaces support or <b>null</b> if no compatible Connector was found
	 * @see Connector
	 */
	public Connector getBestConnector(ChargingInterface cf) {
		List<Connector> tmp = new ArrayList<>();
		for (Connector c1 : connectors) {
			for (Connector c2 : cf.connectors) {
				if (c1.isCompatible(c2)) {
					if (c1.compareTo(c2) < 0) {
						tmp.add(c1);
					} else {
						tmp.add(c2);
					}
				}
			}
		}
		if (!tmp.isEmpty()) {
			return Collections.max(tmp);
		} else return null;
	}
}
