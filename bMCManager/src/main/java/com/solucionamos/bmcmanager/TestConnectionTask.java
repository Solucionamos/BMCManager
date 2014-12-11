package com.solucionamos.bmcmanager;


import android.os.AsyncTask;

import com.solucionamos.bmcmanager.model.Server;

public class TestConnectionTask extends AsyncTask<Server, Void, Void> {

    public AsyncResponse<BMCResponse> delegate = null;
    private BMCResponse response = null;
    private Exception ex;

    public TestConnectionTask() {
    }

    @Override
    protected Void doInBackground(Server... params) {
        response = new BMCResponse();

        ex = null;
        Server server = params[0];
        response.setServer(server);
        try {
            server.connect();
            response.setPwState(server.getPwState());
        } catch (Exception e) {
            e.printStackTrace();
            ex = e;
        }

        try {
            server.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        delegate.processFinish(response, ex);
    }
}
