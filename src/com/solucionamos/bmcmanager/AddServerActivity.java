package com.solucionamos.bmcmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.solucionamos.bmcmanager.model.Server;
import com.example.bmcmanager.R;

public class AddServerActivity extends Activity implements OnClickListener,
		AsyncResponse<BMCResponse> {

	// It has a reference to the button and the EditText, in the future, there
	// should be a reference to each text field in the fragment.
	private Button btn;
	private EditText serverName;
	private EditText hostname;
	private EditText username;
	private EditText password;
	private Spinner spinner;
	private String spinnerText;
	private Button cancelBtn;

	private DBHelper mydb;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addserver_frag);
		
		// Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        	getActionBar().setHomeAsUpIndicator(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);

		btn = (Button) this.findViewById(R.id.addServerBtn);
		btn.setOnClickListener(this);

		cancelBtn = (Button) this.findViewById(R.id.backServerBtn);
		cancelBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}

		});

		spinnerText = "";

		// serverProtocol = (EditText)
		// this.findViewById(R.id.serverProtocolTxt);
		serverName = (EditText) this.findViewById(R.id.serverNameTxt);
		hostname = (EditText) this.findViewById(R.id.ipaddressTxt);
		username = (EditText) this.findViewById(R.id.usernameTxt);
		password = (EditText) this.findViewById(R.id.pswText);
		spinner = (Spinner) this.findViewById(R.id.serverProtocolSpinner);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				spinnerText = spinner.getSelectedItem().toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		

		
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Set title
		this.getActionBar().setTitle(R.string.action_titleAddServer);
	}

	@Override
	public void onClick(View v) {
		try {

			String name = serverName.getText().toString().trim();
			String ipaddress = hostname.getText().toString().trim();
			String uname = username.getText().toString().trim();
			String pass = password.getText().toString().trim();
			System.out.println("SPINNER: " + spinnerText);
			Server serv = new Server(spinnerText, "IVB", serverName.getText()
					.toString(), hostname.getText().toString(), username
					.getText().toString(), password.getText().toString());

			if (!(spinnerText.length() == 0 || name.length() == 0
					|| ipaddress.length() == 0 || uname.length() == 0 || pass
						.length() == 0)) {
				mydb = new DBHelper(this);
				Server el = mydb.getServer(name);
				if(el != null){
					Context context = this.getApplicationContext();
					CharSequence text = getString(R.string.server_exists);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}else{
					TestConnectionTask asyncTask = new TestConnectionTask();
					asyncTask.delegate = this;
					asyncTask.execute(serv);
				}
			} else {
				Context context = this.getApplicationContext();
				CharSequence text = getString(R.string.required_string) + "\n";
				if (name.length() == 0)
					text = text
							+ " -"
							+ getString(R.string.addserver_name).replace(":",
									"") + "\n";
				if (ipaddress.length() == 0)
					text = text
							+ " -"
							+ getString(R.string.addserver_address).replace(
									":", "") + "\n";
				if (uname.length() == 0)
					text = text
							+ " -"
							+ getString(R.string.addserver_username).replace(
									":", "") + "\n";
				if (pass.length() == 0)
					text = text
							+ " -"
							+ getString(R.string.addserver_password).replace(
									":", "") + "\n";
				if (spinnerText.length() == 0) {
					text = text
							+ " -"
							+ getString(R.string.addserver_protocol).replace(
									":", "") + "\n";
				}
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		} catch (Exception e) {
			Context context = this.getApplicationContext();
			CharSequence text = getString(R.string.connection_nosuccess);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private void addServer(Server el) {
		mydb = new DBHelper(this);
		mydb.insertServer(el);

		try {
			Intent k = new Intent(AddServerActivity.this, ServerListActivity.class);
			k.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(k);
		} catch (Exception e) {

			Context context = this.getApplicationContext();
			CharSequence text = getString(R.string.connection_nosuccess);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	@Override
	public void processFinish(BMCResponse response, Exception ex) {
		if (ex != null) {

			System.out.println(ex);
			//ex.printStackTrace();
			Context context = this.getApplicationContext();
			CharSequence text = getString(R.string.connection_nosuccess);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} else {
			this.addServer(response.getServer());
		}
	}
}
