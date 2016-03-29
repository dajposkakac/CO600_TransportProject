package transportapp.co600.journeyorganiserapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Displays a DatePicker dialog and updates the Request with the specified date.
 *
 * @author mfm9
 */
public class DateDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private MainActivity activity;
    private EditText dateText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        activity.hideSoftKeyboard();
        final Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int day = c.get(Calendar.DAY_OF_MONTH);
        dateText = (EditText) activity.findViewById(R.id.date);
        return new DatePickerDialog(activity, this, year, month, day);
    }

    @Override
    public void onDateSet(final DatePicker view, final int year, int monthOfYear, final int dayOfMonth) {
        final String dash = getString(R.string.dash);
        monthOfYear++;
        final String monthAddedZeros = activity.addMissingZero(monthOfYear);
        final String dayAddedZeros = activity.addMissingZero(dayOfMonth);
        final String date = year + dash + monthAddedZeros + dash + dayAddedZeros;
        final String dateDisplay = dayAddedZeros + dash + monthAddedZeros + dash + year;
        dateText.setText(dateDisplay);
        activity.getRequest().setDate(date);
    }
}
