package transportapp.co600.journeyorganiserapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity displays a MultiStateButton used to select sorting of the request results
 * in the ListView below it. Each position on the list basic information about its route
 * and can be clicked on to bring up more detailed information.
 *
 * @author jg404, mfm9
 */
public class ResultsActivity extends AppCompatActivity {

    public static final String INFO_TAG = "info";
    public static final String RESULTS_TAG = "results";

    private static final String TAG = "resultsActivity";

    private HashMap<String, String> info;
    private ArrayList<HashMap<String, String>> results;

    private ViewFlipper viewFlipper;
    private MultiStateToggleButton listSwitcher;
    private Context context;
    private ResultsAdapter distanceAdapter;
    private ResultsAdapter timeAdapter;
    private ResultsAdapter costAdapter;


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        context = this;

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        listSwitcher = (MultiStateToggleButton) findViewById(R.id.mstb_multi_id);

        listSwitcher.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int position) {
                Log.d(TAG, "Button Selected : " + position);
                flipToList(position);
            }
        });

        if(savedInstanceState != null)  {
            info = (HashMap<String, String>) savedInstanceState.getSerializable(INFO_TAG);
            results = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(RESULTS_TAG);
        }   else {
            info = (HashMap<String, String>) getIntent().getSerializableExtra(INFO_TAG);
            results = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra(RESULTS_TAG);
        }

        ResultClickListener resultClickListener = new ResultClickListener();
        ListView distanceList = (ListView) findViewById(R.id.list_distance);
        distanceAdapter = new ResultsAdapter(this, info, results, "distance");
        distanceList.setAdapter(distanceAdapter);
        distanceList.setOnItemClickListener(resultClickListener);

        ListView timeList = (ListView) findViewById(R.id.list_time);
        timeAdapter = new ResultsAdapter(this, info, results, "arrivalTimeInSeconds");
        timeList.setAdapter(timeAdapter);
        timeList.setOnItemClickListener(resultClickListener);

        ListView costList = (ListView) findViewById(R.id.list_cost);
        costAdapter = new ResultsAdapter(this, info, results, "price");
        costList.setAdapter(costAdapter);
        costList.setOnItemClickListener(resultClickListener);

        flipToList(info.get("sortingPreference"));
        setTitle(info.get("originDisplay") + " -> " + info.get("destinationDisplay"));

        Log.d("adapter", "" + distanceAdapter.getCount());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ResultClickListener for the ListView. It starts the DetailedResultsActivity
     * with the data for the selected route.
     */
    private class ResultClickListener implements AdapterView.OnItemClickListener    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(context, DetailedResultActivity.class);
            intent.putExtra(INFO_TAG, info);
            intent.putExtra("result", results.get(position));
            context.startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(INFO_TAG, info);
        outState.putSerializable(RESULTS_TAG, results);
    }

    /**
     * Flips the ViewFlipper containing differently sorted results lists according to the specified preference.
     * @param preference
     */
    private void flipToList(String preference)    {
        int pos = 0;
        String[] names = getResources().getStringArray(R.array.sorting_preference_names);
        boolean[] states = new boolean[names.length];
        boolean found = false;
        int i = 0;
        while(!found && i < names.length) {
            if (preference.equals(names[i])) {
                found = true;
                pos = i;
            }
            states[i] = found;
            i++;
        }
        listSwitcher.setStates(states);
        flipToList(pos);
    }

    /**
     * Flips the ViewFlipper containing differently sorted results lists to the specified position.
     * @param pos
     */
    private void flipToList(int pos)    {
        if(pos == 0)  {
            results = distanceAdapter.getSortedResults();
        }   else if(pos == 1)  {
            results = timeAdapter.getSortedResults();
        }   else if(pos == 2)  {
            results = costAdapter.getSortedResults();
        }
        viewFlipper.setDisplayedChild(pos);
    }

    /**
     * Returns the currently selected sorting preference.
     * @return
     */
    private int getSelectedList()   {
        return viewFlipper.getDisplayedChild();
    }
}
