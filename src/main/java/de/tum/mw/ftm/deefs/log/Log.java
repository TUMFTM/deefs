package de.tum.mw.ftm.deefs.log;

import de.tum.mw.ftm.deefs.elements.facilitiies.TaxiRank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Deprecated
public class Log {
	//TODO: Read from config File
	public static String FOLDER_OUTPUT_TRACKS = "output/tracks";
	public static String FOLDER_OUTPUT_RANKS = "output/ranks";
	public static String FOLDER_OUTPUT_CONTROLLER = "output/controller";

	//TODO: Read from config File
	private static String getOutputFolderTracks() {
		return FOLDER_OUTPUT_TRACKS;
	}

	private static String getOutputFolderRanks() {
		return FOLDER_OUTPUT_RANKS;
	}

	@SuppressWarnings("unused")
	private static String getOutputFolderController() {
		return FOLDER_OUTPUT_CONTROLLER;
	}

	public static void appendtoFile(String filepath, String message) {
		File logFile = new File(filepath);
		try {
			FileWriter writer = new FileWriter(logFile, true);
			writer.append(message);
			writer.close();
		} catch (IOException e) {
			System.err.println("Error while trying to write Rankfile");
		}
	}

	public static void saveTrackpoints(int car_id, List<Trackpoint> tps) {
		String[] headers = {"track_id", "time_ms", "time_formatted", "status", "facility_id", "lat", "lon", "distance", "soc"};

		String filename = String.format("car%d.csv", car_id);
		File logDir = new File(getOutputFolderTracks());

		logDir.setWritable(true);
		if (!logDir.exists()) {
			try {
				logDir.mkdir();
			} catch (SecurityException se) {

			}
		}
		File logFile = new File(logDir, filename);
		try {
			if (!logFile.exists()) {
				logFile.createNewFile();
				FileWriter writer = new FileWriter(logFile);
				for (int i = 0; i < headers.length; i++) {
					if (i < headers.length - 1) {
						writer.append(headers[i]);
						writer.append(",");
					} else {
						writer.append(headers[i]);
						writer.append("\n");
					}
				}
				writer.close();
			}

			FileWriter writer = new FileWriter(logFile, true);
			for (Trackpoint tp : tps) {
				writer.append(tp.getCSVString());
			}
			writer.close();

		} catch (IOException e) {
			System.err.println("Error while trying to write Trackfile");
		}
	}

	public static void rankStats(TaxiRank rank, String message) {

		String filename = String.format("rank%d.csv", rank.getId());
		File logDir = new File(getOutputFolderRanks());

		logDir.setWritable(true);
		if (!logDir.exists()) {
			try {
				logDir.mkdir();
			} catch (SecurityException se) {

			}
		}
		File logFile = new File(logDir, filename);
		try {

			FileWriter writer = new FileWriter(logFile, true);
			writer.append(message);
			writer.close();

		} catch (IOException e) {
			System.err.println("Error while trying to write Rankfile");
		}
	}

	public static void controllerStats(long time, int count) {
		String filename = "controller.csv";
		File logDir = new File(FOLDER_OUTPUT_CONTROLLER);
		String message = String.format("%d;%d\n", time, count);
		logDir.setWritable(true);
		if (!logDir.exists()) {
			try {
				logDir.mkdir();
			} catch (SecurityException se) {

			}
		}
		File logFile = new File(logDir, filename);
		try {

			FileWriter writer = new FileWriter(logFile, true);
			writer.append(message);
			writer.close();

		} catch (IOException e) {
			System.err.println("Error while trying to write Rankfile");
		}

	}
}
