package de.tum.mw.ftm.deefs.utils;


/**
 * Helper Class to display an easy comandline progressbar
 *
 * @author Michael Wittmann
 */
public class ProgressBar {
	private final int max;            // maximum value
	private int value;            // current value
	private float progress;        // progess
	private int actProgress;


	/**
	 * Initialize Progessbar with maximum value
	 *
	 * @param max 100%-value
	 */
	public ProgressBar(int max) {
		this.max = max;
		this.value = 0;
		this.actProgress = 0;
		this.progress = 0;
	}


	/**
	 * Increments the current value one step up
	 *
	 * @return current progess
	 */
	public float incrementProgress() {
		value++;
		return updateProggress(value, true);
	}


	/**
	 * updates the progess to the given value
	 *
	 * @param value new value to be set for progress
	 * @param show  progessbar will be printed to console if <b>true</b>
	 * @return
	 */
	public float updateProggress(int value, boolean show) {
		progress = value / (float) max * 100.0f;
		int intPg = (int) (progress);
		if (show && (intPg > actProgress)) {
			actProgress = intPg;
			printProgress();
		}
		return progress;
	}


	/**
	 * Prints progessbar to console by overwriting the last line (Does not work in eclipse console)
	 */
	public void printProgress() {
		StringBuilder bar = new StringBuilder("[");

		for (int i = 0; i < 50; i++) {
			if (i < (progress / 2)) {
				bar.append("=");
			} else if (i == (progress / 2)) {
				bar.append(">");
			} else {
				bar.append(" ");
			}
		}
		bar.append("]   ").append(StringUtils.parseDoubleOneDigit(progress)).append("%     ");
		System.out.print("\r" + bar.toString());
	}


}
