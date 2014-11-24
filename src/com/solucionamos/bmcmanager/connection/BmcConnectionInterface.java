package com.solucionamos.bmcmanager.connection;

import java.util.List;

import com.solucionamos.bmcmanager.model.Sensor;

public interface BmcConnectionInterface {

	public void connect() throws Exception;

	public void disconnect() throws Exception;

	public List<Sensor> getSensors(String type) throws Exception;

	public int getPwState() throws Exception;

	public void setPwState(int state) throws Exception;

}
