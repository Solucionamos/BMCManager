package com.solucionamos.bmcmanager;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.solucionamos.bmcmanager.model.Server;


import com.example.bmcmanager.R;
import com.solucionamos.bmcmanager.dummy.DummyContent;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ServerListActivity}
 * in two-pane mode (on tablets) or a {@link ServerDetailActivity}
 * on handsets.
 */
public class ServerDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Server serverItem = null;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.serverdetails_fragment, container, false);

        // Show the dummy content as text in a TextView.
        if (serverItem != null) {
         
            ((TextView) rootView.findViewById(R.id.textServerName)).setText(serverItem.getName());

         	((TextView) rootView.findViewById(R.id.textServerDescription)).setText(serverItem.getAddress());
         	
         	/*()
         			powerSwitch = (Switch) rootView.findViewById(R.id.switchStatus);

         			powerSwitch
         					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
         					});*/
        }

        return rootView;
    }
    

}
