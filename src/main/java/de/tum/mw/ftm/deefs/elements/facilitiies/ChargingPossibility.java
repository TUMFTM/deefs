package de.tum.mw.ftm.deefs.elements.facilitiies;

import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.Connector;
import de.tum.mw.ftm.deefs.elements.taxi.BEVTaxi;

/**
 * Interface that defines certain methods a Charging Possibility must offer.
 * This way it is possible to simply add ChargingPossibility to other Facilities than ChargingStations later on if needed.
 *
 * @author Michael Wittmann
 */
public interface ChargingPossibility {

	/**
	 * Returns the number of available charging points.
	 *
	 * @return the number of available charging points
	 */
	int numberOfAvailableCP();

	/**
	 * Looks if there are <b>free</b> and <b>compatible</b> charing points
	 *
	 * @param ci charging interface requirement
	 * @return <b>true</b> if there are free and compatible charing points, <b>false</b> otherwise
	 */
	boolean hasFreeChargingPoints(ChargingInterface ci);

	/**
	 * Looks for the connector which supports the highest power to the given charging interface
	 *
	 * @param ci charging interface requirement
	 * @return best possible connector
	 */
	Connector bestConnector(ChargingInterface ci);

	/**
	 * Checks if the given charging interface is compatible to this charging possibility
	 *
	 * @param ci charging interface requirement
	 * @return <b>true</b> if there are compatible charing points, <b>false</b> otherwise
	 */
	boolean isCompatible(ChargingInterface ci);

	/**
	 * Checks if the connected car is allowed to quit the charging process.
	 *
	 * @param car  connected car
	 * @param time simulation time in ms
	 * @return <b>true</b> if the connected car is allowed to quit the charging process, <b>false</b> otherwise
	 */
	boolean mayTerminateCharging(BEVTaxi car, long time);

	/**
	 * Sets the given car into the waiting queue, if all compatible charging points are occupied by other vehicles.
	 *
	 * @param car  car to connect
	 * @param time simulation time in ms
	 * @return <b>true</b> if car was successfully added to queue, <b>false</b> otherwise
	 */
	boolean loginToQueue(BEVTaxi car, long time);

	/**
	 * Removes the car from the waiting queue.
	 *
	 * @param car  car to be removed
	 * @param time simulation time in ms
	 * @return <b>true</b> if car was successfully removed from queue, <b>false</b> otherwise
	 */
	boolean abortWaiting(BEVTaxi car, long time);


	/**
	 * Returns the number of waiting cars in queue
	 *
	 * @return the number of waiting cars in queue
	 */
	int getQueueSize();

}
