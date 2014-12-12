package com.solucionamos.bmcmanager;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.bmcmanager.R;
import com.solucionamos.bmcmanager.model.Sensor;
import com.solucionamos.bmcmanager.model.Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerAdapter extends ArrayAdapter<Server> implements
        AsyncResponse<BMCResponse> {
    private final List<View> viewList;
    private final List<Server> objects;
    private final List<AsyncTask> tasks;
    private final Context context;

    public ServerAdapter(Context context, List<Server> objects) {
        super(context, R.layout.serverlistitem, objects);
        this.objects = objects;
        viewList = new ArrayList<>();
        tasks = new ArrayList<>();
        this.context = context;
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        viewList.add(position, convertView);
        // view = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (viewList.get(position) == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewList.set(position,
                    inflater.inflate(R.layout.serverlistitem, parent, false));
        }

		/*
         * Recall that the variable position is sent in as an argument to this
		 * method. The variable simply refers to the position of the current
		 * object in the list. (The ArrayAdapter iterates through the list we
		 * sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
        Server s = objects.get(position);

        if (s != null) {
            s.setPosition(position);
            System.out.println("SERVER " + s.getPosition());
            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView sName = (TextView) viewList.get(position).findViewById(
                    R.id.itemName);
            TextView sDesc = (TextView) viewList.get(position).findViewById(
                    R.id.itemAddress);

            // check to see if each individual textview is null.
            // if not, assign some text!
            if (sName != null) {
                sName.setText(s.getName());
            }
            if (sDesc != null) {
                sDesc.setText(s.getAddress());
            }

            RetrieveSensorsTask asyncTask = new RetrieveSensorsTask();
            asyncTask.delegate = this;
            asyncTask.execute(s);
            tasks.add(asyncTask);

        }
        // the view must be returned to our activity
        return viewList.get(position);

    }

    @Override
    public void processFinish(BMCResponse response, Exception ex) {
        if (ex == null) {
            if (response.getPwState() == Server.PWSTATE_OFF) {
                viewList.get(response.getServer().getPosition())
                        .setBackgroundResource(R.color.background_grey);
                viewList.get(response.getServer().getPosition()).setTag(
                        R.color.background_grey);
            } else if (response.getPwState() == Server.PWSTATE_ON) {
                if (response.getColorIndex() == 0) {
                    viewList.get(response.getServer().getPosition())
                            .setBackgroundResource(R.color.background_green);
                    viewList.get(response.getServer().getPosition()).setTag(
                            R.color.background_green);
                } else if (response.getColorIndex() == 1) {
                    viewList.get(response.getServer().getPosition())
                            .setBackgroundResource(R.color.background_orange);
                    viewList.get(response.getServer().getPosition()).setTag(
                            R.color.background_orange);
                } else if (response.getColorIndex() == 2) {
                    viewList.get(response.getServer().getPosition())
                            .setBackgroundResource(R.color.background_red);
                    viewList.get(response.getServer().getPosition()).setTag(
                            R.color.background_red);
                }
            }
        } else {
            viewList.get(response.getServer().getPosition())
                    .setBackgroundResource(R.color.background_grey);
            viewList.get(response.getServer().getPosition()).setTag(
                    R.color.background_grey);
        }

        if (tasks.isEmpty()) {
            ServerListActivity listActivity = (ServerListActivity) context;
            ServerListFragment listFragment = ((ServerListFragment) listActivity.getFragmentManager()
                    .findFragmentById(R.id.item_list));
            if (listFragment != null) {
                listFragment.stopRefreshing();
            }
        }
    }

    private class RetrieveSensorsTask extends
            AsyncTask<Server, Void, BMCResponse> {
        public AsyncResponse<BMCResponse> delegate = null;
        private Exception ex = null;
        private boolean isRunning = true;

        @Override
        protected BMCResponse doInBackground(Server... args) {
            Integer colorIndex = 0;
            int pwState;
            List<Sensor> sensors;
            Server server = args[0];
            BMCResponse response = new BMCResponse();
            response.setServer(server);
            try {
                server.connect();
                Integer colorAux;

                sensors = server.getFans();
                colorAux = goThroughList(sensors.iterator());
                if (colorAux > colorIndex) {
                    colorIndex = colorAux;
                }

                sensors = server.getTemperatures();
                colorAux = goThroughList(sensors.iterator());
                if (colorAux > colorIndex) {
                    colorIndex = colorAux;
                }

                sensors = server.getVoltages();
                colorAux = goThroughList(sensors.iterator());
                if (colorAux > colorIndex) {
                    colorIndex = colorAux;
                }

                pwState = server.getPwState();
                response.setPwState(pwState);

            } catch (Exception e) {
                ex = e;
                e.printStackTrace();
            }
            try {
                server.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            response.setColorIndex(colorIndex);
            return response;
        }

        private int goThroughList(Iterator<Sensor> itSensor) {
            int colorIndex = 0;
            while (itSensor.hasNext()) {

                Sensor aSensor = itSensor.next();
                String sensorStatus = aSensor.getStatus();

                if (sensorStatus.equals("Warning")) {
                    if (colorIndex < 1) {
                        colorIndex = 1;
                    }
                } else if (sensorStatus.equals("Critical")) {
                    if (colorIndex < 2) {
                        colorIndex = 2;
                    }
                }

            }
            return colorIndex;
        }

        @Override
        protected void onCancelled() {
            isRunning = false;
        }

        @Override
        protected void onPostExecute(BMCResponse response) {
            if (isRunning) {
                tasks.remove(this);
                delegate.processFinish(response, ex);
            }
        }
    }

}
