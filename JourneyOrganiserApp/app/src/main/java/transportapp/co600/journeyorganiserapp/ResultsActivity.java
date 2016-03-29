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

    private static final String TAG = "RESULTS";

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
                flipToList(position);
            }
        });

        final String infoTag = getString(R.string.info_xml_tag);
        final String resultsTag = getString(R.string.results_xml_tag);
        if(savedInstanceState != null)  {
            info = (HashMap<String, String>) savedInstanceState.getSerializable(infoTag);
            results = (ArrayList<HashMap<String, String>>) savedInstanceState.getSerializable(resultsTag);
        }   else {
            info = (HashMap<String, String>) getIntent().getSerializableExtra(infoTag);
            results = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra(resultsTag);
        }

        final ResultClickListener resultClickListener = new ResultClickListener();
        final ListView distanceList = (ListView) findViewById(R.id.list_distance);
        distanceAdapter = new ResultsAdapter(this, info, results, getString(R.string.distance_xml_tag));
        distanceList.setAdapter(distanceAdapter);
        distanceList.setOnItemClickListener(resultClickListener);

        final ListView timeList = (ListView) findViewById(R.id.list_time);
        timeAdapter = new ResultsAdapter(this, info, results, getString(R.string.arrival_time_in_sec_xml_tag));
        timeList.setAdapter(timeAdapter);
        timeList.setOnItemClickListener(resultClickListener);

        final ListView costList = (ListView) findViewById(R.id.list_cost);
        costAdapter = new ResultsAdapter(this, info, results, getString(R.string.price_xml_tag));
        costList.setAdapter(costAdapter);
        costList.setOnItemClickListener(resultClickListener);

        flipToList(info.get(getString(R.string.sorting_preference_xml_tag)));
        setTitle(info.get(getString(R.string.origin_display_xml_tag)) + getString(R.string.arrow_string) + info.get(getString(R.string.destination_display_xml_tag)));
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
            final Intent intent = new Intent(context, DetailedResultActivity.class);
            intent.putExtra(getString(R.string.info_xml_tag), info);
            intent.putExtra(getString(R.string.detailed_result_xml_tag), results.get(position));
            context.startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(getString(R.string.info_xml_tag), info);
        outState.putSerializable(getString(R.string.results_xml_tag), results);
    }

    /**
     * Flips the ViewFlipper containing differently sorted results lists according to the specified preference.
     * @param preference
     */
    private void flipToList(final String preference)    {
        int pos = 0;
        final String[] names = getResources().getStringArray(R.array.sorting_preference_names);
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
    private void flipToList(final int pos)    {
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
