package com.solucionamos.bmcmanager;

import com.example.bmcmanager.R;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.solucionamos.bmcmanager.model.Server;

public class TestConnectionTask extends AsyncTask<Server, Void, Void> {

	private BMCResponse response = null;
	private Exception ex;
	public AsyncResponse<BMCResponse> delegate = null;

	public TestConnectionTask() {
	}

	@Override
	protected Void doInBackground(Server... params) {
		response = new BMCResponse();

		ex = null;
		Server serv = params[0];
		response.setServer(serv);
		try {
			serv.connect();
			response.setPwState(serv.getPwState());
		} catch (Exception e) {
			e.printStackTrace();
			ex = e;
		}

		try {
			serv.disconnect();
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
