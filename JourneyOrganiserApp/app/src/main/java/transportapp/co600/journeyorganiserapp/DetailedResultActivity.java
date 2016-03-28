package transportapp.co600.journeyorganiserapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Displays a detailed view for a single route, including all of the information contained in the
 * request and displays a map of the route.
 *
 * @author jg404, mfm9
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

        info = (HashMap<String, String>) getIntent().getSerializableExtra(INFO_TAG);
        results = (HashMap<String, String>) getIntent().getSerializableExtra(RESULT_TAG);

        String transitModeText = results.get("transitMode");

        TextView departAt = null;
        TextView arriveAt = null;

        String priceValue = results.get("price");

        setContentView(R.layout.detail_results_activity);
        departAt = (TextView) findViewById(R.id.depart_at_result);
        arriveAt = (TextView) findViewById(R.id.arrive_at_result);
        TextView origin = (TextView) findViewById(R.id.from_result);
        TextView destination = (TextView) findViewById(R.id.to_result);
        TextView distance = (TextView) findViewById(R.id.distance);
        TextView duration = (TextView) findViewById(R.id.duration);
        TextView price = (TextView) findViewById(R.id.price);

        if(priceValue.equals("-1"))  {
            findViewById(R.id.price_layout).setVisibility(View.GONE);
            price.setText("£" + results.get("price"));
        }

        ImageView transitMode = (ImageView) findViewById(R.id.transit_mode);

        origin.setText(info.get("origin"));
        destination.setText(info.get("destination"));
        distance.setText(results.get("distance"));
        duration.setText(results.get("duration"));

        String departureTime = String.valueOf(results.get("departureTime"));
        String arrivalTime = String.valueOf(results.get("arrivalTime"));
        String departureDate = String.valueOf(results.get("departureDate"));
        String arrivalDate = String.valueOf(results.get("arrivalDate"));
        String departureTimeDate = departureTime + " - " + departureDate;
        String arrivalTimeDate = arrivalTime + " - " + arrivalDate;
        departAt.setText(departureTimeDate);
        arriveAt.setText(arrivalTimeDate);

        switch (transitModeText) {
            case "TRAIN":
                transitMode.setImageResource(R.drawable.train);
                break;
            case "BUS":
                transitMode.setImageResource(R.drawable.bus);
                break;
            case "WALKING":
                transitMode.setImageResource(R.drawable.walk);
                break;
            case "DRIVING":
                transitMode.setImageResource(R.drawable.car);
                break;
            case "BICYCLING":
                transitMode.setImageResource(R.drawable.cycle);
                break;
        }

        SupportMapFragment mapFragment = (InterceptTouchMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final ScrollView sv = (ScrollView) findViewById(R.id.scroll_view);
        ((InterceptTouchMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .setListener(new InterceptTouchMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        sv.requestDisallowInterceptTouchEvent(true);
                    }
                });


        String oll = info.get("originLatLng");
        String[] originData = oll.split(",");
        LatLng originPosition = new LatLng(Double.valueOf(originData[0]), Double.valueOf(originData[1]));
        Log.d("LatLng", originPosition.toString());
        String[] destinationData = info.get("destinationLatLng").split(",");
        LatLng destinationPosition = new LatLng(Double.valueOf(destinationData[0]), Double.valueOf(destinationData[1]));
        List<LatLng> polyline = PolyUtil.decode(results.get("polyline"));
        Log.d("polyline", results.get("polyline"));
//        final LatLngBounds bounds = new LatLngBounds(originPosition, destinationPosition);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.addMarker(new MarkerOptions().position(originPosition));
        mMap.addMarker(new MarkerOptions().position(destinationPosition));
//        String[] polylineString = results.get("polyline").split("\\|");
//        List<LatLng> polyline = new ArrayList<>();
//        for(int i = 0; i < polylineString.length; i++)  {
//            String[] coord = polylineString[i].split(",");
//            polyline.add(new LatLng(Double.valueOf(coord[0]), Double.valueOf(coord[1])));
//        }
        mMap.addPolyline(new PolylineOptions().addAll(polyline).width(10).color(Color.RED));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originPosition, 10));
        //mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }
}
