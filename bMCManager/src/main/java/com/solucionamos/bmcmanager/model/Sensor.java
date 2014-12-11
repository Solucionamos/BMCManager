package com.solucionamos.bmcmanager.model;

public class Sensor {

    public static final String TYPE_FAN = "fan";
    public static final String TYPE_VOLTAGE = "voltage";
    public static final String TYPE_TEMPERATURE = "temperature";

    private String type;
    private String status;
    private String name;
    private String reading;
    private String units;
    private String lowerNC;
    private String upperNC;
    private String lowerCT;
    private String upperCT;
    private String lowerNR;
    private String upperNR;

    public Sensor() {
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

    public void setType(String type) {
        this.type = type;
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

    public void setLowerNC(String lowerNC) {
        this.lowerNC = lowerNC;
    }

    public void setUpperNC(String upperNC) {
        this.upperNC = upperNC;
    }

    public void setLowerCT(String lowerCT) {
        this.lowerCT = lowerCT;
    }

    public void setUpperCT(String upperCT) {
        this.upperCT = upperCT;
    }

    public void setLowerNR(String lowerNR) {
        this.lowerNR = lowerNR;
    }

    public void setUpperNR(String upperNR) {
        this.upperNR = upperNR;
    }

}
