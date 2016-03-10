package transportapp.co600.googledirectionstest;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by Homo King on 04/03/2016.
 */
public class TimeDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private MainActivity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
       EditText timeText = (EditText) activity.findViewById(R.id.timeText);
        String time = activity.addMissingZero(hourOfDay) + ":" + activity.addMissingZero(minute);
        timeText.setText(time);
        timeText.setFocusable(false);
        activity.getRequest().setTime(time);
    }
}
