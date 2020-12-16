package de.tum.mw.ftm.deefs.utils;

import java.util.Date;

/**
 * Helper Class to measure times
 *
 * @author Michael Wittmann
 */
public class Stopwatch {

    private Date begin; //start time


    /**
     * Initializes and starts a new stopwatch
     */
    public Stopwatch() {
        start();
    }


    /**
     * starts or restarts the stopwatch
     */
    public void start() {
        this.begin = new Date();
    }


    /**
     * @return elapsed time in milliseconds since stopwatch was started. Can be called multiple times as a stopwatch always refers to the time it was started the last time
     */
    public long getTimeInMillies() {
        Date now = new Date();
        return now.getTime() - begin.getTime();
    }

    /**
     * @return elapsed time as a String since stopwatch was started.
     * Can be called multiple times as a stopwatch always refers to the time it was started the last time
     */
    public String getTimeAsString() {
        Date now = new Date();
        return StringUtils.formatMilliseconds(now.getTime() - begin.getTime());
    }


}
