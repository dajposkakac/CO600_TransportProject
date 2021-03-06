package transportapp.co600.journeyorganiserapp;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The first activity presented to the user. Allows the user to set details for the journey search,
 * such as origin, destination, time, travel mode, etc. It is also the starting point of an AsyncTask
 * which makes a request to the server.
 *
 * @author jg404, mfm9
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private static final String REQUEST_TAG = "request";

    private GeoApiContext geoApicontext;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private Intent ppIntent;
    private Resources resources;

    private AutoCompleteTextView from;
    private AutoCompleteTextView to;
    private int mapButtonId;

    private LatLngBounds boundsCurrentLocation;
    private Request req;

    private ActionBarDrawerToggle mDrawerToggle;

    private Spinner transitModeSpinner;
    private Spinner sortingPreferenceSpinner;
    private Spinner dateSpinner;
    private EditText timePicker;
    private EditText datePicker;

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;

    private ProgressDialog progressBar;

    private ArrayList<AsyncTask> tasks;

    /**
     * Initialises GoogleApiClient and the UI elements.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        geoApicontext = new GeoApiContext().setApiKey(readKey(getString(R.string.google_key_android)));
        buildGoogleApiClient();
        resources = getResources();
        context = this;
        tasks = new ArrayList<>();

        initNavigationDrawer();
        initLocationPickers();
        initTimePickers();
        initModesSpinner();
        initSortingSpinner();
        initGoButton();
        initRequest();
    }

    /**
     * Checks network connection and starts the RequestDirectionsTask.
     */
    public void goHandler(final View view) {
        hideSoftKeyboard();
        final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            progressBar.show();
            AsyncTask<String, Void, String> requestTask = new RequestDirectionsTask(this, req).execute();
            addTask(requestTask);
        } else {
            ErrorDialogFragment.errorDialog(this, "Error", -2, "No Internet connection.");

        }
    }

    /**
     * Builds GoogleApiClient by adding necessary Apis.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Initialises text input fields for origin and destination, autocomplete adapters,
     * clear text buttons and map buttons.
     */
    private void initLocationPickers()  {
        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);

        from.setOnItemClickListener(mAutocompleteClickListener);
        to.setOnItemClickListener(mAutocompleteClickListener);

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, boundsCurrentLocation,
                null);
        from.setAdapter(mAdapter);
        to.setAdapter(mAdapter);

        initCloseButtons();

        initMapButtons();
    }

    /**
     * Initialises clear text buttons.
     */
    private void initCloseButtons() {
        final ImageButton fromClearButton = (ImageButton) findViewById(R.id.from_clear);
        fromClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from.setText("");
            }
        });

        final ImageButton toClearButton = (ImageButton) findViewById(R.id.to_clear);
        toClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to.setText("");
            }
        });
    }

    /**
     * Initialises map buttons for location picking.
     */
    private void initMapButtons()   {
        final PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            ppIntent = builder.build(this);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        mapButtonId = -1;
        final ImageButton fromMapButton = (ImageButton) findViewById(R.id.from_map);
        final ImageButton toMapButton = (ImageButton) findViewById(R.id.to_map);
        fromMapButton.setOnClickListener(mapButtonListener);
        toMapButton.setOnClickListener(mapButtonListener);
    }

    /**
     * Initialises navigation drawer, which contains settings and About us.
     * Also intialisese the toolbar.
     */
    private void initNavigationDrawer() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.setTitle(getString(R.string.app_name));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        final LayoutInflater inflater = getLayoutInflater();
        final View listHeaderView = inflater.inflate(R.layout.header_view, null, false);
        mDrawerList.addHeaderView(listHeaderView);

        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, getResources().getStringArray(R.array.navigation_drawer_array)));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.open, R.string.close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    /**
     * Initialises the time and date pickers as well as the departure option spinner.
     */
    private void initTimePickers()  {
        timePicker = (EditText) findViewById(R.id.time);
        timePicker.setInputType(InputType.TYPE_NULL);
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimeDialogFragment();
                newFragment.show(getFragmentManager(), getString(R.string.time_picker_tag));
            }
        });
        timePicker.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                hideSoftKeyboard();
                timePicker.setText("");
                return true;
            }
        });
        timePicker.setFocusable(false);
        timePicker.setMaxWidth(timePicker.getWidth());
        datePicker = (EditText) findViewById(R.id.date);
        datePicker.setInputType(InputType.TYPE_NULL);
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DateDialogFragment();
                newFragment.show(getFragmentManager(), getString(R.string.date_picker_tag));
            }
        });
        datePicker.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                hideSoftKeyboard();
                datePicker.setText("");
                return true;
            }
        });
        datePicker.setFocusable(false);
        datePicker.setMaxWidth(datePicker.getWidth());
        dateSpinner = (Spinner) findViewById(R.id.dateSpinner);
        final ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.dateNames));
        dateSpinner.setAdapter(dateAdapter);
        dateSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Initialises travel modes spinner.
     */
    private void initModesSpinner() {
        transitModeSpinner = (Spinner) findViewById(R.id.transit_modes_spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.transit_mode_names));
        transitModeSpinner.setAdapter(adapter);
        transitModeSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Initialises sorting preference spinner
     */
    private void initSortingSpinner() {
        sortingPreferenceSpinner = (Spinner) findViewById(R.id.sorting_preference_spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.sorting_preference_names));
        sortingPreferenceSpinner.setAdapter(adapter);
        sortingPreferenceSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Initialises the search button.
     */
    private void initGoButton() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Getting routes...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setIndeterminate(true);
        progressBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelTasks();
            }
        });
        final Button goButton = (Button) findViewById(R.id.go);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserInput();
                goHandler(v);
            }
        });
    }

    /**
     * Initialises the Request object.
     */
    private void initRequest()   {
        req = new Request();
    }

    /**
     * Grabs data from user input fields and updates the Request.
     */
    private void getUserInput() {
        req.setOrigin(from.getText().toString());
        req.setDestination(to.getText().toString());
        String time = timePicker.getText().toString();
        final Calendar calendar = Calendar.getInstance();
        if(time.isEmpty())  {
            String colon = getString(R.string.colon);
            time = addMissingZero(calendar.get(Calendar.HOUR_OF_DAY)) + colon + addMissingZero(calendar.get(Calendar.MINUTE))  + colon + addMissingZero(calendar.get(Calendar.SECOND));
            req.setTime(time);
        }
        String date = datePicker.getText().toString();
        if(date.isEmpty())  {
            String dash = getString(R.string.dash);
            date = calendar.get(Calendar.YEAR) + dash + addMissingZero(calendar.get(Calendar.MONTH) + 1) + dash + addMissingZero(calendar.get(Calendar.DAY_OF_MONTH));
            req.setDate(date);
        }
    }

    /**
     * Initialises autocomplete click listener.
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);

        }
    };

    /**
     * Initialises the map button listener.
     */
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

    /**
     * Sets the origin or destination field after location was selected on the map.
     */
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String result = null;
                Place place = PlacePicker.getPlace(this, data);
                final String placeAddress = place.getAddress().toString();
                if(placeAddress.isEmpty())  {
                    result = place.getLatLng().latitude + getString(R.string.comma) + place.getLatLng().longitude;
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

    /**
     * Class containing navigation drawer item click listener.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     *  Makes the navigation drawer click happen.
     */
    private void selectItem(final int position)   {
        if(position == 1) {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).addToBackStack(null).commit();
        }
        else if(position == 2)  {
             startActivity(new Intent(this, AboutUsActivity.class));
        }
        mDrawerLayout.closeDrawer(mDrawerList);
        mDrawerList.setItemChecked(position, false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

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

        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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
        dismissProgressSpinner();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(REQUEST_TAG, req);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    /**
     * Gets current client location.
     */
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                final LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                boundsCurrentLocation = new LatLngBounds(loc, loc);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

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

    /**
     * Handles all spinners onItemSelected. Updates Request appropriately.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        hideSoftKeyboard();
        int parentId = parent.getId();
        if(parentId == dateSpinner.getId()) {
            final String[] departureSpinner = getResources().getStringArray(R.array.dateNames);
            req.setDepartureOption(departureSpinner[position]);
        }   else if(parentId == transitModeSpinner.getId()) {
            String tm = null;
            switch (position) {
                case 0:
                    tm = TravelMode.DRIVING.toString() + getString(R.string.comma) + TravelMode.TRANSIT.toString();
                    break;
                case 1:
                    String comma = getString(R.string.comma);
                    tm = TravelMode.DRIVING.toString() + comma + TravelMode.TRANSIT.toString() + comma + TravelMode.BICYCLING.toString() + comma + TravelMode.WALKING.toString();
                    break;
                case 2:
                    tm = TravelMode.DRIVING.toString();
                    break;
                case 3:
                    tm = TravelMode.TRANSIT.toString();
                    break;
                case 4:
                    tm = TravelMode.WALKING.toString();
                    break;
                case 5:
                    tm = TravelMode.BICYCLING.toString();
                    break;
            }
            req.setTransitMode(tm);
        }   else if(parentId == sortingPreferenceSpinner.getId())  {
            final String[] names = resources.getStringArray(R.array.sorting_preference_names);
            req.setSortingPreference(names[position]);
        }
    }

    /**
     * Adds leading 0s to single digits.
	 */
    public String addMissingZero(final int time)    {
        String timeString = String.valueOf(time);
        if(time < 10 && time > -1)  {
            timeString = getString(R.string.zero_string) + timeString;
        }
        return timeString;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hideSoftKeyboard();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    /**
     *  Returns Request object.
     */
    public Request getRequest() {
        return req;
    }

    public void addTask(AsyncTask task) {
        tasks.add(task);
    }

    public void removeTask(AsyncTask task)  {
        while(tasks.contains(task))    {
            tasks.remove(task);
        }
    }

    public void cancelTasks()   {
        for(AsyncTask t : tasks)    {
            t.cancel(true);
            removeTask(t);
        }
    }

    public void hideSoftKeyboard() {
        final InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public void dismissProgressSpinner()    {
        if(progressBar.isShowing()) {
            progressBar.dismiss();
        }
    }

    /**
     * Reads the key for the key type specified.
     */
    private String readKey(final String keyName)  {
        try {
            final Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getResources().openRawResource(R.raw.supersecretsecret));
            final NodeList nodes = xmlDoc.getFirstChild().getChildNodes();
            for(int i = 0; i < nodes.getLength(); i++)  {
                final Node node = nodes.item(i);
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
