package com.solucionamos.bmcmanager.model;

import java.util.List;

import com.solucionamos.bmcmanager.connection.*;

//It is just here representing a connection, for now, I think the name should be different. Like HTTPConnection implements ServerConnection or something.
public class Server {
	public static final int PWSTATE_OFF = 0;
	public static final int PWSTATE_ON = 1;
	public static final int PWSTATE_RESET = 2;
	public static final int PWSTATE_HRESET = 3;

	public static final String MODEL_IVB = "IVB";

	BmcConnectionInterface connection;

	private String name;
	private String model;
	private String protocol;
	private String address;
	private String username;
	private String password;
	private int listPosition;

	public Server(String protocol, String model, String name, String address,
			String username, String password) {

		if (protocol.equals("HTTPS") || protocol.equals("HTTP")) {
			if (model.equals(MODEL_IVB)) {
				this.connection = new IvbHttpsConnection(protocol, address,
						username, password);
			}
		}

		this.name = name;
		this.model = model;
		this.protocol = protocol;
		this.address = address;
		this.username = username;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public String getModel() {
		return model;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getAddress() {
		return this.address;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public List<Sensor> getFans() throws Exception {
		return this.connection.getSensors(Sensor.TYPE_FAN);
	}

	public List<Sensor> getTemperatures() throws Exception {
		return this.connection.getSensors(Sensor.TYPE_TEMPERATURE);
	}

	public List<Sensor> getVoltages() throws Exception {
		return this.connection.getSensors(Sensor.TYPE_VOLTAGE);
	}

	public int getPWState() throws Exception {
		return this.connection.getPwState();
	}

	public void setPWState(int a) throws Exception {
		this.connection.setPwState(a);
	}

	public void connect() throws Exception {
		this.connection.connect();
	}

	public void disconnect() throws Exception {
		this.connection.disconnect();
	}

	public void setPosition(int position) {
		this.listPosition = position;
	}

	public int getPosition() {
		return listPosition;
	}

	public int getPwState() throws Exception {
		return this.connection.getPwState();
	}
}
