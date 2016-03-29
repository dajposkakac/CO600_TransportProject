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

    public ResultsAdapter(final Context pContext, final HashMap<String, String> pInfo, final ArrayList<HashMap<String, String>> pResults, String sort) {
        super(pContext, layout = R.layout.result_row);
        context = pContext;
        info = pInfo;
        results = new ArrayList<>(pResults);
        valuesArray = new double[results.size()];

        for (int j = 0; j < results.size(); j++)   {
            final String values = results.get(j).get(sort);
            String value = null;
            switch (sort) {
                case "distance":
                    value = values.replaceAll("[\\D+]+$", "").replace(",", "");
                    if(isDouble(value)) {
                        valuesArray[j] = Double.valueOf(value);
                    }
                    else    {
                        valuesArray[j] = Integer.valueOf(value);
                    }
                    break;
                case "arrivalTimeInSeconds":
                    value = values;
                        if(info.get(context.getString(R.string.departure_option_xml_tag)).startsWith("Arrive"))  {
                            valuesArray[j] = Double.valueOf(value);
                        }
                        else {
                            sort = context.getString(R.string.departure_time_in_sec_xml_tag);
                            j--;
                        }
                    break;
                case "departureTimeInSeconds":
                    value = values;
                    if(info.get(context.getString(R.string.departure_option_xml_tag)).startsWith("Depart"))  {
                        valuesArray[j] = Double.valueOf(value);
                    }
                    else {
                        sort = context.getString(R.string.arrival_time_in_sec_xml_tag);
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
        final HashMap<String, String> result = getItem(position);

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
        final String price = result.get(context.getString(R.string.price_xml_tag));
        if(price.equals(context.getString(R.string.missing_price)))  {
            holder.price.setVisibility(View.GONE);
            convertView.findViewById(R.id.price_label).setVisibility(View.GONE);
        }   else    {
            holder.price.setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.price_label).setVisibility(View.VISIBLE);
            holder.price.setText(context.getString(R.string.pound_sign) + price);
        }
        final String transitMode = result.get(context.getString(R.string.transit_mode_xml_tag));
        holder.distance.setText(result.get(context.getString(R.string.distance_xml_tag)));
        holder.duration.setText(result.get(context.getString(R.string.duration_xml_tag)));

        final String departureTime = result.get(context.getString(R.string.departure_time_xml_tag));
        final String arrivalTime = result.get(context.getString(R.string.arrival_time_xml_tag));
        final String departureDate = result.get(context.getString(R.string.departure_date_xml_tag));
        final String arrivalDate = result.get(context.getString(R.string.arrival_date_xml_tag));
        final String departureTimeDate = departureTime + context.getString(R.string.spaced_dash) + departureDate;
        final String arrivalTimeDate = arrivalTime + context.getString(R.string.spaced_dash) + arrivalDate;
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
    public void sort(final int min, final int max)  {
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

    boolean isDouble(final String str) {
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
