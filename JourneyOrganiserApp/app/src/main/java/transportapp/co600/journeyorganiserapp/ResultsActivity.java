package transportapp.co600.journeyorganiserapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

import java.util.HashMap;

public class ResultsActivity extends AppCompatActivity {

    public static final String INFO_TAG = "info";
    static ResultsActivity instance;
    public Bundle savedData = null;

    public static final String RESULTS_TAG = "results";

    private static final String TAG = "resultsActivity";
    private HashMap<String, String> info;
    private HashMap<Integer, HashMap<String, String>> results;

    private ViewFlipper viewFlipper;
    private float lastX;
    private MultiStateToggleButton listSwitcher;
    private Context context;


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
                viewFlipper.setDisplayedChild(position);
            }
        });

        ResultClickListener resultClickListener = new ResultClickListener();
        if(savedInstanceState != null)  {
            info = (HashMap<String, String>) savedInstanceState.getSerializable(INFO_TAG);
            results = (HashMap<Integer, HashMap<String, String>>) savedInstanceState.getSerializable(RESULTS_TAG);
            Log.d(TAG, "savedinstance");
        }   else {
            info = (HashMap<String, String>) getIntent().getSerializableExtra(INFO_TAG);
            results = (HashMap<Integer, HashMap<String, String>>) getIntent().getSerializableExtra(RESULTS_TAG);
            Log.d(TAG, "intent");
        }

        ListView distanceList = (ListView) findViewById(R.id.list_distance);
        ResultsAdapter resultsAdapter = new ResultsAdapter(this, info, results, "distance");
        distanceList.setAdapter(resultsAdapter);
        distanceList.setOnItemClickListener(resultClickListener);

        ListView timeList = (ListView) findViewById(R.id.list_time);
        ResultsAdapter timeListAdapter = new ResultsAdapter(this, info, results, "duration");
        timeList.setAdapter(timeListAdapter);
        timeList.setOnItemClickListener(resultClickListener);

        ListView costList = (ListView) findViewById(R.id.list_cost);
        ResultsAdapter costListAdapter = new ResultsAdapter(this, info, results, "price");
        costList.setAdapter(costListAdapter);
        costList.setOnItemClickListener(resultClickListener);

        viewFlipper.setDisplayedChild(0);
        listSwitcher.setValue(0);

        Log.d("adapter", "" + resultsAdapter.getCount());
    }

    public static ResultsActivity getInstance() {
        if(instance == null)    {
            instance = new ResultsActivity();
        }
        return instance;
    }

    void setSavedData(Bundle bundle)    {
        savedData = bundle;
    }

    public Bundle getSavedData()    {
        return savedData;
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
    protected void onResume() {
        super.onResume();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onStop() {
        super.onStop();
        Bundle state = ResultsActivity.getInstance().getSavedData();
        if(state != null)   {
            info = (HashMap<String, String>) state.getSerializable(INFO_TAG);
            results = (HashMap<Integer, HashMap<String, String>>) state.getSerializable(RESULTS_TAG);
        }
    }

    @Override
    protected void onPause() {
        Bundle state = new Bundle();
        state.putSerializable(INFO_TAG, info);
        state.putSerializable(RESULTS_TAG, results);
        ResultsActivity.getInstance().setSavedData(state);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(INFO_TAG, info);
        outState.putSerializable(RESULTS_TAG, results);
    }
}
