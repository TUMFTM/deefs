package de.tum.mw.ftm.deefs.demand;

import de.tum.mw.ftm.deefs.events.DemandEvent;
import de.tum.mw.ftm.deefs.location.Position;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to import the given demand list an convert customer rides to demand events.
 *
 * @author Michael Wittmann
 */
public class DemandList {

	/**
	 * Converts the given CSV-File into a list of demand events
	 *
	 * @param filepath to the demand definition CSV-File
	 * @return list of demand events
	 */
	public static List<DemandEvent> getEventList(String filepath) {
		Reader in;
		try {
			in = new FileReader(filepath);

			List<DemandEvent> events = new ArrayList<>();

			Iterable<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in);
			for (CSVRecord record : records) {
				//calulate time in ms
				int scheduled_time = (Integer.parseInt(record.get("day")) - 1) * 24 * 3600 + (Integer.parseInt(record.get("hour")) - 1) * 3600 + (Integer.parseInt(record.get("minute")) - 1) * 60;
				DemandEvent a = new DemandEvent(Integer.parseInt(record.get("track_id")),
						scheduled_time * 1000,
						new Position(Double.parseDouble(record.get("start_y")), Double.parseDouble(record.get("start_x"))),
						new Position(Double.parseDouble(record.get("stop_y")), Double.parseDouble(record.get("stop_x"))),
						Integer.parseInt(record.get("distance")),
						//convert to ms
						Integer.parseInt(record.get("duration")) * 1000);
				events.add(a);
			}
			return events;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
