package transportapp.co600.googledirectionstest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by daj on 16/12/2015.
 */
public class ResultsAdapter extends ArrayAdapter<String> {

    private final Context context;
    private static int layout;
    private HashMap<String, String> results;

    public ResultsAdapter(Context pContext, HashMap<String, String> pResults) {
        super(pContext, layout = R.layout.result_row, pResults.keySet().toArray(new String[pResults.size()]));
        context = pContext;
        results = pResults;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)   {
        ViewHolder holder;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, parent, false);
            holder = new ViewHolder();
            holder.transitMode = (TextView) convertView.findViewById(R.id.transit_mode);
            holder.origin = (TextView) convertView.findViewById(R.id.origin);
            holder.destination = (TextView) convertView.findViewById(R.id.destination);
            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            holder.duration = (TextView) convertView.findViewById(R.id.time);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            convertView.setTag(holder);
        }   else    {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.transitMode.setText(results.get("transitMode"));
        holder.origin.setText(results.get("origin"));
        holder.destination.setText(results.get("destination"));
        holder.distance.setText(results.get("distance"));
        holder.duration.setText(results.get("duration"));
        holder.price.setText("£" + results.get("price"));

        return convertView;
    }

    @Override
    public int getCount()   {
        return 1;
    }


    static class ViewHolder	{
        TextView transitMode;
        TextView origin;
        TextView destination;
        TextView distance;
        TextView duration;
        TextView price;
    }
}
