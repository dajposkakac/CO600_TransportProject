package transportapp.co600.journeyorganiserapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Homo King on 24/03/2016.
 */
public class AboutUsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        view.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
        return inflater.inflate(R.layout.about_us, container, false);
    }


}
