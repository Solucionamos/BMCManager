package com.solucionamos.bmcmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.bmcmanager.R;

public class ActionDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.action_dialog_text)
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

    public interface ActionDialogInterface {
        public void cancelPowerOperation(int op);

        public void confirmPowerOperation(int op);
    }
}
