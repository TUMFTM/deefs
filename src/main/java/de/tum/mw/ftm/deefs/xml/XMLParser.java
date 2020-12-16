package de.tum.mw.ftm.deefs.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLParser {

	//XML TAGS
	public static final String TAG_FACILITIES = "FACILITIES";
	public static final String TAG_FACILITY = "FACILITY";
	public static final String TAG_ID = "ID";
	public static final String TAG_LATITUDE = "LATITUDE";
	public static final String TAG_LONGITUDE = "LONGITUDE";
	public static final String TAG_AREA = "AREA";
	public static final String TAG_CAPACITY = "CAPACITY";
	public static final String TAG_ADDRESS = "ADDRESS";
	public static final String TAG_DESCRIPTION = "DESCRIPTION";
	public static final String TAG_PRIORITY = "PRIORITY";
	public static final String TAG_DEMAND1 = "DEMAND1";
	public static final String TAG_DEMAND2 = "DEMAND2";
	public static final String TAG_DEMAND3 = "DEMAND3";
	public static final String TAG_DEMAND4 = "DEMAND4";
	public static final String ATTR_RANK = "RANK";
	public static final String ATTR_CHARGINGSTATION = "CHARGINGSTATION";
	public static final String TAG_CHARGINGPOINTS = "CHARGINGPOINTS";
	public static final String TAG_CHARGINGPOINT = "CHARGINGPOINT";
	public static final String TAG_CONNECTOR = "CONNECTOR";
	public static final String TAG_TYPE = "TYPE";
	public static final String TAG_PMAX = "PMAX";
	public static final String TAG_FLEET = "FLEET";
	public static final String TAG_CAR = "CAR";
	public static final String TAG_HOME = "HOME";
	public static final String ATTR_ICETAXI = "ICETAXI";
	public static final String ATTR_BEVTAXI = "BEVTAXI";
	public static final String TAG_EVCONCEPT = "EVCONCEPT";
	public static final String TAG_NAME = "NAME";
	public static final String TAG_E_MEAN = "E_MEAN";
	public static final String TAG_BATTERY = "BATTERY";
	public static final String TAG_E_BAT_MAX = "E_BAT_MAX";
	public static final String TAG_U_BAT = "U_BAT";
	public static final String TAG_U_BAT_CELL_N = "U_BAT_CELL_N";
	public static final String TAG_U_BAT_CELL_LS = "U_BAT_CELL_LS";
	public static final String TAG_ETA_L = "ETA_L";
	public static final String TAG_CHARGINGINTERFACE = "CHARGINGINTERFACE";


	/**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is 'name' I will return John
	 */
	public static String getString(Element ele, String tagName) {
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			return el.getFirstChild().getNodeValue();
		} else {
			throw new RuntimeException("Element " + tagName + " not foud");
		}
	}

	public static Element getElement(Element ele, String tagName) {
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			return (Element) nl.item(0);

		} else {
			throw new RuntimeException("Element " + tagName + " not foud");
		}

	}


	/**
	 * Calls getTextValue and returns a int value
	 */
	public static int getInt(Element ele, String tagName) {
		return Integer.parseInt(getString(ele, tagName));
	}

	/**
	 * Calls getTextValue and returns a double value
	 */
	public static double getDouble(Element ele, String tagName) {
		return Double.parseDouble(getString(ele, tagName));
	}

	/**
	 * Calls getTextValue and returns a double value
	 */
	public static float getFloat(Element ele, String tagName) {
		return Float.parseFloat(getString(ele, tagName));
	}

}
