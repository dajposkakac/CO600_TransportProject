package transportapp.co600.journeyorganiserapp;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

/**
 * Created by daj on 15/03/2016.
 */
public class DetailedResultActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String INFO_TAG = "info";
    public static final String RESULT_TAG = "result";

    private HashMap<String, String> info;
    private HashMap<String, String> results;

    private GoogleMap mMap;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_results_activity);

        info = (HashMap<String, String>) getIntent().getSerializableExtra(INFO_TAG);
        results = (HashMap<String, String>) getIntent().getSerializableExtra(RESULT_TAG);

        TextView transitMode = (TextView) findViewById(R.id.transit_mode);
        TextView origin = (TextView) findViewById(R.id.from_result);
        TextView destination = (TextView) findViewById(R.id.to_result);
        TextView distance = (TextView) findViewById(R.id.distance);
        TextView duration = (TextView) findViewById(R.id.duration);
        TextView price = (TextView) findViewById(R.id.price);
        TextView departAt = (TextView) findViewById(R.id.depart_at_result);
        TextView arriveAt = (TextView) findViewById(R.id.arrive_at_result);

        String transitModeText = results.get("transitMode");

        transitMode.setText(transitModeText);
        origin.setText(info.get("origin"));
        destination.setText(info.get("destination"));
        distance.setText(results.get("distance"));
        duration.setText(results.get("duration"));
        price.setText("£" + results.get("price"));


        if(transitModeText.equals("TRANSIT")) {
            String departureTime = String.valueOf(results.get("departureTime"));
            String arrivalTime = String.valueOf(results.get("arrivalTime"));
            String date = String.valueOf(results.get("date"));
            String departureTimeDate = departureTime + " - " + date;
            String arrivalTimeDate = arrivalTime + " - "  + date;
            departAt.setText(departureTimeDate);
            arriveAt.setText(arrivalTimeDate);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng canters = new LatLng(51.2750, 1.0870);
        mMap.addMarker(new MarkerOptions().position(canters));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(canters));
    }
}
