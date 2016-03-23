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
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by daj on 16/12/2015.
 */
public class ResultsAdapter extends ArrayAdapter<String> {

    private final Context context;
    private static int layout;
    private final HashMap<String, String> info;
    private final LinkedHashMap<Integer, HashMap<String, String>> results;
    private final double[] valuesArray;
    private Integer[] list;

    public ResultsAdapter(Context pContext, HashMap<String, String> pInfo, LinkedHashMap<Integer, HashMap<String, String>> pResults, String sort) {
        super(pContext, layout = R.layout.result_row);
        context = pContext;
        info = pInfo;
        results = new LinkedHashMap<>(pResults);
        LinkedHashMap<Integer, HashMap<String, String>> resultsCopy = new LinkedHashMap<>();
        resultsCopy.putAll(results);

        list = results.keySet().toArray(new Integer[results.size()]);

        valuesArray = new double[list.length];

        for (int j = 0; j < list.length; j++)   {
            String values = results.get(j).get(sort);
            String value = null;
            switch (sort) {
                case "distance":
                    value = values.replaceAll("[\\D+]+$", "");
                    if(isDouble(value)) {
                        valuesArray[j] = Double.valueOf(value);
                    }
                    else    {
                        valuesArray[j] = Integer.valueOf(value);
                    }
                    break;
                case "arrivalTimeInSeconds":
                    value = values;
                        if(results.get(0).get("departureOption").startsWith("Arrive"))  {
                            valuesArray[j] = Double.valueOf(value);
                        }
                        else {
                            sort = "departureTimeInSeconds";
                            j--;
                        }
                    break;
                case "departureTimeInSeconds":
                    value = values;
                    if(results.get(0).get("departureOption").startsWith("Depart"))  {
                        valuesArray[j] = Double.valueOf(value);
                    }
                    else {
                        sort = "arrivalTimeInSeconds";
                        j--;
                    }
                    break;
                case "price":
                    value = values;
                    if(isDouble(value)) {
                        valuesArray[j] = Double.valueOf(value);
                    }
                    else    {
                        valuesArray[j] = Integer.valueOf(value);
                    }
                    break;
            }
        }
        sort(0, list.length - 1);

        for(int i = 0; i < list.length; i++) {
            results.put(i, resultsCopy.get(list[i]));
        }

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

        String departureTime = String.valueOf(results.get(position).get("departureTime"));
        String arrivalTime = String.valueOf(results.get(position).get("arrivalTime"));
        String date = String.valueOf(results.get(position).get("date"));
        String departureTimeDate = departureTime + " - " + date;
        String arrivalTimeDate = arrivalTime + " - " + date;
        holder.departAt.setText(departureTimeDate);
        holder.arriveAt.setText(arrivalTimeDate);

        switch (transitMode) {
            case "TRAIN":
                holder.transitModeImage.setImageResource(R.drawable.train);
                break;
            case "BUS":
                holder.transitModeImage.setImageResource(R.drawable.bus);
                break;
            case "DRIVING":
                holder.transitModeImage.setImageResource(R.drawable.car);
                break;
            case "WALKING":
                holder.transitModeImage.setImageResource(R.drawable.walk);
                break;
            case "BICYCLING":
                holder.transitModeImage.setImageResource(R.drawable.cycle);
                break;
        }
        return convertView;
    }

//    @Override
//    public int getCount()   {
//        return 1;
//    }

    public void sort(int min, int max)  {
        int i = min;
        int j = max;
        double pivot = valuesArray[min + (max - min) / 2];

        while(i <= j)   {
            while(valuesArray[i] < pivot)    {
                i++;
            }

            while(valuesArray[j] > pivot)    {
                j--;
            }

            if(i <= j)  {
                int ai = list[i];
                double bi = valuesArray[i];
                int aj = list[j];
                double bj = valuesArray[j];
                list[i] = aj;
                valuesArray[i] = bj;
                list[j] = ai;
                valuesArray[j] = bi;
                i++;
                j--;
            }
        }

        if(min < j) {
            sort(min, j);
        }

        if(i < max) {
            sort(i, max);
        }
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static class ViewHolder	{
        TextView distance;
        TextView duration;
        TextView price;
        TextView departAt;
        TextView arriveAt;
        ImageView transitModeImage;
    }
}
