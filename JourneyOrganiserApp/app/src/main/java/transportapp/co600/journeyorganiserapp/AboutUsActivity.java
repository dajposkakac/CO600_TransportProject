package transportapp.co600.journeyorganiserapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Displays the About Us page.
 *
 * @author mfm9
 */
public final class AboutUsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_us);

        setTitle(R.string.about_us_activity_title);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
    }
}
