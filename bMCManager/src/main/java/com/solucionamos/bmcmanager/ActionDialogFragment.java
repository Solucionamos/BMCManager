package com.solucionamos.bmcmanager;

import com.example.bmcmanager.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ActionDialogFragment extends DialogFragment {

	public interface ActionDialogInterface {
		public void cancelPowerOperation(int op);

		public void confirmPowerOperation(int op);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.actiondialog_text)
				.setPositiveButton(R.string.dialog_confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								/*ServerListActivity main = (ServerListActivity) getActivity();
								if (main != null) {
								}*/
								((ActionDialogInterface) getTargetFragment())
										.confirmPowerOperation(getOperation());

							}
						})
				.setNegativeButton(R.string.dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								((ActionDialogInterface) getTargetFragment())
										.cancelPowerOperation(getOperation());

							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}

	int getOperation() {
		return getArguments().getInt("operation");
	}
}
