package de.tum.mw.ftm.deefs.events;


/**
 * Basic Event class used to control the simulation flow.
 * Events are naturally ordered by their scheduled time beginning with the earliest event.
 *
 * @author Michael Wittmann
 */
public class Event implements Comparable<Event> {

	private long scheduledTime; // time the event should be triggered im ms

	/**
	 * New instance of Event
	 *
	 * @param scheduledTime time the event should be triggered in ms
	 */
	public Event(long scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	/**
	 * @return time the event is scheduled in ms
	 */
	public long getScheduledTime() {
		return scheduledTime;
	}

	/**
	 * Set the time the event should be triggered
	 *
	 * @param scheduledTime time the event should be triggered in ms
	 */
	public void setScheduledTime(long scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	@Override
	public int compareTo(Event o) {
		return Long.compare(this.scheduledTime, o.scheduledTime);
	}

	@Override
	public String toString() {
		return Long.toString(scheduledTime);
	}
}
