package de.tum.mw.ftm.deefs;

import de.tum.mw.ftm.deefs.utils.Stopwatch;


/**
 * Main Class for taxi behavior model
 *
 * @author Michael Wittmann
 */
public class Main {

    public static void main(String[] args) {
        // Start simulation timer
        Stopwatch timer = new Stopwatch();

        // define a new simulation scenario
        Scenario scenario = new Scenario();

        // Initialize the scenario
        scenario.initialize();

        // let the scenario run
        scenario.run();

        // beep signalizes end of simulation
        java.awt.Toolkit.getDefaultToolkit().beep();

        // show time needed for simulation
        System.out.println("Finished " + timer.getTimeAsString());
    }


}
 