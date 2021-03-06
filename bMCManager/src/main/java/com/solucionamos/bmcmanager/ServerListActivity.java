package com.solucionamos.bmcmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.bmcmanager.R;
import com.solucionamos.bmcmanager.model.Server;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ServerDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ServerListFragment} and the item details
 * (if present) is a {@link ServerDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ServerListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ServerListActivity extends Activity
        implements ServerListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ServerListFragment) getFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .activateOnItemClick();
        }

    }

    /**
     * Callback method from {@link ServerListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ServerDetailFragment.ARG_ITEM_ID, id);
            ServerDetailFragment fragment = new ServerDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            System.out.println(id);
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ServerDetailActivity.class);
            detailIntent.putExtra(ServerDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_server_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.add_server:
                goToAddServerActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTwoPane)
            getActionBar().setTitle(R.string.title_AppTitle);
        else
            getActionBar().setTitle(R.string.action_titleServerList);
    }

    void goToAddServerActivity() {
        Intent k = new Intent(ServerListActivity.this, AddServerActivity.class);
        startActivity(k);
    }

    public void deleteServer(Server el) {
        DBHelper db = new DBHelper(this);
        db.deleteServer(el.getName());
        ((ServerListFragment) getFragmentManager()
                .findFragmentById(R.id.item_list)).removeServer(el);

        FrameLayout myFrame = (FrameLayout) findViewById(R.id.item_detail_container);
        myFrame.removeAllViews();
    }
}
