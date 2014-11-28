package com.example.gregoire.rfa;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NoticeDialogFragment extends DialogFragment
{
    private static int m_dialogType;
    NoticeDialogListener mListener;

    static public void setDialogType(int dialogType) {
        m_dialogType = dialogType;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder dialogBuilder;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.add_feed_dialog, null);

        dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage("Add a feed")
                .setView(v)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        try {
                            mListener.onDialogPositiveClick(NoticeDialogFragment.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        return (dialogBuilder.create());
    }

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog) throws Exception;
    }
}

