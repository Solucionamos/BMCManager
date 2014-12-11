package com.solucionamos.bmcmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.bmcmanager.R;
import com.solucionamos.bmcmanager.model.Server;

public class DeleteDialogFragment extends DialogFragment {
    private Server el;
    private boolean tabletFlag;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_dialog_text)
                .setPositiveButton(R.string.dialog_confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (tabletFlag) {
                                    ServerListActivity main = (ServerListActivity) getActivity();
                                    if (main != null)
                                        main.deleteServer(el);
                                } else {
                                    ServerDetailActivity main = (ServerDetailActivity) getActivity();
                                    if (main != null)
                                        main.deleteServer(el);
                                }


                            }
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setServer(Server el) {
        this.el = el;
    }

    public void setTablet(boolean tabletFlag) {
        this.tabletFlag = tabletFlag;
    }
}