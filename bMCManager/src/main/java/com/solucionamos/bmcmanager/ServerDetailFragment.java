package com.solucionamos.bmcmanager;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bmcmanager.R;
import com.solucionamos.bmcmanager.ActionDialogFragment.ActionDialogInterface;
import com.solucionamos.bmcmanager.model.Sensor;
import com.solucionamos.bmcmanager.model.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ServerListActivity}
 * in two-pane mode (on tablets) or a {@link ServerDetailActivity}
 * on handsets.
 */
public class ServerDetailFragment extends Fragment
        implements AsyncResponse<BMCResponse>,
        SwipeRefreshLayout.OnRefreshListener,
        ActionDialogInterface {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Server serverItem = null;

    private LinearLayout temperatureBlock, powerBlock, fanBlock;
    private Switch powerSwitch;
    private DeleteDialogFragment delete_dialog;
    private ActionDialogFragment power_dialog;
    private ImageView colorBlock;
    private List<AsyncTask> tasks;
    private ServerDetailFragment thisRef;
    private TextView statusTxt;
    private View rootView = null;
    private SwipeRefreshLayout swipeLayout;
    private boolean isRefreshing;
    private HashMap<String, HashMap<String, Integer>> sensorIcon = new HashMap<>();

    private enum Severity {
        NORMAL,
        WARNING,
        CRITICAL
    }
    private Severity severity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ServerDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            DBHelper db = new DBHelper(this.getActivity());
            serverItem = db.getServer(getArguments().getString(ARG_ITEM_ID));

            delete_dialog = new DeleteDialogFragment();
            delete_dialog.setServer(this.serverItem);
            if (this.getActivity().getClass().equals(ServerDetailActivity.class)) {
                delete_dialog.setTablet(false);
            } else {
                delete_dialog.setTablet(true);
            }

            tasks = new ArrayList<>();
            power_dialog = new ActionDialogFragment();
            thisRef = this;
        }

        HashMap<String, Integer> temperatureIcon = new HashMap<>();
        temperatureIcon.put("Normal", R.drawable.ic_temperature_good);
        temperatureIcon.put("Warning", R.drawable.ic_temperature_warning);
        temperatureIcon.put("Critical", R.drawable.ic_temperature_bad);
        sensorIcon.put(Sensor.TYPE_TEMPERATURE, temperatureIcon);

        HashMap<String, Integer> powerIcon = new HashMap<>();
        powerIcon.put("Normal", R.drawable.ic_power_good);
        powerIcon.put("Warning", R.drawable.ic_power_warning);
        powerIcon.put("Critical", R.drawable.ic_power_bad);
        sensorIcon.put(Sensor.TYPE_VOLTAGE, powerIcon);

        HashMap<String, Integer> fanIcon = new HashMap<>();
        fanIcon.put("Normal", R.drawable.ic_fan_good);
        fanIcon.put("Warning", R.drawable.ic_fan_warning);
        fanIcon.put("Critical", R.drawable.ic_fan_bad);
        sensorIcon.put(Sensor.TYPE_FAN, fanIcon);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater
                .inflate(R.layout.server_details_fragment, container, false);

        if (serverItem == null) {
            return rootView;
        }

        swipeLayout = (SwipeRefreshLayout) rootView
                .findViewById(R.id.swipeContainer);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setRefreshing(true);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        ((TextView) rootView.findViewById(R.id.textServerName))
                .setText(serverItem.getName());
        ((TextView) rootView.findViewById(R.id.textServerDescription))
                .setText(serverItem.getAddress());

        powerSwitch = (Switch) rootView.findViewById(R.id.switchStatus);
        if (savedInstanceState != null) {
            powerSwitch.setChecked(savedInstanceState.getBoolean("switchChecked"));
        }

        powerSwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (!isRefreshing) {
                            Bundle bundle = new Bundle();
                            if (isChecked) {
                                bundle.putInt("operation", Server.PWSTATE_ON);
                            } else {
                                bundle.putInt("operation", Server.PWSTATE_OFF);
                            }
                            power_dialog.setArguments(bundle);
                            power_dialog.setTargetFragment(thisRef, 0);
                            power_dialog.show(getFragmentManager(), getTag());
                        }
                    }
                });

        rootView.findViewById(R.id.Options_Image)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopUp(v);
                    }
                });

        statusTxt = (TextView) rootView.findViewById(R.id.serverStatus);
        colorBlock = (ImageView) rootView.findViewById(R.id.detailStatusColor);
        colorBlock.setBackgroundColor(getResources().getColor(R.color.grey));

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("switchChecked", powerSwitch.isChecked());
        super.onSaveInstanceState(savedInstanceState);
    }

    private void getPwState() {
        GetPwStateTask asyncTask = new GetPwStateTask();
        asyncTask.delegate = this;
        tasks.add(asyncTask);
        asyncTask.execute();
    }

    private void removeChildrenFromBlock(LinearLayout block) {
        for (int i = block.getChildCount() - 1; i > 0; i--)
            block.removeViewAt(i);
    }

    public void refreshData() {
        powerSwitch.setEnabled(false);
        cancelTasks();

        if (powerBlock != null) {
            removeChildrenFromBlock(powerBlock);
        }
        if (temperatureBlock != null) {
            removeChildrenFromBlock(temperatureBlock);
        }
        if (fanBlock != null) {
            removeChildrenFromBlock(fanBlock);
        }

        getPwState();
        updateSensors();
    }

    private void updateSensors() {
        severity = Severity.NORMAL;

        RetrievePWStateTask switchTask = new RetrievePWStateTask();
        switchTask.delegate = this;
        switchTask.execute();
        tasks.add(switchTask);

        temperatureBlock = (LinearLayout) rootView.findViewById(R.id.temperatureBlock);
        RetrieveSensorsTask tempTask = new RetrieveSensorsTask();
        tempTask.delegate = this;
        tempTask.execute(Sensor.TYPE_TEMPERATURE);
        tasks.add(tempTask);

        fanBlock = (LinearLayout) rootView.findViewById(R.id.coolerBlock);
        RetrieveSensorsTask fanTask = new RetrieveSensorsTask();
        fanTask.delegate = this;
        fanTask.execute(Sensor.TYPE_FAN);
        tasks.add(fanTask);

        powerBlock = (LinearLayout) rootView.findViewById(R.id.powerBlock);
        RetrieveSensorsTask voltTask = new RetrieveSensorsTask();
        voltTask.delegate = this;
        voltTask.execute(Sensor.TYPE_VOLTAGE);
        tasks.add(voltTask);
    }

    void cancelTasks() {
        for (AsyncTask task : tasks) {
            task.cancel(true);
        }
        tasks.clear();
        swipeLayout.setRefreshing(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        setPowerSwitch(false);

        // Set title
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            if (this.getActivity().getClass().equals(ServerDetailActivity.class)) {
                actionBar.setTitle(R.string.action_titleServerDetails);
            } else {
                actionBar.setTitle(R.string.title_AppTitle);
            }
        }

        swipeLayout.setRefreshing(false);
        refreshData();
    }

    void showPopUp(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.server_details_options_popmenu, popup.getMenu());

        if (!powerSwitch.isChecked()) {
            popup.getMenu().removeItem(R.id.hard_reset_server);
            popup.getMenu().removeItem(R.id.reset_server);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Bundle bundle = new Bundle();
                switch (item.getItemId()) {
                    case R.id.delete_server:
                        delete_dialog.show(getFragmentManager(), getTag());
                        break;
                    case R.id.hard_reset_server:
                        bundle.putInt("operation", Server.PWSTATE_HRESET);
                        power_dialog.setArguments(bundle);
                        power_dialog.setTargetFragment(thisRef, 0);
                        power_dialog.show(getFragmentManager(), getTag());
                        break;
                    case R.id.reset_server:
                        bundle.putInt("operation", Server.PWSTATE_RESET);
                        power_dialog.setArguments(bundle);
                        power_dialog.setTargetFragment(thisRef, 0);
                        power_dialog.show(getFragmentManager(), getTag());
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    void showToast(String string) {
        Context context = getActivity().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, string, duration);
        toast.show();
    }

    @Override
    public void processFinish(BMCResponse response, Exception ex) {
        if (ex != null) {
            callbackServerUnreachable();
            response.type = -1;
        }

        switch (response.type) {
            case BMCResponse.TYPE_PWSTATE:
                if (response.getPwState() == Server.PWSTATE_ON) {
                    callbackServerPowerState(true);
                } else {
                    callbackServerPowerState(false);
                }
                break;

            case BMCResponse.TYPE_SENSOR:
                callBackSensors(response.getSensors());
                break;
        }

        if (tasks.isEmpty()) {
            swipeLayout.setRefreshing(false);
            powerSwitch.setEnabled(true);
        }
    }

    private void callbackServerUnreachable() {
        setPowerSwitch(false);

        showToast(getString(R.string.connection_no_success));
        statusTxt.setText(getString(R.string.server_status) + " "
                + getString(R.string.server_unreachable_status));
        showEmptyBlocks(true);
    }

    private void callbackServerPowerState(boolean poweredOn) {
        setPowerSwitch(poweredOn);

        if(!poweredOn) {
            showToast(getString(R.string.server_powered_off));
            statusTxt.setText(getString(R.string.server_status) + " "
                    + getString(R.string.server_off_status));
            colorBlock.setBackgroundColor(getResources().getColor(R.color.grey));
            showEmptyBlocks(false);
        }
    }

    private void callBackSensors(List<Sensor> sensors) {
        TextView sensorName;
        TextView value;
        ImageView iconChange;
        LinearLayout aBlock = null;

        int textName = 0;
        int textValue = 0;
        int layoutItem = 0;
        int iconItem = 0;
        int textDescId = 0;

        setPowerSwitch(true);
        statusTxt.setText(getString(R.string.server_status) + " "
                + getString(R.string.server_on_status));

        String sensorType;
        String sensorStatus;
        View view;
        LayoutInflater inflater = LayoutInflater.from(getActivity()
                .getBaseContext());

        for (Sensor sensor : sensors) {
            sensorType = sensor.getType();

            switch (sensorType) {
                case Sensor.TYPE_TEMPERATURE:
                    aBlock = temperatureBlock;
                    textName = R.id.textTemperature1;
                    textValue = R.id.textTemperatureNumber1;
                    layoutItem = R.layout.temperature_item;
                    iconItem = R.id.iconTemperature1;
                    textDescId = R.id.textTemperatureDesc1;
                    break;
                case Sensor.TYPE_VOLTAGE:
                    aBlock = powerBlock;
                    textName = R.id.textPower1;
                    textValue = R.id.textPowerNumber1;
                    layoutItem = R.layout.power_item;
                    iconItem = R.id.imageView12;
                    textDescId = R.id.textPowerDesc1;
                    break;
                case Sensor.TYPE_FAN:
                    aBlock = fanBlock;
                    textName = R.id.textFan1;
                    textValue = R.id.textFanNumber1;
                    layoutItem = R.layout.fan_item;
                    iconItem = R.id.imageView3;
                    textDescId = R.id.textFanDesc1;
                    break;
            }

            view = inflater.inflate(layoutItem, aBlock, false);
            sensorName = (TextView) view.findViewById(textName);
            sensorName.setText(sensor.getName());

            String reading = sensor.getReading();
            String unit = sensor.getUnits();

            value = (TextView) view.findViewById(textValue);
            if (unit != null) {
                if (unit.equals("C") || unit.equals("F")){
                    unit = "º" + unit;
                }
                value.setText(reading + " " + unit);
            } else {
                value.setText(reading);
            }

            TextView textDesc = (TextView) view.findViewById(textDescId);
            sensorStatus = sensor.getStatus();
            switch (sensorStatus) {
                case "Critical":
                    textDesc.setText(getString(R.string.status_critical));
                    if (severity.compareTo(Severity.CRITICAL) < 0) {
                        severity = Severity.CRITICAL;
                    }
                    break;
                case "Warning":
                    textDesc.setText(getString(R.string.status_warning));
                    if (severity.compareTo(Severity.WARNING) < 0)
                        severity = Severity.WARNING;
                    break;
                case "Normal":
                    textDesc.setText(getString(R.string.status_normal));
                    break;
            }

            iconChange = (ImageView) view.findViewById(iconItem);
            if (sensorIcon.get(sensorType).containsKey(sensorStatus)) {
                iconChange.setImageResource(sensorIcon.get(sensorType).get(sensorStatus));
            }

            aBlock.addView(view);
        }

        if (severity.compareTo(Severity.CRITICAL) == 0) {
            colorBlock.setBackgroundColor(getResources().getColor(R.color.background_red));
            statusTxt.setText(statusTxt.getText() + " - " + getString(R.string.status_critical));
        } else if (severity.compareTo(Severity.WARNING) == 0) {
            colorBlock.setBackgroundColor(getResources().getColor(R.color.background_orange));
            statusTxt.setText(statusTxt.getText() + " - " + getString(R.string.status_warning));
        } else {
            colorBlock.setBackgroundColor(getResources().getColor(R.color.background_green));
            statusTxt.setText(statusTxt.getText() + " - " + getString(R.string.status_normal));
        }
    }

    private void setPowerSwitch(boolean poweredOn) {
        isRefreshing = true;
        powerSwitch.setChecked(poweredOn);
        isRefreshing = false;
    }

    private void showEmptyBlocks(boolean unreachable) {
        TextView sensorName;
        TextView sensorLocal;
        TextView value;
        ImageView iconChange;
        LinearLayout aBlock;
        int textName;
        int textLocal;
        int textValue;
        int layoutItem;

        View view;
        LayoutInflater inflater = LayoutInflater.from(getActivity()
                .getBaseContext());

        cancelTasks();

        if (unreachable) {
            colorBlock.setBackgroundColor(getResources().getColor(R.color.background_red));
            removeChildrenFromBlock(temperatureBlock);
            removeChildrenFromBlock(powerBlock);
            removeChildrenFromBlock(fanBlock);
        }

        aBlock = temperatureBlock;
        textName = R.id.textTemperature1;
        textLocal = R.id.textTemperatureDesc1;
        textValue = R.id.textTemperatureNumber1;
        layoutItem = R.layout.temperature_item;

        view = inflater.inflate(layoutItem, aBlock, false);
        iconChange = (ImageView) view.findViewById(R.id.iconTemperature1);
        iconChange.setImageResource(R.drawable.ic_temperature_unreachable);
        sensorName = (TextView) view.findViewById(textName);
        sensorName.setText(getString(R.string.empty_block));
        sensorLocal = (TextView) view.findViewById(textLocal);
        sensorLocal.setText(getString(R.string.empty_value));
        value = (TextView) view.findViewById(textValue);
        value.setText(getString(R.string.empty_value));

        aBlock.addView(view);

        aBlock = powerBlock;
        textName = R.id.textPower1;
        textLocal = R.id.textPowerDesc1;
        textValue = R.id.textPowerNumber1;
        layoutItem = R.layout.power_item;

        view = inflater.inflate(layoutItem, aBlock, false);
        iconChange = (ImageView) view.findViewById(R.id.imageView12);
        iconChange.setImageResource(R.drawable.ic_power_unreachable);
        sensorName = (TextView) view.findViewById(textName);
        sensorName.setText(getString(R.string.empty_block));
        sensorLocal = (TextView) view.findViewById(textLocal);
        sensorLocal.setText(getString(R.string.empty_value));
        value = (TextView) view.findViewById(textValue);
        value.setText(getString(R.string.empty_value));

        aBlock.addView(view);

        aBlock = fanBlock;
        textName = R.id.textFan1;
        textLocal = R.id.textFanDesc1;
        textValue = R.id.textFanNumber1;
        layoutItem = R.layout.fan_item;

        view = inflater.inflate(layoutItem, aBlock, false);
        iconChange = (ImageView) view.findViewById(R.id.imageView3);
        iconChange.setImageResource(R.drawable.ic_fan_unreachable);
        sensorName = (TextView) view.findViewById(textName);
        sensorName.setText(getString(R.string.empty_block));
        sensorLocal = (TextView) view.findViewById(textLocal);
        sensorLocal.setText(getString(R.string.empty_value));
        value = (TextView) view.findViewById(textValue);
        value.setText(getString(R.string.empty_value));

        aBlock.addView(view);
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelTasks();
    }

    public void onRefresh() {
        refreshData();
    }

    @Override
    public void cancelPowerOperation(int op) {
        if (op == Server.PWSTATE_ON || op == Server.PWSTATE_OFF) {
            isRefreshing = true;
            powerSwitch.toggle();
            isRefreshing = false;
        }
    }

    @Override
    public void confirmPowerOperation(int op) {
        SetPWStateTask asyncTask = new SetPWStateTask();
        asyncTask.delegate = this;
        asyncTask.execute(op);
    }

    private class RetrieveSensorsTask extends AsyncTask<String, Void, BMCResponse> {
        public AsyncResponse<BMCResponse> delegate = null;
        private Exception ex = null;
        private boolean isRunning = true;

        @Override
        protected BMCResponse doInBackground(String... args) {
            BMCResponse response = new BMCResponse();
            List<Sensor> sensors = null;
            String type = args[0];
            try {
                serverItem.connect();
                switch (type) {
                    case Sensor.TYPE_FAN:
                        sensors = serverItem.getFans();
                        break;
                    case Sensor.TYPE_TEMPERATURE:
                        sensors = serverItem.getTemperatures();
                        break;
                    case Sensor.TYPE_VOLTAGE:
                        sensors = serverItem.getVoltages();
                        break;
                }
                response.setSensors(sensors);
            } catch (Exception e) {
                ex = e;
            }
            try {
                serverItem.disconnect();
            } catch (Exception e) {
                // ignore the exception
            }
            return response;
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

    private class GetPwStateTask extends AsyncTask<String, Void, BMCResponse> {
        public AsyncResponse<BMCResponse> delegate = null;
        private Exception ex = null;
        private boolean isRunning = true;

        @Override
        protected BMCResponse doInBackground(String... args) {
            BMCResponse response = new BMCResponse();
            try {
                serverItem.connect();
                int pwState = serverItem.getPwState();
                response.setPwState(pwState);
            } catch (Exception e) {
                ex = e;
            }
            try {
                serverItem.disconnect();
            } catch (Exception e) {
                // ignore the exception
            }
            return response;
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

    private class RetrievePWStateTask extends AsyncTask<String, Void, BMCResponse> {
        public AsyncResponse<BMCResponse> delegate = null;
        private Exception ex = null;
        private boolean isRunning = true;

        @Override
        protected BMCResponse doInBackground(String... params) {
            BMCResponse response = new BMCResponse();
            try {
                serverItem.connect();
                response.setPwState(serverItem.getPWState());
                serverItem.disconnect();
            } catch (Exception e) {
                ex = e;
            }
            return response;
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

    private class SetPWStateTask extends AsyncTask<Integer, Void, BMCResponse> {
        public AsyncResponse<BMCResponse> delegate = null;
        private Exception ex = null;
        private boolean isRunning = true;

        @Override
        protected BMCResponse doInBackground(Integer... params) {
            BMCResponse response = new BMCResponse();
            try {
                serverItem.connect();
                serverItem.setPWState(params[0]);
                response.setPwState(params[0]);
                serverItem.disconnect();
            } catch (Exception e) {
                ex = e;
            }
            return response;
        }

        @Override
        protected void onCancelled() {
            isRunning = false;
        }

        @Override
        protected void onPostExecute(BMCResponse param) {
            if (isRunning) {
                tasks.remove(this);
                delegate.processFinish(param, ex);
            }
        }
    }
}
