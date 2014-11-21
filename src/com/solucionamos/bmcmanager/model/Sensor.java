package com.solucionamos.bmcmanager.model;

public class Sensor {

	public static final String TYPE_FAN = "fan";
	public static final String TYPE_VOLTAGE = "voltage";
	public static final String TYPE_TEMPERATURE = "temperature";

	public static final int OK = 1;
	public static final int WARNING = 2;
	public static final int CRITICAL = 3;
	public static final int NONRCV = 4;

	String type;
	String status;
	String name;
	String reading;
	String units;
	String lowerNC;
	String upperNC;
	String lowerCT;
	String upperCT;
	String lowerNR;
	String upperNR;

	public Sensor() {
	}

	public Sensor(String type, String status, String name, String reading,
			String units, String lowerNC, String upperNC, String lowerCT,
			String upperCT, String lowerNR, String upperNR) {
		this.type = type;
		this.status = status;
		this.name = name;
		this.reading = reading;
		this.units = units;
		this.lowerNC = lowerNC;
		this.upperNC = upperNC;
		this.lowerCT = lowerCT;
		this.upperCT = upperCT;
		this.lowerNR = lowerNR;
		this.upperNR = upperNR;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReading() {
		return reading;
	}

	public void setReading(String reading) {
		this.reading = reading;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getLowerNC() {
		return lowerNC;
	}

	public void setLowerNC(String lowerNC) {
		this.lowerNC = lowerNC;
	}

	public String getUpperNC() {
		return upperNC;
	}

	public void setUpperNC(String upperNC) {
		this.upperNC = upperNC;
	}

	public String getLowerCT() {
		return lowerCT;
	}

	public void setLowerCT(String lowerCT) {
		this.lowerCT = lowerCT;
	}

	public String getUpperCT() {
		return upperCT;
	}

	public void setUpperCT(String upperCT) {
		this.upperCT = upperCT;
	}

	public String getLowerNR() {
		return lowerNR;
	}

	public void setLowerNR(String lowerNR) {
		this.lowerNR = lowerNR;
	}

	public String getUpperNR() {
		return upperNR;
	}

	public void setUpperNR(String upperNR) {
		this.upperNR = upperNR;
	}

	public void setType(String type) {
		this.type = type;
	}

}
