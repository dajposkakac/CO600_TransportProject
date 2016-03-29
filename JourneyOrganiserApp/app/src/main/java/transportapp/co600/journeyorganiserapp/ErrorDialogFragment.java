package transportapp.co600.journeyorganiserapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * ErrorDialog fragment, which can be used by calling its static method.
 *
 * @author jg404
 */
public class ErrorDialogFragment extends DialogFragment {

    /**
     * Displays an AlertDialog with the information specified in the parameters.
     * @param a Activity to show the dialog on.
     * @param title title of the dialog window
     * @param status status code of the error
     * @param message error message
     */
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
