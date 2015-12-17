package transportapp.co600.googledirectionstest;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ResultsActivity extends AppCompatActivity {

    private ListView resultsList;
    private ResultsAdapter resultsAdapter;
    private HashMap<String, String> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        results = (HashMap<String, String>) getIntent().getSerializableExtra("results");
        resultsList = (ListView) findViewById(R.id.list);
        resultsAdapter = new ResultsAdapter(this, results);
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
