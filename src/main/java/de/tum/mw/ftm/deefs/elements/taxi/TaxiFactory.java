package de.tum.mw.ftm.deefs.elements.taxi;


import de.tum.mw.ftm.deefs.Scenario;
import de.tum.mw.ftm.deefs.elements.TaxiAgency;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.ChargingInterface;
import de.tum.mw.ftm.deefs.elements.eMobilityComponents.Connector;
import de.tum.mw.ftm.deefs.elements.evConcept.Battery;
import de.tum.mw.ftm.deefs.elements.evConcept.EVConcept;
import de.tum.mw.ftm.deefs.graphopper.extensions.MyGraphHopper;
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
 * FactoryClass for Taxis.
 * Creating instances of Taxi over this Factory ensures unique Taxi ids and that only a single instanace of GraphHopper is used.
 * There are also methods provided to read vehicle fleets automatically from XML-Files
 *
 * @author Michael Wittmann
 */
public class TaxiFactory {

	private final MyGraphHopper hopper;  //GraphHopper instance needed for route calculations
	private final AtomicInteger car_id;  //unique car id
	private final Scenario scenario;       //scenario the cars operate in
	private final TaxiAgency agency;       //taxi agency the car belongs to


	/**
	 * New Instance of TaxiFactory
	 *
	 * @param scenario Simulation scenario
	 * @param agency   TaxiAgency taxis should be connected to
	 */
	public TaxiFactory(Scenario scenario, TaxiAgency agency) {
		this.scenario = scenario;
		this.agency = agency;
		hopper = new MyGraphHopper();
		car_id = new AtomicInteger(1);
	}

	/**
	 * @param home Taxi's home position
	 * @return a new instance of ICETaxi, which is already placed in the scenario and connected to the taxiAgency
	 */
	public ICETaxi getICETaxi(Position home) {
		ICETaxi car = new ICETaxi(car_id.getAndIncrement(), home, hopper, scenario);
		agency.addCar(car);
		return car;
	}

	/**
	 * @param id   vehicle id, make sure that id's are unique!
	 * @param home taxi's home position
	 * @return a new instance of ICETaxi, which is already placed in the scenario and connected to the taxiAgency
	 */
	public ICETaxi getICETaxi(int id, Position home) {
		ICETaxi car = new ICETaxi(id, home, hopper, scenario);
		agency.addCar(car);
		return car;
	}


	/**
	 * @param id        vehicle id, make sure that id's are unique!
	 * @param home      taxi's home position
	 * @param evConcept taxis's evConcept
	 * @return a new instance of BEVTaxi, which is already placed in the scenario and connected to the taxiAgency
	 */
	public BEVTaxi getBEVTaxi(int id, Position home, EVConcept evConcept) {
		BEVTaxi car = new BEVTaxi(id, home, hopper, scenario, evConcept);
		agency.addCar(car);
		return car;
	}

	/**
	 * @param home      taxi's home position
	 * @param evConcept taxis's evConcept
	 * @return a new instance of BEVTaxi, which is already placed in the scenario and connected to the taxiAgency
	 */
	public BEVTaxi getBEVTaxi(Position home, EVConcept evConcept) {
		BEVTaxi car = new BEVTaxi(car_id.getAndIncrement(), home, hopper, scenario, evConcept);
		agency.addCar(car);
		return car;
	}


	/**
	 * Reads vehicle fleet automatically from XML-File
	 *
	 * @param filePath file Path to fleet definition XML-File
	 * @return List of taxis, according to the defined vehicle fleet in XML-File
	 */
	public List<Taxi> getTaxiFromXML(String filePath) {
		List<Taxi> taxis = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(filePath);

			Element docEle = dom.getDocumentElement();
			NodeList nl = docEle.getElementsByTagName(XMLParser.TAG_CAR);
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element e1 = (Element) nl.item(i);
					if (e1.getAttribute(XMLParser.TAG_TYPE).equals(XMLParser.ATTR_BEVTAXI)) {
						BEVTaxi taxi = getBEVTaxi(
								XMLParser.getInt(e1, XMLParser.TAG_ID),
								getHome(e1),
								getEVConcept(e1));
						taxis.add(taxi);
					} else if (e1.getAttribute(XMLParser.TAG_TYPE).equals(XMLParser.ATTR_ICETAXI)) {
						ICETaxi taxi = getICETaxi(
								XMLParser.getInt(e1, XMLParser.TAG_ID),
								getHome(e1));
						taxis.add(taxi);
					}
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException pce) {
			pce.printStackTrace();
		}
		return taxis;
	}


	private Position getHome(Element e1) {
		Element e = XMLParser.getElement(e1, XMLParser.TAG_HOME);
		return new Position(XMLParser.getDouble(e, XMLParser.TAG_LATITUDE),
				XMLParser.getDouble(e, XMLParser.TAG_LONGITUDE));

	}

	private EVConcept getEVConcept(Element e1) {
		Element e = XMLParser.getElement(e1, XMLParser.TAG_EVCONCEPT);
		return new EVConcept(
				getBattery(e),
				getChargingInterface(e),
				XMLParser.getFloat(e, XMLParser.TAG_E_MEAN),
				XMLParser.getString(e, XMLParser.TAG_NAME));
	}

	private Battery getBattery(Element e1) {
		Element e = XMLParser.getElement(e1, XMLParser.TAG_BATTERY);
		return new Battery(
				XMLParser.getFloat(e, XMLParser.TAG_E_BAT_MAX),
				XMLParser.getFloat(e, XMLParser.TAG_U_BAT),
				XMLParser.getFloat(e, XMLParser.TAG_U_BAT_CELL_N),
				XMLParser.getFloat(e, XMLParser.TAG_U_BAT_CELL_LS),
				XMLParser.getFloat(e, XMLParser.TAG_ETA_L));
	}

	private ChargingInterface getChargingInterface(Element e1) {
		Element e = XMLParser.getElement(e1, XMLParser.TAG_CHARGINGINTERFACE);
		NodeList nl = e.getElementsByTagName(XMLParser.TAG_CONNECTOR);
		if (nl != null && nl.getLength() > 0) {
			ChargingInterface chargingInterface = new ChargingInterface();
			for (int i = 0; i < nl.getLength(); i++) {
				Connector connector = getConnector((Element) nl.item(i));
				chargingInterface.addConnector(connector);
			}
			return chargingInterface;
		} else {
			throw new RuntimeException("Error parsing fleet xml. Can't build a charging interface without connectors");
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
				throw new RuntimeException("Error parsing fleet xml connector type unknown");
		}
	}

}
