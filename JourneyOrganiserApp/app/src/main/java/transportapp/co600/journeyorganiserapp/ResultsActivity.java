package transportapp.co600.journeyorganiserapp;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.HashMap;

public class ResultsActivity extends AppCompatActivity {

    private HashMap<String, String> info;
    private HashMap<Integer, HashMap<String, String>> results;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        info = (HashMap<String, String>) getIntent().getSerializableExtra("info");
        results = (HashMap<Integer, HashMap<String, String>>) getIntent().getSerializableExtra("results");
        ListView resultsList = (ListView) findViewById(R.id.list);
        ResultsAdapter resultsAdapter = new ResultsAdapter(this, info, results);
        resultsList.setAdapter(resultsAdapter);
        Log.d("adapter", "" + resultsAdapter.getCount());
        //getActionBar().setDisplayHomeAsUpEnabled(true);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
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
