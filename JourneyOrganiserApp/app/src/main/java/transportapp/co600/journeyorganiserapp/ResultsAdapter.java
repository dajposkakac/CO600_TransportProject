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

/**
 * Adapter displaying results sorted based on the sort parameter given to the constructor.
 *
 * @author jg404, mfm9
 */
public class ResultsAdapter extends ArrayAdapter<HashMap<String, String>> {

    private final Context context;
    private static int layout;
    private final HashMap<String, String> info;
    private final ArrayList<HashMap<String, String>> results;
    private final double[] valuesArray;

    public ResultsAdapter(Context pContext, HashMap<String, String> pInfo, ArrayList<HashMap<String, String>> pResults, String sort) {
        super(pContext, layout = R.layout.result_row);
        context = pContext;
        info = pInfo;
        results = new ArrayList<>(pResults);
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

        addAll(results);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)   {
        ViewHolder holder;
        HashMap<String, String> result = getItem(position);

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
        String price = result.get("price");
        if(price.equals("-1"))  {
            holder.price.setVisibility(View.GONE);
            convertView.findViewById(R.id.price_label).setVisibility(View.GONE);
        }   else    {
            holder.price.setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.price_label).setVisibility(View.VISIBLE);
            holder.price.setText("£" + price);
        }
        String transitMode = result.get("transitMode");
        holder.distance.setText(result.get("distance"));
        holder.duration.setText(result.get("duration"));

        String departureTime = result.get("departureTime");
        String arrivalTime = result.get("arrivalTime");
        String departureDate = result.get("departureDate");
        String arrivalDate = result.get("arrivalDate");
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

    /**
     * Standart quicksort, with the exception that it sorts an ArrayList
     * and an array at the same time.
     * @param min
     * @param max
     */
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
                double valj = valuesArray[j];
                valuesArray[i] = valj;
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
