package com.solucionamos.bmcmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bmcmanager.R;
import com.solucionamos.bmcmanager.ActionDialogFragment.ActionDialogInterface;
import com.solucionamos.bmcmanager.model.Sensor;
import com.solucionamos.bmcmanager.model.Server;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ServerListActivity}
 * in two-pane mode (on tablets) or a {@link ServerDetailActivity}
 * on handsets.
 */
public class ServerDetailFragment extends Fragment implements 
	AsyncResponse<BMCResponse>, SwipeRefreshLayout.OnRefreshListener, ActionDialogInterface{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_POSITION = "position";

    /**
     * The dummy content this fragment is presenting.
     */
    private Server serverItem = null;

    
    private LinearLayout tempBlock, voltBlock, fanBlock;
	private Switch powerSwitch;
	private Button imgBtn;
	private DeleteDialogFragment ddialog;
	private ActionDialogFragment pdialog;
	private RelativeLayout colorBlock;
	private List<AsyncTask> tasks;
	private boolean checkedBefore;
	private ServerDetailFragment thisRef;
	private TextView statusTxt;
	private View rootView = null;
	private SwipeRefreshLayout swipeLayout;
	private boolean isRefreshing;
	private int colorFlag;
    
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
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
        	DBHelper mydb = new DBHelper(this.getActivity());
            serverItem = mydb.getServer(getArguments().getString(ARG_ITEM_ID));
            
            ddialog = new DeleteDialogFragment();
    		ddialog.setServer(this.serverItem);
    		
    		if(this.getActivity().getClass().equals(ServerDetailActivity.class)){
    			ddialog.setTablet(false);
    		}else{
    			ddialog.setTablet(true);
    		}
    		
    		tasks = new ArrayList<AsyncTask>();

    		pdialog = new ActionDialogFragment();
    		thisRef = this;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.serverdetails_fragment, container, false);
        
        // Show the dummy content as text in a TextView.
        if (serverItem != null) {
         
        	swipeLayout = (SwipeRefreshLayout) rootView
    				.findViewById(R.id.swipeContainer);
    		swipeLayout.setOnRefreshListener(this);
    		swipeLayout.setRefreshing(true);
    		swipeLayout.setColorSchemeResources(android.R.color.holo_blue_light,
    				android.R.color.holo_green_light,
    				android.R.color.holo_orange_light,
    				android.R.color.holo_red_light);
        	
            ((TextView) rootView.findViewById(R.id.textServerName)).setText(serverItem.getName());

         	((TextView) rootView.findViewById(R.id.textServerDescription)).setText(serverItem.getAddress());
         	
         	powerSwitch = (Switch) rootView.findViewById(R.id.switchStatus);
         	powerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 				public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (!isRefreshing) {
							Bundle bundle = new Bundle();
							if (isChecked) {
								checkedBefore = !isChecked;
								bundle.putInt("operation",
										Server.PWSTATE_ON);

							} else {
								checkedBefore = true;
								bundle.putInt("operation",
										Server.PWSTATE_OFF);
							}
							pdialog.setArguments(bundle);
							pdialog.setTargetFragment(thisRef, 0);
							pdialog.show(getFragmentManager(), getTag());
						}
					}
				});

         	
         	
         	((Button) rootView.findViewById(R.id.Options_Image)).setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				showPopUp(v);
    			}
    		});
         	
         	statusTxt = (TextView) rootView.findViewById(R.id.serverStatus);
         	colorBlock = (RelativeLayout) rootView.findViewById(R.id.relativeHeader);
         	colorBlock.setBackgroundResource(R.color.background_green);
         	
         	
        }

        return rootView;
    }
    
    private class RetrieveSensorsTask extends AsyncTask<String, Void, BMCResponse> {
    	private Exception ex = null;
    	public AsyncResponse<BMCResponse> delegate = null;
    	private boolean isrunning = true;

    	@Override
    	protected BMCResponse doInBackground(String... args) {
    		BMCResponse response = new BMCResponse();
    		List<Sensor> sensors = null;
    		String type = args[0];
    		try {
    			serverItem.connect();
    			if (type.equals(Sensor.TYPE_FAN)) {
    				sensors = serverItem.getFans();
    			} else if (type.equals(Sensor.TYPE_TEMPERATURE)) {
    				sensors = serverItem.getTemperatures();
    			} else if (type.equals(Sensor.TYPE_VOLTAGE)) {
    				sensors = serverItem.getVoltages();
    			}
    			response.setSensors(sensors);
    		} catch (Exception e) {
    			ex = e;
    			e.printStackTrace();
    		}
    		try {
    			serverItem.disconnect();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		return response;
    	}

    	@Override
    	protected void onCancelled() {
    		isrunning = false;
    	}

    	@Override
    	protected void onPostExecute(BMCResponse response) {
    		if (isrunning) {
    			tasks.remove(this);
    			delegate.processFinish(response, ex);
    		}
    	}
    }

    private class GetPwStateTask extends AsyncTask<String, Void, BMCResponse> {
    	private Exception ex = null;
    	public AsyncResponse<BMCResponse> delegate = null;
    	private boolean isrunning = true;

    	@Override
		protected BMCResponse doInBackground(String... args) {
    		BMCResponse response = new BMCResponse();
    		try {
    			serverItem.connect();
    			int pwState = serverItem.getPwState();
    			response.setPwState(pwState);
    		} catch (Exception e) {
    			ex = e;
    			e.printStackTrace();
    		}
    		try {
    			serverItem.disconnect();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		return response;
    	}

    	@Override
    	protected void onCancelled() {
    		isrunning = false;
    	}

    	@Override
    	protected void onPostExecute(BMCResponse response) {
    		if (isrunning) {
    			tasks.remove(this);
    			delegate.processFinish(response, ex);
    		}
    	}
    }

    private class RetrievePWStateTask extends AsyncTask<String, Void, BMCResponse> {
    	private Exception ex = null;
    	public AsyncResponse<BMCResponse> delegate = null;
    	private boolean isrunning = true;

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
    		isrunning = false;
    	}

    	@Override
    	protected void onPostExecute(BMCResponse response) {
    		if (isrunning) {
    			tasks.remove(this);
    			delegate.processFinish(response, ex);
    		}
    	}
    }

    private class SetPWStateTask extends AsyncTask<Integer, Void, BMCResponse> {
    	private Exception ex = null;
    	public AsyncResponse<BMCResponse> delegate = null;
    	private boolean isrunning = true;

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
    		isrunning = false;
    	}

    	@Override
    	protected void onPostExecute(BMCResponse param) {
    		if (isrunning) {
    			tasks.remove(this);
    			delegate.processFinish(param, ex);
    		}
    	}
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

		if (voltBlock != null) {
			removeChildrenFromBlock(voltBlock);
		}
		if (tempBlock != null) {
			removeChildrenFromBlock(tempBlock);
		}
		if (fanBlock != null) {
			removeChildrenFromBlock(fanBlock);
		}

		getPwState();
		updateSensors();
	}

	private void updateSensors() {

		colorFlag = 0;

		RetrievePWStateTask switchTask = new RetrievePWStateTask();
		switchTask.delegate = this;
		switchTask.execute();
		tasks.add(switchTask);

		tempBlock = (LinearLayout) rootView.findViewById(R.id.temperatureBlock);
		RetrieveSensorsTask tempTask = new RetrieveSensorsTask();
		tempTask.delegate = this;
		tempTask.execute(Sensor.TYPE_TEMPERATURE);
		tasks.add(tempTask);

		fanBlock = (LinearLayout) rootView.findViewById(R.id.coolerBlock);
		RetrieveSensorsTask fanTask = new RetrieveSensorsTask();
		fanTask.delegate = this;
		fanTask.execute(Sensor.TYPE_FAN);
		tasks.add(fanTask);

		voltBlock = (LinearLayout) rootView.findViewById(R.id.powerBlock);
		RetrieveSensorsTask voltTask = new RetrieveSensorsTask();
		voltTask.delegate = this;
		voltTask.execute(Sensor.TYPE_VOLTAGE);
		tasks.add(voltTask);
	}

	public void cancelTasks() {
		for (AsyncTask task : tasks) {
			task.cancel(true);
		}
		tasks.clear();
		swipeLayout.setRefreshing(false);
	}

	

	@Override
	public void onResume() {
		super.onResume();
		// Set title
		
		if(this.getActivity().getClass().equals(ServerDetailActivity.class)){
			getActivity().getActionBar().setTitle(
					R.string.action_titleServerDetails);
		}else{
			getActivity().getActionBar().setTitle(
					R.string.title_AppTitle);
		}
		swipeLayout.setRefreshing(false);
		refreshData();
	}

	public void showPopUp(View v) {
		PopupMenu popup = new PopupMenu(getActivity(), v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.serverdetails_options_popmenu, popup.getMenu());
		
		if (!powerSwitch.isChecked()) {
			popup.getMenu().removeItem(R.id.hreset_server);
			popup.getMenu().removeItem(R.id.reset_server);
		}
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				Bundle bundle = new Bundle();
				switch (item.getItemId()) {
				case R.id.delete_server:
					ddialog.show(getFragmentManager(), getTag());
					break;
				case R.id.hreset_server:
					bundle.putInt("operation", Server.PWSTATE_HRESET);
					pdialog.setArguments(bundle);
					pdialog.setTargetFragment(thisRef, 0);
					pdialog.show(getFragmentManager(), getTag());
					break;
				case R.id.reset_server:
					bundle.putInt("operation", Server.PWSTATE_RESET);
					pdialog.setArguments(bundle);
					pdialog.setTargetFragment(thisRef, 0);
					pdialog.show(getFragmentManager(), getTag());
					break;
				}
				return true;
			}
		});
		popup.show();
	}

	

	public void showToast(String string) {
		Context context = getActivity().getApplicationContext();
		CharSequence text = string;
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();

		colorBlock.setBackgroundResource(R.color.background_red);
	}

	@Override
	public void processFinish(BMCResponse response, Exception ex) {
		TextView sensorName = null;
		TextView value = null;
		ImageView iconChange = null;
		LinearLayout aBlock = null;

		List<Sensor> sensors = null;
		int pwState = -1;
		if(ex == null){
			if (response.type.equals(BMCResponse.TYPE_SENSOR)) {
				sensors = response.getSensors();
			} else if (response.type.equals(BMCResponse.TYPE_PWSTATE)) {
				pwState = response.getPwState();
			}
		}
		

		int textName = 0;
		int textValue = 0;
		int layoutItem = 0;
		int iconItem = 0;
		
		if (ex != null) {
			showToast(getString(R.string.connection_nosuccess));
			statusTxt.setText(getString(R.string.server_status) + " "
					+ getString(R.string.server_unreachable_status));
			showEmptyBlocks();
		} else if (pwState == Server.PWSTATE_OFF) {
			isRefreshing = true;
			powerSwitch.setChecked(false);
			isRefreshing = false;
			showToast(getString(R.string.server_powered_off));
			statusTxt.setText(getString(R.string.server_status) + " "
					+ getString(R.string.server_off_status));
			showEmptyBlocks();
		} else if (sensors != null) {
			isRefreshing = true;
			powerSwitch.setChecked(true);
			isRefreshing = false;
			statusTxt.setText(getString(R.string.server_status) + " "
					+ getString(R.string.server_on_status));
			String sensorType = null;
			String sensorStatus = null;
			LayoutInflater inflater = LayoutInflater.from(getActivity()
					.getBaseContext());

			Sensor aSensor = null;
			Iterator<Sensor> it = sensors.iterator();
			// colorFlag = 0;
			while (it.hasNext()) {
				aSensor = it.next();
				sensorType = aSensor.getType();
				sensorStatus = aSensor.getStatus();
				View view = null;

				if (sensorType.equals(Sensor.TYPE_TEMPERATURE)) {
					aBlock = tempBlock;
					textName = R.id.textTemperature1;
					textValue = R.id.textTemperatureNumber1;
					layoutItem = R.layout.temperatureitem;
					iconItem = R.id.iconTemperature1;
				} else if (sensorType.equals(Sensor.TYPE_VOLTAGE)) {
					aBlock = voltBlock;
					textName = R.id.textPower1;
					textValue = R.id.textPowerNumber1;
					layoutItem = R.layout.voltageitem;
					iconItem = R.id.imageView12;
				} else if (sensorType.equals(Sensor.TYPE_FAN)) {
					aBlock = fanBlock;
					textName = R.id.textFan1;
					textValue = R.id.textFanNumber1;
					layoutItem = R.layout.fanitem;
					iconItem = R.id.imageView3;
				}

				view = inflater.inflate(layoutItem, aBlock, false);
				sensorName = (TextView) view.findViewById(textName);
				sensorName.setText(aSensor.getName());
				value = (TextView) view.findViewById(textValue);
				value.setText(aSensor.getReading());

				iconChange = (ImageView) view.findViewById(iconItem);

				if (sensorType.equals(Sensor.TYPE_TEMPERATURE)) {
					if (sensorStatus.equals("Normal")) {
						iconChange
								.setImageResource(R.drawable.ic_temperature_good);
					} else if (sensorStatus.equals("Warning")) {
						if (colorFlag < 1) {
							colorFlag = 1;
						}
						iconChange
								.setImageResource(R.drawable.ic_temperature_warning);
					} else if (sensorStatus.equals("Critical")) {
						if (colorFlag < 2) {
							colorFlag = 2;
						}
						iconChange
								.setImageResource(R.drawable.ic_temperature_bad);
					}
				} else if (sensorType.equals(Sensor.TYPE_VOLTAGE)) {
					if (sensorStatus.equals("Normal")) {
						iconChange.setImageResource(R.drawable.ic_power_good);
					} else if (sensorStatus.equals("Warning")) {
						if (colorFlag < 1) {
							colorFlag = 1;
						}
						iconChange
								.setImageResource(R.drawable.ic_power_warning);
					} else if (sensorStatus.equals("Critical")) {
						if (colorFlag < 2) {
							colorFlag = 2;
						}
						iconChange.setImageResource(R.drawable.ic_power_bad);
					}
				} else if (sensorType.equals(Sensor.TYPE_FAN)) {
					if (sensorStatus.equals("Normal")) {
						iconChange.setImageResource(R.drawable.ic_fan_good);
					} else if (sensorStatus.equals("Warning")) {
						if (colorFlag < 1) {
							colorFlag = 1;
						}
						iconChange.setImageResource(R.drawable.ic_fan_warning);
					} else if (sensorStatus.equals("Critical")) {
						if (colorFlag < 2) {
							colorFlag = 2;
						}
						iconChange.setImageResource(R.drawable.ic_fan_bad);
					}
				}

				aBlock.addView(view);
			}

			if (colorFlag == 2) {
				colorBlock.setBackgroundResource(R.color.background_red);
			} else if (colorFlag == 1) {
				colorBlock.setBackgroundResource(R.color.background_orange);
			} else {
				colorBlock.setBackgroundResource(R.color.background_green);
			}

		}

		if (tasks.isEmpty()) {
			swipeLayout.setRefreshing(false);
		}
	}

	private void showEmptyBlocks() {
		TextView sensorName;
		TextView value;
		ImageView iconChange;
		LinearLayout aBlock;
		int textName;
		int textValue;
		int layoutItem;
		View view = null;
		LayoutInflater inflater = LayoutInflater.from(getActivity()
				.getBaseContext());

		cancelTasks();

		colorBlock.setBackgroundResource(R.color.background_red);

		aBlock = tempBlock;
		textName = R.id.textTemperature1;
		textValue = R.id.textTemperatureNumber1;
		layoutItem = R.layout.temperatureitem;

		view = inflater.inflate(layoutItem, aBlock, false);
		iconChange = (ImageView) view.findViewById(R.id.iconTemperature1);
		iconChange.setImageResource(R.drawable.ic_temperature_bad);
		sensorName = (TextView) view.findViewById(textName);
		sensorName.setText(getString(R.string.empty_block));
		value = (TextView) view.findViewById(textValue);
		value.setText(getString(R.string.empty_value));

		aBlock.addView(view);

		aBlock = voltBlock;
		textName = R.id.textPower1;
		textValue = R.id.textPowerNumber1;
		layoutItem = R.layout.voltageitem;

		view = inflater.inflate(layoutItem, aBlock, false);
		iconChange = (ImageView) view.findViewById(R.id.imageView12);
		iconChange.setImageResource(R.drawable.ic_power_bad);
		sensorName = (TextView) view.findViewById(textName);
		sensorName.setText(getString(R.string.empty_block));
		value = (TextView) view.findViewById(textValue);
		value.setText(getString(R.string.empty_value));

		aBlock.addView(view);

		aBlock = fanBlock;
		textName = R.id.textFan1;
		textValue = R.id.textFanNumber1;
		layoutItem = R.layout.fanitem;

		view = inflater.inflate(layoutItem, aBlock, false);
		iconChange = (ImageView) view.findViewById(R.id.imageView3);
		iconChange.setImageResource(R.drawable.ic_fan_bad);
		sensorName = (TextView) view.findViewById(textName);
		sensorName.setText(getString(R.string.empty_block));
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
}
