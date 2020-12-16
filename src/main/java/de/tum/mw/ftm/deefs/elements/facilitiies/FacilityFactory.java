package de.tum.mw.ftm.deefs.elements.facilitiies;


import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingPoint;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.Connector;
import de.tum.mw.ftm.deefs.location.Position;
import de.tum.mw.ftm.deefs.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FactoryClass for Facilities. Reads facilities from input file and places them into the scenario.
 *
 * @author Michael Wittmann
 */
public class FacilityFactory {
	private final Scenario scenario;    //scenario the facilities should be placed in

	@SuppressWarnings("unused")
	private final AtomicInteger id;    // unique facility id couter

	public FacilityFactory(Scenario scenario) {
		this.scenario = scenario;
		id = new AtomicInteger(1);
	}

	/**
	 * Reads facilities automatically from XML-File.
	 *
	 * @param filePath file Path to facility definition XML-File
	 * @return List of facilities, according to the defined facility in XML-File
	 */
	public FacilityList getFacilitiesFromXML(String filePath) {
		//get the factory
		List<Facility> facilities = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(filePath);

			Element docEle = dom.getDocumentElement();
			NodeList nl = docEle.getElementsByTagName(XMLParser.TAG_FACILITY);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element e1 = (Element) nl.item(i);
					if (e1.getAttribute(XMLParser.TAG_TYPE).equals(XMLParser.ATTR_RANK)) {
						TaxiRank rank = getRank(e1);
						facilities.add(rank);
					} else if (e1.getAttribute(XMLParser.TAG_TYPE).equals(XMLParser.ATTR_CHARGINGSTATION)) {
						ChargingStation chargingStation = getChargingStation(e1);
						facilities.add(chargingStation);
					}
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException pce) {
			pce.printStackTrace();
		}
		return new FacilityList(facilities);
	}

	private ChargingStation getChargingStation(Element e) {
		NodeList nl = e.getElementsByTagName(XMLParser.TAG_CHARGINGPOINT);
		List<ChargingPoint> chargingPoints = new ArrayList<>();
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				ChargingPoint chargingPoint = getChargingPoint((Element) nl.item(i));
				chargingPoints.add(chargingPoint);
			}
		}
		return new ChargingStation(XMLParser.getInt(e, XMLParser.TAG_ID),
				new Position(XMLParser.getDouble(e, XMLParser.TAG_LATITUDE), XMLParser.getDouble(e, XMLParser.TAG_LONGITUDE)),
				chargingPoints,
				scenario);
	}

	private ChargingPoint getChargingPoint(Element e) {
		NodeList nl = e.getElementsByTagName(XMLParser.TAG_CONNECTOR);
		if (nl != null && nl.getLength() > 0) {
			ChargingInterface chargingInterface = new ChargingInterface();
			for (int i = 0; i < nl.getLength(); i++) {
				Connector connector = getConnector((Element) nl.item(i));
				chargingInterface.addConnector(connector);
			}
			return new ChargingPoint(chargingInterface, scenario);
		} else {
			throw new RuntimeException("Error parsing facility xml. Can't build a charging point without connectors");
		}
	}

	private Connector getConnector(Element e) {
		String type = XMLParser.getString(e, XMLParser.TAG_TYPE);
		switch (type) {
			case "SCHUKO":
				return new Connector(Connector.TYPE_SCHUKO, XMLParser.getFloat(e, XMLParser.TAG_PMAX));
			case "TYP2":
				return new Connector(Connector.TYPE_TYPE2, XMLParser.getFloat(e, XMLParser.TAG_PMAX));
			case "CCS":
				return new Connector(Connector.TYPE_CCS, XMLParser.getFloat(e, XMLParser.TAG_PMAX));
			case "CHADEMO":
				return new Connector(Connector.TYPE_CHADEMO, XMLParser.getFloat(e, XMLParser.TAG_PMAX));
			case "SUPERCHARGER":
				return new Connector(Connector.TYPE_SUPERCHARGER, XMLParser.getFloat(e, XMLParser.TAG_PMAX));
			default:
				throw new RuntimeException("Error parsing facility xml connector type unknown");
		}
	}

	private TaxiRank getRank(Element e) {
		return new TaxiRank(XMLParser.getInt(e, XMLParser.TAG_ID),
				new Position(XMLParser.getDouble(e, XMLParser.TAG_LONGITUDE), XMLParser.getDouble(e, XMLParser.TAG_LATITUDE), XMLParser.getInt(e, XMLParser.TAG_AREA)),
				scenario,
				XMLParser.getInt(e, XMLParser.TAG_CAPACITY),
				TaxiRank.getPriority("PRIORITY_STANDARD"),
//				TaxiRank.getPriority(XMLParser.getString(e,XMLParser.TAG_PRIORITY)), 
				XMLParser.getString(e, XMLParser.TAG_ADDRESS),
				XMLParser.getString(e, XMLParser.TAG_DESCRIPTION),
				XMLParser.getFloat(e, XMLParser.TAG_DEMAND1),
				XMLParser.getFloat(e, XMLParser.TAG_DEMAND2),
				XMLParser.getFloat(e, XMLParser.TAG_DEMAND3),
				XMLParser.getFloat(e, XMLParser.TAG_DEMAND4));
	}

	@Deprecated
	public ChargingStation getTestChargingStation(int id, int nChargingPoints, Position position) {
		List<ChargingPoint> chargingPoints = new ArrayList<>();
		for (int i = 0; i < nChargingPoints; i++) {
			ChargingPoint chargingPoint = new ChargingPoint(new ChargingInterface(2800, 7400, 50000, 0, 0), scenario);
			chargingPoints.add(chargingPoint);
		}
		return new ChargingStation(id, position, chargingPoints, scenario);

	}

	@Deprecated
	public FacilityList getTestFacilities() {
		List<Facility> facilities = new ArrayList<>();
		TaxiRank rank = new TaxiRank(1, new Position(48.138977, 11.565011), scenario, 10, TaxiRank.PRIORITY_STANDARD);
		facilities.add(rank);
		ChargingPoint chargingPoint = new ChargingPoint(new ChargingInterface(2800, 7400, 50000, 0, 0), scenario);
		List<ChargingPoint> chargingPoints = new ArrayList<>();
		chargingPoints.add(chargingPoint);

		ChargingStation chargingStation = new ChargingStation(2, new Position(48.142399, 11.556377), chargingPoints, scenario);
		facilities.add(chargingStation);
//		rank = new TaxiRank(2, new Position(48.142399, 11.556377), 1, null);
//		ranks.put(rank.id,rank);
//		rank = new TaxiRank(3, new Position(48.153870, 11.533134), 3, null);
//		ranks.put(rank.id, rank);
//		rank = new TaxiRank(4, new Position(48.158277, 11.511351), 1, null);
//		ranks.put(rank.id, rank);
		return new FacilityList(facilities);
	}
}

