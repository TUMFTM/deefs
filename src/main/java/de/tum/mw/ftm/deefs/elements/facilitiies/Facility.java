package de.tum.mw.ftm.deefs.elements.facilitiies;

import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.elements.PhysicalElement;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;
import de.tum.mw.ftm.deefs.location.Position;

/**
 * This class represents a base class for all Facilities a Car can connect with.
 * Each Facility has a physical position, a scenario it operates in, and a capacity that defines how many cars can connect to the facility at the same time.
 *
 * @author Michael Wittmann
 */
public abstract class Facility extends PhysicalElement {

    protected final String ACTION_CHECKIN = "CHECKIN";
    protected final String ACTION_CHECKOUT = "CHECKOUT";
    protected final String ACTION_CHECKIN_DENIED = "CHECKIN_DENIED";
    protected int capacity;

    /**
     * Basic constructor
     *
     * @param id       the facility's id
     * @param scenario the scenario the facility operates in
     * @param position physical position of this facility
     * @param capacity number of cars, that can be connected at the same time
     */
    public Facility(int id, Scenario scenario, Position position, int capacity) {
        super(id, scenario, position);
        this.capacity = capacity;
    }

    /**
     * Checks if the facility has free space left.
     *
     * @return true if there are less cars connected to the facility than the maximum capacity, false otherwise
     */
    public abstract boolean hasSpace();


    /**
     * Use this method to check in a car to this facility.
     *
     * @param car  car to be checked in
     * @param time check in time
     * @return <b>true</b> if the car was successfully connected to the facility, <b>false</b> otherwise
     */
    public abstract boolean checkInCar(Taxi car, long time);

    /**
     * Use this method to check out a car from this facility.
     *
     * @param car  car to be checked out
     * @param time check out time
     * @return <b>true</b> if the car was successfully disconnected from the facility, <b>false</b> otherwise
     */
    public abstract boolean checkOutCar(Taxi car, long time);


    /**
     * Calculates the remaining Space at this facility.
     *
     * @return number of free spaces at this facility
     */
    public abstract int getRemainingSpace();


    /**
     * Gives the maximum number of cars that can be connected to this facility at the same time
     *
     * @return maximum number of cars thah can be connected at the same time
     */
    public int getCapacity() {
        return this.capacity;
    }
}
