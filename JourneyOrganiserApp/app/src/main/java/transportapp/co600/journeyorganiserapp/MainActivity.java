package transportapp.co600.journeyorganiserapp;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.EditText;

import java.io.IOException;
import java.util.Calendar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener {

    private GeoApiContext geoApicontext;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private Intent ppIntent;

    private AutoCompleteTextView from;
    private AutoCompleteTextView to;
    private int mapButtonId;

    private LatLngBounds boundsCurrentLocation;
    private Request req;

    private ActionBarDrawerToggle mDrawerToggle;
    private String mDrawerTitle;

    private Spinner transitModeSpinner;
    private Spinner dateSpinner;
    private EditText timePicker;
    private EditText datePicker;

    private Calendar calendar;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        geoApicontext = new GeoApiContext().setApiKey(readKey("google_key_android"));
        buildGoogleApiClient();

        context = this;

        initNavigationDrawer();
        initLocationPickers();
        initTimePickers();
        initModesSpinner();
        initGoButton();
        initRequest();

    }

    public void goHandler(View view) {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            new RequestDirectionsTask(this, req).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
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

    private void initLocationPickers()  {
        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);

        from.setOnItemClickListener(mAutocompleteClickListener);
        to.setOnItemClickListener(mAutocompleteClickListener);
        from.setText("London, United Kingdom");
        to.setText("Oxford");

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, boundsCurrentLocation,
                null);
        from.setAdapter(mAdapter);
        to.setAdapter(mAdapter);

        initMapButtons();
    }

    private void initMapButtons()   {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            ppIntent = builder.build(this);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        mapButtonId = -1;
        Button fromMapButton = (Button) findViewById(R.id.from_map);
        Button toMapButton = (Button) findViewById(R.id.to_map);
        fromMapButton.setOnClickListener(mapButtonListener);
        toMapButton.setOnClickListener(mapButtonListener);
    }

    private void initNavigationDrawer() {
        mDrawerTitle = getTitle().toString();
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.setTitle("Journey Organiser");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, getResources().getStringArray(R.array.navigation_drawer_array)));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getSupportActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    private void initTimePickers()  {
        calendar = Calendar.getInstance();
        timePicker = (EditText) findViewById(R.id.time);
        timePicker.setInputType(InputType.TYPE_NULL);
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimeDialogFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        datePicker = (EditText) findViewById(R.id.date);
        datePicker.setInputType(InputType.TYPE_NULL);
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DateDialogFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        dateSpinner = (Spinner) findViewById(R.id.dateSpinner);
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.dateNames));
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(this);
    }

    private void initModesSpinner() {
        transitModeSpinner = (Spinner) findViewById(R.id.transit_modes_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.transit_mode_names));
        transitModeSpinner.setAdapter(adapter);
        transitModeSpinner.setOnItemSelectedListener(this);
    }

    private void initGoButton() {
        Button goButton = (Button) findViewById(R.id.go);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                req.setOrigin(from.getText().toString());
                req.setDestination(to.getText().toString());
                String time = timePicker.getText().toString();
                if(time.isEmpty())  {
                    time = "now";
                    req.setTime(time);
                }
                String date = datePicker.getText().toString();
                if(date.isEmpty())  {
                    date = "now";
                    req.setDate(date);
                }
                goHandler(v);
            }
        });
    }

    private void initRequest()   {
        req = new Request();
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

    private View.OnClickListener mapButtonListener
            = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int bId = v.getId();
            if(bId == R.id.from_map)  {
                mapButtonId = bId;
            }   else if (bId == R.id.to_map)    {
                mapButtonId = bId;
            }
            startActivityForResult(ppIntent, 1);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String result = null;
                Place place = PlacePicker.getPlace(this, data);
                String placeAddress = place.getAddress().toString();
                if(placeAddress.isEmpty())  {
                    result = place.getLatLng().latitude + "," + place.getLatLng().longitude;
                }   else {
                    result = placeAddress;
                }
                if(mapButtonId == R.id.from_map)    {
                    from.setText(String.format("%s", result));
                    from.requestFocus();
                    from.setSelection(from.getText().length());
                }   else if(mapButtonId == R.id.to_map) {
                    to.setText(String.format("%s", result));
                    to.requestFocus();
                    to.setSelection(to.getText().length());
                }
            }
        }
    }



    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position)   {
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).addToBackStack(null).commit();
        mDrawerLayout.closeDrawer(mDrawerList);
        mDrawerList.setItemChecked(position, false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

//    @Override
//    public void setTitle(CharSequence title) {
//        mTitle = title;
//        getActionBar().setTitle(mTitle);
//    }

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

        if (mDrawerToggle.onOptionsItemSelected(item)) {
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
    protected void onResume() {
        super.onResume();
        findViewById(R.id.loading).setVisibility(View.INVISIBLE);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                boundsCurrentLocation = new LatLngBounds(loc, loc);
            }
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        if(parentId == dateSpinner.getId()) {
            String[] departureSpinner = getResources().getStringArray(R.array.dateNames);
            req.setDepartureOption(departureSpinner[position]);
        }   else if(parentId == transitModeSpinner.getId()) {
            TravelMode tm = TravelMode.UNKNOWN;
            switch (position) {
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
    }

    public String addMissingZero(int time)    {
        String timeString = String.valueOf(time);
        if(time < 10 && time > -1)  {
            timeString = "0" + timeString;
        }
        return timeString;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public Request getRequest() {
        return req;
    }

    private String readKey(String keyName)  {
        try {
            Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getResources().openRawResource(R.raw.supersecretsecret));
            NodeList nodes = xmlDoc.getFirstChild().getChildNodes();
            for(int i = 0; i < nodes.getLength(); i++)  {
                Node node = nodes.item(i);
                if(node.getNodeName().equals(keyName))    {
                    return node.getTextContent();
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
