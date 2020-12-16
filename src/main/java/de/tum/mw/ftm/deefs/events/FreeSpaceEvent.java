package de.tum.mw.ftm.deefs.events;

/**
 * Special Event, triggered by a charging station if, a used charging spot gets empty.
 *
 * @author Michael Wittmann
 * @see Event
 */
public class FreeSpaceEvent extends Event {

    public FreeSpaceEvent(long scheduledTime) {
        super(scheduledTime);
    }

}
