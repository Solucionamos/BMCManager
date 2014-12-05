package com.solucionamos.bmcmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.app.ListFragment;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.bmcmanager.R;
import com.solucionamos.bmcmanager.dummy.DummyContent;
import com.solucionamos.bmcmanager.model.Server;

/**
 * A list fragment representing a list of Items. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ServerDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ServerListFragment extends ListFragment implements
		SwipeRefreshLayout.OnRefreshListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private List<Server> listArray = null;

	private View view;
	
	private int pos;

	private ListFragmentSwipeRefreshLayout swipeLayout;

	
	
	private ServerAdapter adapter;
	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ServerListFragment() {
		swipeLayout = null;
		listArray = new ArrayList<Server>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DBHelper mydb = new DBHelper(this.getActivity());
		listArray = mydb.getAllServers();
		
		
		
		
		// Create adapter
		adapter = new ServerAdapter(this.getActivity(),
						R.layout.serverlistitem, listArray);
		/* Setting the list adapter for the ListFragment */
		// Set the adapter for this list as the one we created
		
		adapter.setNotifyOnChange(false);
		
		setListAdapter(adapter);
		
		
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		//setRetainInstance(true);

		View listView = super.onCreateView(inflater, container,
				savedInstanceState);

		swipeLayout = new ListFragmentSwipeRefreshLayout(listView.getContext());

		swipeLayout.addView(listView, ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		// Make sure that the SwipeRefreshLayout will fill the fragment
		swipeLayout.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		swipeLayout.setOnRefreshListener(this);
		//swipeLayout.setRefreshing(true);
		swipeLayout.setColorSchemeResources(android.R.color.holo_blue_light,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		// Return a view by using the Fragment onCreateView standard method.
		return swipeLayout;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		
		Server s = listArray.get(position);
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(s.getName());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		swipeLayout.setRefreshing(true);
		adapter.notifyDataSetChanged();
		if(this.getActivity().findViewById(R.id.item_detail_container) != null){
			ServerDetailFragment detailFragment = ((ServerDetailFragment) this.getActivity().getFragmentManager()
	                .findFragmentById(R.id.item_detail_container));
			if(detailFragment != null)
				detailFragment.refreshData();
		}
	}
	

	
	// This happens the last, after the view and activity are created, then
	// setEmptyText for this fragment as the text from the string empty from the
	// strings.xml (important for localization)
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setEmptyText(this.getActivity().getResources().getString(R.string.empty));
		
	}
	
	public void removeServer(Server el){
		Iterator<Server> it = listArray.iterator();
		Server itElement = null;
		while(it.hasNext()){
			itElement = it.next();
			if(itElement.getName().equals(el.getName())){
				break;
			}
		}
		adapter.remove(itElement);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				swipeLayout.setRefreshing(true);
			}
		}, 100);
		
		adapter.notifyDataSetChanged();
		/*if(listArray != null){
			DBHelper mydb = new DBHelper(this.getActivity().getApplicationContext());
			listArray = mydb.getAllServers();
		}*/
		//this.refresh();
	}
	
	private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

		public ListFragmentSwipeRefreshLayout(Context context) {
			super(context);
		}

		/**
		 * As mentioned above, we need to override this method to properly
		 * signal when a 'swipe-to-refresh' is possible.
		 * 
		 * @return true if the {@link android.widget.ListView} is visible and
		 *         can scroll up.
		 */
		@Override
		public boolean canChildScrollUp() {
			final ListView listView = getListView();
			if (listView.getVisibility() == View.VISIBLE) {
				return canListViewScrollUp(listView);
			} else {
				return false;
			}
		}

	}

	/**
	 * Utility method to check whether a {@link ListView} can scroll up from
	 * it's current position. Handles platform version differences, providing
	 * backwards compatible functionality where needed.
	 */
	private static boolean canListViewScrollUp(ListView listView) {
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			// For ICS and above we can call canScrollVertically() to determine
			// this
			return ViewCompat.canScrollVertically(listView, -1);
		} else {
			// Pre-ICS we need to manually check the first visible item and the
			// child view's top
			// value
			return listView.getChildCount() > 0
					&& (listView.getFirstVisiblePosition() > 0 || listView
							.getChildAt(0).getTop() < listView.getPaddingTop());
		}
	}
	
	public void setRefreshing(boolean bool){
		swipeLayout.setRefreshing(bool);
	}
	
}
