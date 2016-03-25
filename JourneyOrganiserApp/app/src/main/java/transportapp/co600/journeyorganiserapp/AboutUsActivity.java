package transportapp.co600.journeyorganiserapp;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Displays the About Us page.
 *
 * @author mfm9
 */
public class AboutUsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_us);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    }
}
