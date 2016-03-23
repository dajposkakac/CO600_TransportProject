package transportapp.co600.journeyorganiserapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by daj on 21/02/2016.
 */
public class ErrorDialogFragment extends DialogFragment {

    public static void errorDialog(Activity a, String title, int status, String message)    {
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle(title).setMessage(message + "\n" + "code: " + status).setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
