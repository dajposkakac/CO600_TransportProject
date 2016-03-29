package transportapp.co600.journeyorganiserapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.HashMap;
import java.util.List;

/**
 * Displays a detailed view for a single route, including all of the information contained in the
 * request and displays a map of the route.
 *
 * @author jg404, mfm9
 */
public class DetailedResultActivity extends AppCompatActivity implements OnMapReadyCallback {

    private HashMap<String, String> info;
    private HashMap<String, String> results;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        info = (HashMap<String, String>) getIntent().getSerializableExtra(getString(R.string.info_xml_tag));
        results = (HashMap<String, String>) getIntent().getSerializableExtra(getString(R.string.detailed_result_xml_tag));

        setTitle(info.get(getString(R.string.origin_display_xml_tag)) + getString(R.string.arrow_string) + info.get(getString(R.string.destination_display_xml_tag)));
        final String transitModeText = results.get(getString(R.string.transit_mode_xml_tag));

        TextView departAt = null;
        TextView arriveAt = null;

        final String priceValue = results.get(getString(R.string.price_xml_tag));

        setContentView(R.layout.detail_results_activity);
        departAt = (TextView) findViewById(R.id.depart_at_result);
        arriveAt = (TextView) findViewById(R.id.arrive_at_result);
        TextView origin = (TextView) findViewById(R.id.from_result);
        TextView destination = (TextView) findViewById(R.id.to_result);
        TextView distance = (TextView) findViewById(R.id.distance);
        TextView duration = (TextView) findViewById(R.id.duration);
        TextView price = (TextView) findViewById(R.id.price);

        if(priceValue.equals(getString(R.string.missing_price)))  {
            findViewById(R.id.price_layout).setVisibility(View.GONE);
        }   else    {
            price.setText(getString(R.string.pound_sign) + priceValue);
        }

        final ImageView transitMode = (ImageView) findViewById(R.id.transit_mode);

        origin.setText(info.get(getString(R.string.origin_xml_tag)));
        destination.setText(info.get(getString(R.string.destination_xml_tag)));
        distance.setText(results.get(getString(R.string.distance_xml_tag)));
        duration.setText(results.get(getString(R.string.duration_xml_tag)));

        final String departureTime = String.valueOf(results.get(getString(R.string.departure_time_xml_tag)));
        final String arrivalTime = String.valueOf(results.get(getString(R.string.arrival_time_xml_tag)));
        final String departureDate = String.valueOf(results.get(getString(R.string.departure_date_xml_tag)));
        final String arrivalDate = String.valueOf(results.get(getString(R.string.arrival_date_xml_tag)));
        final String departureTimeDate = departureTime + getString(R.string.spaced_dash) + departureDate;
        final String arrivalTimeDate = arrivalTime + getString(R.string.spaced_dash) + arrivalDate;
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
    public void onMapReady(final GoogleMap googleMap) {
        final ScrollView sv = (ScrollView) findViewById(R.id.scroll_view);
        ((InterceptTouchMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .setListener(new InterceptTouchMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        sv.requestDisallowInterceptTouchEvent(true);
                    }
                });
        final String oll = info.get(getString(R.string.origin_latlng_xml_tag));
        final String[] originData = oll.split(getString(R.string.comma));
        final LatLng originPosition = new LatLng(Double.valueOf(originData[0]), Double.valueOf(originData[1]));
        final String[] destinationData = info.get(getString(R.string.destination_latlng_xml_tag)).split(getString(R.string.comma));
        final LatLng destinationPosition = new LatLng(Double.valueOf(destinationData[0]), Double.valueOf(destinationData[1]));
        final List<LatLng> polyline = PolyUtil.decode(results.get(getString(R.string.polyline_xml_tag)));
//        final LatLngBounds bounds = new LatLngBounds(originPosition, destinationPosition);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.addMarker(new MarkerOptions().position(originPosition));
        googleMap.addMarker(new MarkerOptions().position(destinationPosition));
        googleMap.addPolyline(new PolylineOptions().addAll(polyline).width(10).color(Color.RED));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originPosition, 10));
        //mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }
}
