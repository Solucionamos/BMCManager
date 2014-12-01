package com.solucionamos.bmcmanager;

import java.util.List;

import com.solucionamos.bmcmanager.model.*;

public class BMCResponse {

	public final static String TYPE_SENSOR = "SENSOR";
	public final static String TYPE_PWSTATE = "PWSTATE";
	public final static String TYPE_SERVER = "SERVER";
	
	public Object type = null;
	private List<Sensor> sensors;
	private int pwState;
	private Server server;
	private int colorIndex;

	public void setSensors(List<Sensor> sensors) {
		type = TYPE_SENSOR;
		this.sensors = sensors;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setPwState(int i) {
		type = TYPE_PWSTATE;
		this.pwState = i;
	}

	public int getPwState() {
		return pwState;
	}

	public void setServer(Server server) {
		type = TYPE_SERVER;
		this.server = server;
	}

	public Server getServer() {
		return server;
	}

	public int getColorIndex() {
		return colorIndex;
	}

	public void setColorIndex(int colorIndex) {
		this.colorIndex = colorIndex;
	}

}
