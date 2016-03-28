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
    private final ArrayList<HashMap<String, String>> results;
    private final double[] valuesArray;
    private int[] list;

    public ResultsAdapter(Context pContext, HashMap<String, String> pInfo, ArrayList<HashMap<String, String>> pResults, String sort) {
        super(pContext, layout = R.layout.result_row);
        context = pContext;
        info = pInfo;
        results = new ArrayList<>(pResults);
        ArrayList<HashMap<String, String>> resultsCopy = new ArrayList<>();
        resultsCopy.addAll(results);

//        list = new int[results.size()];
//        for(int i =0; i < list.length; i++) {
//            list[i] =  i;
//        }

        valuesArray = new double[results.size()];

        for (int j = 0; j < results.size(); j++)   {
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
                        if(info.get("departureOption").startsWith("Arrive"))  {
                            valuesArray[j] = Double.valueOf(value);
                        }
                        else {
                            sort = "departureTimeInSeconds";
                            j--;
                        }
                    break;
                case "departureTimeInSeconds":
                    value = values;
                    if(info.get("departureOption").startsWith("Depart"))  {
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
        sort(0, results.size() - 1);

//        for(int i = 0; i < results.size(); i++) {
//            results.add(i, resultsCopy.get(list[i]));
//        }


        for(int i = 0; i < results.size(); i++)   {
            add(String.valueOf(i));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)   {
        ViewHolder holder;
        String price = results.get(position).get("price");
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
//            if(price.equals("-1"))  {
//                holder.price.setVisibility(View.GONE);
//                convertView.findViewById(R.id.price_label).setVisibility(View.GONE);
//            }
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
        String departureDate = String.valueOf(results.get(position).get("departureDate"));
        String arrivalDate = String.valueOf(results.get(position).get("arrivalDate"));
        String departureTimeDate = departureTime + " - " + departureDate;
        String arrivalTimeDate = arrivalTime + " - " + arrivalDate;
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
                HashMap<String, String> resulti = results.get(i);
                HashMap<String, String> resultj = results.get(j);

                double vali = valuesArray[i];
//                int listi = results.;
                double valj = valuesArray[j];
//                int listj = list[j];
//                list[i] = listj;
                valuesArray[i] = valj;
//                list[j] = listi;
                valuesArray[j] = vali;
                results.remove(i);
                results.add(i, resultj);
                results.remove(j);
                results.add(j, resulti);
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

    public ArrayList<HashMap<String, String>> getSortedResults()   {
        return results;
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
