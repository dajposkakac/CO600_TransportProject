package transportapp.co600.journeyorganiserapp;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Displays a TimePicker dialog and updates the Request with the specified time.
 *
 * @author mfm9
 */
public class TimeDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private MainActivity activity;
    private EditText timeText;
    private Calendar c;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        timeText = (EditText) activity.findViewById(R.id.time);
        return new TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String timeAddedZero = activity.addMissingZero(hourOfDay) + ":" + activity.addMissingZero(minute);
        timeText.setText(timeAddedZero);
        timeAddedZero = timeAddedZero + ":" + activity.addMissingZero(c.get(Calendar.SECOND));
        activity.getRequest().setTime(timeAddedZero);
    }
}
