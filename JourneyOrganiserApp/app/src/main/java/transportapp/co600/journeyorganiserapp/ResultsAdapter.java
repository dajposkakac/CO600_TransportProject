package transportapp.co600.journeyorganiserapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daj on 16/12/2015.
 */
public class ResultsAdapter extends ArrayAdapter<String> {

    private final Context context;
    private static int layout;
    private final HashMap<String, String> info;
    private final HashMap<Integer, HashMap<String, String>> results;

    public ResultsAdapter(Context pContext, HashMap<String, String> pInfo, HashMap<Integer, HashMap<String, String>> pResults) {
        super(pContext, layout = R.layout.result_row);
        context = pContext;
        info = pInfo;
        results = pResults;
        List<Integer> list = new ArrayList<>(results.keySet());
        for(int i : list)   {
            add(String.valueOf(i));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)   {
        ViewHolder holder;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, parent, false);
            holder = new ViewHolder();
            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            holder.duration = (TextView) convertView.findViewById(R.id.time);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            holder.departAt = (TextView) convertView.findViewById(R.id.depart_at_result);
            holder.arriveAt = (TextView) convertView.findViewById(R.id.arrive_at_result);
            holder.transitModeImage = (ImageView) convertView.findViewById(R.id.transit_mode);
            convertView.setTag(holder);
        }   else    {
            holder = (ViewHolder) convertView.getTag();
        }

        String transitMode = results.get(position).get("transitMode");

        holder.distance.setText(results.get(position).get("distance"));
        holder.duration.setText(results.get(position).get("duration"));
        holder.price.setText("£" + results.get(position).get("price"));

        if (transitMode.equals("TRANSIT")) {
            String departureTime = String.valueOf(results.get(position).get("departureTime"));
            String arrivalTime = String.valueOf(results.get(position).get("arrivalTime"));
            String date = String.valueOf(results.get(position).get("date"));
            String departureTimeDate = departureTime + " - " + date;
            String arrivalTimeDate = arrivalTime + " - "  + date;
            holder.departAt.setText(departureTimeDate);
            holder.arriveAt.setText(arrivalTimeDate);
        }
        if(transitMode.equals("DRIVING"))   {
            holder.transitModeImage.setImageResource(R.drawable.car);
        }
        if(transitMode.equals("WALKING"))   {
            holder.transitModeImage.setImageResource(R.drawable.walk);
        }
        if(transitMode.equals("CYCLING"))   {
            holder.transitModeImage.setImageResource(R.drawable.cycle);
        }

        return convertView;
    }

//    @Override
//    public int getCount()   {
//        return 1;
//    }


    static class ViewHolder	{
        TextView distance;
        TextView duration;
        TextView price;
        TextView departAt;
        TextView arriveAt;
        ImageView transitModeImage;
    }
}
