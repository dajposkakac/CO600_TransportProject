package transportapp.co600.googledirectionstest;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener {

    private GeoApiContext geoApicontext;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView from;
    private AutoCompleteTextView to;
    private Button goButton;
    private Spinner transitModeSpinner;

    private LatLngBounds BOUNDS_CURRENT_LOCATION;
    private Location mLastLocation;

    private static Socket socket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;
    private PrintWriter printwriter;
    private Request req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        geoApicontext = new GeoApiContext().setApiKey("AIzaSyA7zjvluw5ono4sjIZQx2LTCQdr7d0uP5E");
        context = this;
        buildGoogleApiClient();

        req = new Request();

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);

        from.setOnItemClickListener(mAutocompleteClickListener);
        to.setOnItemClickListener(mAutocompleteClickListener);
        from.setText("London, United Kingdom");
        to.setText("Oxford");

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_CURRENT_LOCATION,
                null);
        from.setAdapter(mAdapter);
        to.setAdapter(mAdapter);

        transitModeSpinner = (Spinner) findViewById(R.id.transit_modes_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.transit_mode_names));
        transitModeSpinner.setAdapter(adapter);
        transitModeSpinner.setOnItemSelectedListener(this);

        goButton = (Button) findViewById(R.id.go);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                req.setOrigin(from.getText().toString());
                req.setDestination(to.getText().toString());
                goHandler(v);

//                setContentView(R.layout.directions_list_layout);
//                listView = (ListView) findViewById(R.id.directionsList);
//                Button backButton = (Button) findViewById(R.id.back);
//                backButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        setContentView(R.layout.activity_main);
//
//                    }
//                });
            }
        });
    }

    public void goHandler(View view)    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnected())    {
            new RequestDirectionsTask(this, req).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }   else    {
            Log.d("CONN", "No network connection");
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .build();
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            //Log.i(TAG, "Autocomplete item selected: " + primaryText);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
//            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            //Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume()   {
        super.onResume();
        findViewById(R.id.loading).setVisibility(View.INVISIBLE);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            BOUNDS_CURRENT_LOCATION = new LatLngBounds(loc, loc);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TravelMode tm = TravelMode.UNKNOWN;
        switch (position)   {
            case 1:
                tm = TravelMode.DRIVING;
                break;
            case 2:
                tm = TravelMode.TRANSIT;
                break;
            case 3:
                tm = TravelMode.WALKING;
                break;
            case 4:
                tm = TravelMode.BICYCLING;
                break;
        }
        req.setTransitMode(tm);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
