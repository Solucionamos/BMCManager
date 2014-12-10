package com.solucionamos.bmcmanager.connection;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.solucionamos.bmcmanager.model.Sensor;

public class SensorSaxHandler extends DefaultHandler {
	private final List<Sensor> sensors = new ArrayList<>();
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
        switch (localName) {
            case "sensorStatus":
                lastSensor.setStatus(content);
                break;
            case "name":
                lastSensor.setName(content);
                break;
            case "reading":
                lastSensor.setReading(content);
                break;
            case "units":
                lastSensor.setUnits(content);
                break;
            case "lowerNC":
                lastSensor.setLowerNC(content);
                break;
            case "upperNC":
                lastSensor.setUpperNC(content);
                break;
            case "lowerCT":
                lastSensor.setLowerCT(content);
                break;
            case "upperCT":
                lastSensor.setUpperCT(content);
                break;
            case "lowerNR":
                lastSensor.setLowerNR(content);
                break;
            case "upperNR":
                lastSensor.setUpperNR(content);
                break;
            case "sensor":
                sensors.add(lastSensor);
                break;
        }
	}

	public void characters(char[] ch, int start, int length) {
		content = String.copyValueOf(ch, start, length);
	}

}
