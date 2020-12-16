package de.tum.mw.ftm.deefs.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Helper Class offering static methods to convert content from csv to xml format
 * <br> fleet and facility definition files
 *
 * @author Michael Wittmann
 */
public class CSVtoXML {


	/**
	 * Converts a csv-File containing taxiRank information into XML
	 *
	 * @param path_in  Path to CSV-File
	 * @param path_out Path for XML-File
	 */
	public static void parseRanksFromCSVtoXML(String path_in, String path_out) {
		try {
			Reader in = new FileReader(path_in);
			File file_out = new File(path_out);
			Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';').withHeader().parse(in);
			file_out.createNewFile();
			FileWriter writer = new FileWriter(file_out);
			for (CSVRecord record : records) {
				writer.append("<FACILITY TYPE = \"RANK\">\n");
				writer.append(String.format("<ID>%s</ID>\n", record.get("id")));
				writer.append(String.format("<LATITUDE>%s</LATITUDE>\n", record.get("latitude")));
				writer.append(String.format("<LONGITUDE>%s</LONGITUDE>\n", record.get("longitude")));
				writer.append(String.format("<AREA>%s</AREA>\n", record.get("area")));
				writer.append(String.format("<CAPACITY>%s</CAPACITY>\n", record.get("capacity")));
				writer.append(String.format("<ADDRESS>%s</ADDRESS>\n", record.get("address")));
				writer.append(String.format("<DESCRIPTION>%s</DESCRIPTION>\n", record.get("description")));
				writer.append(String.format("<DEMAND1>%s</DEMAND1>\n", record.get("demand_21_03")));
				writer.append(String.format("<DEMAND2>%s</DEMAND2>\n", record.get("demand_03_09")));
				writer.append(String.format("<DEMAND3>%s</DEMAND3>\n", record.get("demand_09_15")));
				writer.append(String.format("<DEMAND4>%s</DEMAND4>\n", record.get("demand_15_21")));
				writer.append("</FACILITY>\n");
			}
			writer.close();
			in.close();
		} catch (IOException e) {
			System.err.println("Error while trying to write Trackfile");
		}
	}


	/**
	 * Converts a csv-File containing ChargingStation information into XML
	 *
	 * @param stations_path_in Path to CSV-File
	 * @param path_out         Path for XML-File
	 */
	public static void parseChargingStationsfromCSVtoXML(String stations_path_in, String chargingpoints_path_in, String path_out) {
		try {
			Reader in = new FileReader(stations_path_in);
			Reader in2 = new FileReader(chargingpoints_path_in);
			File file_out = new File(path_out);
			Iterable<CSVRecord> stations = CSVFormat.EXCEL.withDelimiter(';').withHeader().parse(in);
			file_out.createNewFile();
			FileWriter writer = new FileWriter(file_out);
			Iterable<CSVRecord> chargingPoints = CSVFormat.EXCEL.withDelimiter(';').withHeader().parse(in2);
			List<CSVRecord> chargingPoints2 = new ArrayList<>();
			for (CSVRecord cp : chargingPoints) {
				chargingPoints2.add(cp);
			}
			writer.append("<FACILITIES>\n");
			for (CSVRecord record : stations) {
				String id = record.get("id");

				writer.append("<FACILITY TYPE = \"CHARGINGSTATION\">\n");
				writer.append(String.format("<ID>%s</ID>\n", record.get("id")));
				writer.append(String.format("<LATITUDE>%s</LATITUDE>\n", record.get("latitude")));
				writer.append(String.format("<LONGITUDE>%s</LONGITUDE>\n", record.get("longitude")));
				writer.append("<CHARGINGPOINTS>\n");
				for (CSVRecord cp : chargingPoints2) {
					if (cp.get("idChargingStation").equals(id)) {
						writer.append("<CHARGINGPOINT>\n");
						if (Float.parseFloat(cp.get("p_max_schuko")) > 0) {
							writer.append("<CONNECTOR>\n");
							writer.append("<TYPE>SCHUKO</TYPE>\n");
							writer.append(String.format("<PMAX>%s</PMAX>\n", cp.get("p_max_schuko")));
							writer.append("</CONNECTOR>\n");
						}
						if (Float.parseFloat(cp.get("p_max_typ2")) > 0) {
							writer.append("<CONNECTOR>\n");
							writer.append("<TYPE>TYP2</TYPE>\n");
							writer.append(String.format("<PMAX>%s</PMAX>\n", cp.get("p_max_typ2")));
							writer.append("</CONNECTOR>\n");
						}
						if (Float.parseFloat(cp.get("p_max_ccs")) > 0) {
							writer.append("<CONNECTOR>\n");
							writer.append("<TYPE>CCS</TYPE>\n");
							writer.append(String.format("<PMAX>%s</PMAX>\n", cp.get("p_max_ccs")));
							writer.append("</CONNECTOR>\n");
						}
						if (Float.parseFloat(cp.get("p_max_chademo")) > 0) {
							writer.append("<CONNECTOR>\n");
							writer.append("<TYPE>CHADEMO</TYPE>\n");
							writer.append(String.format("<PMAX>%s</PMAX>\n", cp.get("p_max_chademo")));
							writer.append("</CONNECTOR>\n");
						}
						if (Float.parseFloat(cp.get("p_max_super_charger")) > 0) {
							writer.append("<CONNECTOR>\n");
							writer.append("<TYPE>SUPERCHARGER</TYPE>\n");
							writer.append(String.format("<PMAX>%s</PMAX>\n", cp.get("p_max_super_charger")));
							writer.append("</CONNECTOR>\n");
						}
						writer.append("</CHARGINGPOINT>\n");
					}
				}
				writer.append("</CHARGINGPOINTS>\n");
				writer.append("</FACILITY>\n");

			}
			writer.append("</FACILITIES>\n");
			writer.close();
			in.close();
		} catch (IOException e) {
			System.err.println("Error while trying to write Trackfile");
		}


	}

	/**
	 * Converts a csv-File containing fleet information into XML
	 *
	 * @param path_in  Path to CSV-File
	 * @param path_out Path for XML-File
	 */
	public static void parseFleet(String path_in, String path_out) {
		AtomicInteger id = new AtomicInteger(0);
		try {
			Reader in = new FileReader(path_in);
			File file_out = new File(path_out);
			Iterable<CSVRecord> records = CSVFormat.EXCEL.withDelimiter(';').withHeader().parse(in);
			file_out.createNewFile();
			FileWriter writer = new FileWriter(file_out);
			writer.append("<?xml version=\"1.0\"?>\n");
			writer.append("<FLEET>\n");
			for (CSVRecord record : records) {
				int n = Integer.parseInt(record.get("n"));
				switch (record.get("type")) {
					case "BEVTAXI":
						for (int i = 0; i < n; i++) {
							writer.append("<CAR TYPE = \"BEVTAXI\">\n");
							writer.append(String.format("<ID>%d</ID>\n", id.incrementAndGet()));
							writer.append("<HOME>\n");
							writer.append(String.format("<LATITUDE>%s</LATITUDE>\n", record.get("home_lat")));
							writer.append(String.format("<LONGITUDE>%s</LONGITUDE>\n", record.get("home_lon")));
							writer.append("</HOME>\n");
							writer.append("<EVCONCEPT>\n");
							writer.append(String.format("<NAME>%s</NAME>\n", record.get("ev_concept_name")));
							writer.append(String.format("<E_MEAN>%s</E_MEAN>\n", record.get("e_mean")));
							writer.append("<BATTERY>\n");
							writer.append(String.format("<E_BAT_MAX>%s</E_BAT_MAX>\n", record.get("e_bat_max")));
							writer.append(String.format("<U_BAT>%s</U_BAT>\n", record.get("u_bat")));
							writer.append(String.format("<U_BAT_CELL_N>%s</U_BAT_CELL_N>\n", record.get("u_bat_cell_n")));
							writer.append(String.format("<U_BAT_CELL_LS>%s</U_BAT_CELL_LS>\n", record.get("u_bat_cell_ls")));
							writer.append(String.format("<ETA_L>%s</ETA_L>\n", record.get("eta_l")));
							writer.append("</BATTERY>\n");
							writer.append("<CHARGINGINTERFACE>\n");
							if (Float.parseFloat(record.get("p_max_schuko")) > 0) {
								writer.append("<CONNECTOR>\n");
								writer.append("<TYPE>SCHUKO</TYPE>\n");
								writer.append(String.format("<PMAX>%s</PMAX>\n", record.get("p_max_schuko")));
								writer.append("</CONNECTOR>\n");
							}
							if (Float.parseFloat(record.get("p_max_typ2")) > 0) {
								writer.append("<CONNECTOR>\n");
								writer.append("<TYPE>TYP2</TYPE>\n");
								writer.append(String.format("<PMAX>%s</PMAX>\n", record.get("p_max_typ2")));
								writer.append("</CONNECTOR>\n");
							}
							if (Float.parseFloat(record.get("p_max_ccs")) > 0) {
								writer.append("<CONNECTOR>\n");
								writer.append("<TYPE>CCS</TYPE>\n");
								writer.append(String.format("<PMAX>%s</PMAX>\n", record.get("p_max_ccs")));
								writer.append("</CONNECTOR>\n");
							}
							if (Float.parseFloat(record.get("p_max_chademo")) > 0) {
								writer.append("<CONNECTOR>\n");
								writer.append("<TYPE>CHADEMO</TYPE>\n");
								writer.append(String.format("<PMAX>%s</PMAX>\n", record.get("p_max_chademo")));
								writer.append("</CONNECTOR>\n");
							}
							if (Float.parseFloat(record.get("p_max_supercharger")) > 0) {
								writer.append("<CONNECTOR>\n");
								writer.append("<TYPE>SUPERCHARGER</TYPE>\n");
								writer.append(String.format("<PMAX>%s</PMAX>\n", record.get("p_max_supercharger")));
								writer.append("</CONNECTOR>\n");
							}
							writer.append("</CHARGINGINTERFACE>\n");
							writer.append("</EVCONCEPT>\n");
							writer.append("</CAR>\n");
						}
						break;
					case "ICETAXI":
						for (int i = 0; i < n; i++) {
							writer.append("<CAR TYPE = \"ICETAXI\">\n");
							writer.append(String.format("<ID>%d</ID>\n", id.incrementAndGet()));
							writer.append("<HOME>\n");
							writer.append(String.format("<LATITUDE>%s</LATITUDE>\n", record.get("home_lat")));
							writer.append(String.format("<LONGITUDE>%s</LONGITUDE>\n", record.get("home_lon")));
							writer.append("</HOME>\n");
							writer.append("</CAR>\n");
						}

						break;
					default:
						break;
				}
			}
			writer.append("</FLEET>\n");
			writer.close();
			in.close();
		} catch (IOException e) {
			System.err.println("Error while trying to write FleetFile");
		}
	}
}
