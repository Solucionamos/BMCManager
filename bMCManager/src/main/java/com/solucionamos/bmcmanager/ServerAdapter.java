package com.solucionamos.bmcmanager;

import android.content.Context;

import android.content.res.Resources;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ServerAdapter extends ArrayAdapter<Server> implements
        AsyncResponse<BMCResponse> {
    private final List<View> viewList;
    private final List<Server> objects;
    private final List<AsyncTask> tasks;
    private final Context context;
    private HashMap<Integer, Integer> mapStatusColor;

    public ServerAdapter(Context context, List<Server> objects) {
        super(context, R.layout.server_list_item, objects);

        this.objects = objects;
        viewList = new ArrayList<>();
        tasks = new ArrayList<>();
        mapStatusColor = new HashMap<>();
        mapStatusColor.put(Server.STATUS_NORMAL, R.color.green);
        mapStatusColor.put(Server.STATUS_WARNING, R.color.orange);
        mapStatusColor.put(Server.STATUS_CRITICAL, R.color.red);

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
                    inflater.inflate(R.layout.server_list_item, parent, false));
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

    private void paintServer(Server server, int color) {
        Resources res = getContext().getResources();
        viewList.get(server.getPosition())
                .findViewById(R.id.statusColor)
                .setBackgroundResource(color);
        viewList.get(server.getPosition()).setTag(
                res.getColor(color));
    }

    @Override
    public void processFinish(BMCResponse response, Exception ex) {
        if (ex != null) {
            paintServer(response.getServer(), mapStatusColor.get(Server.STATUS_CRITICAL));
        } else {
            if (response.getPwState() == Server.PWSTATE_OFF) {
                paintServer(response.getServer(), R.color.grey);
            } else {
                paintServer(response.getServer(), mapStatusColor.get(response.getColorIndex()));
            }
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
            ArrayList<Integer> colorIndexes = new ArrayList<>();
            int pwState;
            List<Sensor> sensors;
            Server server = args[0];
            BMCResponse response = new BMCResponse();
            response.setServer(server);
            try {
                server.connect();

                sensors = server.getFans();
                colorIndexes.add(goThroughList(sensors));

                sensors = server.getTemperatures();
                colorIndexes.add(goThroughList(sensors));

                sensors = server.getVoltages();
                colorIndexes.add(goThroughList(sensors));

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
            response.setColorIndex(Collections.max(colorIndexes));
            return response;
        }

        private int goThroughList(List<Sensor> sensors) {
            List<String> statusList = new ArrayList<>();
            for (Sensor s: sensors) {
                statusList.add(s.getStatus());
            }

            if (statusList.contains("Critical")) {
                return Server.STATUS_CRITICAL;
            } else if (statusList.contains("Warning")) {
                return Server.STATUS_WARNING;
            } else {
                return Server.STATUS_NORMAL;
            }
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
