package com.solucionamos.bmcmanager;

import com.solucionamos.bmcmanager.model.Sensor;
import com.solucionamos.bmcmanager.model.Server;

import java.util.List;

public class BMCResponse {

    public final static int TYPE_SENSOR = 0;
    public final static int TYPE_PWSTATE = 1;
    public final static int TYPE_SERVER = 2;

    public int type = -1;
    private List<Sensor> sensors;
    private int pwState;
    private Server server;
    private int colorIndex;

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        type = TYPE_SENSOR;
        this.sensors = sensors;
    }

    public int getPwState() {
        return pwState;
    }

    public void setPwState(int i) {
        type = TYPE_PWSTATE;
        this.pwState = i;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        type = TYPE_SERVER;
        this.server = server;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

}
