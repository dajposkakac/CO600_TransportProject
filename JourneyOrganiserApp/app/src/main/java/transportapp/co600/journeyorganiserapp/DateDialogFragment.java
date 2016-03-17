package transportapp.co600.journeyorganiserapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Created by Homo King on 08/03/2016.
 */
public class DateDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String DASH = "-";
    private MainActivity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(activity, this, year, month, day);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        EditText dateText = (EditText) activity.findViewById(R.id.date);
        monthOfYear++;
        String monthAddedZeros = activity.addMissingZero(monthOfYear);
        String dayAddedZeros = activity.addMissingZero(dayOfMonth);
        String date = year + DASH + monthAddedZeros + DASH + dayAddedZeros;
        String dateDisplay = dayAddedZeros + DASH + monthAddedZeros + DASH + year;
        dateText.setText(dateDisplay);
        dateText.setFocusable(false);
        activity.getRequest().setDate(date);
    }
}
