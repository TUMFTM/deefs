package de.tum.mw.ftm.deefs.elements;


import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.comparators.TaxiLastLogOffComparator;
import de.tum.mw.ftm.deefs.comparators.TaxiLastLoginComparator;
import de.tum.mw.ftm.deefs.elements.taxi.Taxi;
import de.tum.mw.ftm.deefs.events.TaxiControlNewTargetCountEvent;
import de.tum.mw.ftm.deefs.log.ControllerStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;


/**
 * Controller regulating the number of active Taxis in the system.
 * Number of active taxis will follow the target value.
 * The controller tries to login taxis in order of their last log off and log of taxis in order to their first login.
 *
 * @author Michael Wittmann
 */
public class TaxiController {

	private final Scenario scenario;
	private int nTarget = 0;


	/**
	 * New instance of TaxiController
	 *
	 * @param scenario scenario the TaxiController should work on
	 */
	public TaxiController(Scenario scenario) {
		this.scenario = scenario;
	}


	/**
	 * Sets a new target value immediately. <b>Notice:<b> don't use future times in this method.
	 *
	 * @param nTarget new target value
	 * @param time    actual time
	 */
	public void setNTarget(int nTarget, long time) {
		if (nTarget > 0) {
			this.nTarget = nTarget;
		} else {
			throw new RuntimeException("nTarget must not be a negative value");
		}
		controlActiveTaxiCount(time);
	}


	/**
	 * When this method is called, the actual number of active taxis is compared to the target value.
	 * If desired the Controller logs on/off vehicles to reach the target value.
	 * There might be a delay between triggering a log off and the actual log off, because the car needs to reach his home position first.
	 * Log on and log off is done by ordering cars regarding their last login / last log off time
	 *
	 * @param time simulation time the count is checked in ms
	 */
	public void controlActiveTaxiCount(long time) {
		/* First check if there are taxis, which already exceeded_their active time, if so log them of.
		/ this is needed because sometimes taxis stuck at at taxi rank gettig no customer request.
		/ in fact no updates are done on this agent and he would never log off as long he gets a customer ride
		*/
		List<Taxi> exceeded_taxis = scenario.getAgency().getFree_taxis();
		exceeded_taxis = exceeded_taxis.stream().filter(c -> c.maxTimeActiveIsExceeded(time)).collect(Collectors.toList());
		for (Taxi taxi : exceeded_taxis) {
			taxi.triggerlogOff(time);
		}

		/*
		 * Next step, all active taxis are collected. If there is a difference between active taxis and the target value taxis will be logged on or logged off.
		 */
		PriorityQueue<Taxi> active_taxis = new PriorityQueue<>(new TaxiLastLoginComparator());
		active_taxis.addAll(scenario.getAgency().getActive_taxis());

		//Try to log on taxis
		if (nTarget > active_taxis.size()) {

			int delta = active_taxis.size() - nTarget;
			PriorityQueue<Taxi> inactive_taxis = new PriorityQueue<>(new TaxiLastLogOffComparator());
			inactive_taxis.addAll(scenario.getAgency().getInactive_taxis());
			while (inactive_taxis.size() > 0 && delta != 0) {
				if (inactive_taxis.poll().logOn(time)) {
					delta++;
				}
			}
		}

		//Try to log off taxis
		else if (nTarget < active_taxis.size()) {

			int delta = active_taxis.size() - nTarget;
			PriorityQueue<Taxi> free_taxis = new PriorityQueue<>(new TaxiLastLoginComparator());
			free_taxis.addAll(scenario.getAgency().getActive_taxis());
			while (free_taxis.size() > 0 && delta != 0) {
				if (free_taxis.poll().triggerlogOff(time)) {
					delta--;
				}
			}
		}
		//Log action in Database
		scenario.getDBLog().addControllerStats(new ControllerStats(time, ControllerStats.TYPE_VALUE, scenario.getAgency().getActive_taxis().size()));
	}


	/**
	 * This method reads the initial target vaules from controller definitoin csv-file.
	 * Control events will be created regarding the given time and target values
	 *
	 * @param filepath path to the controller definition csv-file
	 * @return <b>true</b> if initial event where read and added successfully to the event queue, <b>false</b> otherwise
	 */
	public boolean addInitialEvents(String filepath) {
		try {
			Reader in = new FileReader(filepath);
			Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';').withHeader().parse(in);
			for (CSVRecord record : records) {
				long scheduledTime = (Integer.parseInt(record.get("day")) - 1) * 24 * 3600 * 1000 + (Integer.parseInt(record.get("hour")) - 1) * 3600 * 1000;
				int n_soll = Integer.parseInt(record.get("n"));
				scenario.getDBLog().addControllerStats(new ControllerStats(scheduledTime, ControllerStats.TYPE_TARGET, n_soll));
				TaxiControlNewTargetCountEvent event = new TaxiControlNewTargetCountEvent(scheduledTime, n_soll);
				scenario.addEvent(event);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


}
