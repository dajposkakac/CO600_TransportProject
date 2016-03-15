package transportapp.co600.journeyorganiserapp;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ViewFlipper;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import java.util.HashMap;

public class ResultsActivity extends AppCompatActivity {

    private static final String TAG = "resultsActivity";
    private HashMap<String, String> info;
    private HashMap<Integer, HashMap<String, String>> results;

    private ViewFlipper viewFlipper;
    private float lastX;
    private MultiStateToggleButton listSwitcher;


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        listSwitcher = (MultiStateToggleButton) findViewById(R.id.mstb_multi_id);

        listSwitcher.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int position) {
                Log.d(TAG, "Button Selected : " + position);
                viewFlipper.setDisplayedChild(position);
            }
        });

        info = (HashMap<String, String>) getIntent().getSerializableExtra("info");
        results = (HashMap<Integer, HashMap<String, String>>) getIntent().getSerializableExtra("results");
        ListView distanceList = (ListView) findViewById(R.id.list_distance);
        ResultsAdapter resultsAdapter = new ResultsAdapter(this, info, results);
        distanceList.setAdapter(resultsAdapter);
        Log.d("adapter", "" + resultsAdapter.getCount());

        ListView timeList = (ListView) findViewById(R.id.list_time);
        ResultsAdapter timeListAdapter = new ResultsAdapter(this, info, results);
        timeList.setAdapter(timeListAdapter);

        ListView costList = (ListView) findViewById(R.id.list_cost);
        ResultsAdapter costListAdapter = new ResultsAdapter(this, info, results);
        costList.setAdapter(costListAdapter);

        viewFlipper.setDisplayedChild(0);
        listSwitcher.setValue(0);
    }

    @Override
    public void onBackPressed() {
//        moveTaskToBack(true);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
