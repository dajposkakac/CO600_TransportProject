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
    public static void errorDialog(final MainActivity a, final String title, final int status, final String message)    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle(title).setMessage(message + a.getString(R.string.new_line) + a.getString(R.string.status_code_string) + status).setNeutralButton(a.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
        a.dismissProgressSpinner();
    }
}
