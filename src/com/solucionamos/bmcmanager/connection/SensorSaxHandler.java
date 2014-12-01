package com.solucionamos.bmcmanager.connection;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.solucionamos.bmcmanager.model.Sensor;

public class SensorSaxHandler extends DefaultHandler {
	private List<Sensor> sensors = new ArrayList<Sensor>();
	private Sensor lastSensor = null;
	private String content = null;

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (localName.equals("sensor")) {
			lastSensor = new Sensor();
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals("sensorStatus")) {
			lastSensor.setStatus(content);
		} else if (localName.equals("name")) {
			lastSensor.setName(content);
		} else if (localName.equals("reading")) {
			lastSensor.setReading(content);
		} else if (localName.equals("units")) {
			lastSensor.setUnits(content);
		} else if (localName.equals("lowerNC")) {
			lastSensor.setLowerNC(content);
		} else if (localName.equals("upperNC")) {
			lastSensor.setUpperNC(content);
		} else if (localName.equals("lowerCT")) {
			lastSensor.setLowerCT(content);
		} else if (localName.equals("upperCT")) {
			lastSensor.setUpperCT(content);
		} else if (localName.equals("lowerNR")) {
			lastSensor.setLowerNR(content);
		} else if (localName.equals("upperNR")) {
			lastSensor.setUpperNR(content);
		} else if (localName.equals("sensor")) {
			sensors.add(lastSensor);
		}
	}

	public void characters(char[] ch, int start, int length) {
		content = String.copyValueOf(ch, start, length);
	}

}
