package transportapp.co600.journeyorganiserapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Custom SupportMapFragment, with a transparent overlay which transfers touch events to the map,
 * allowing it to be used inside a ScrollView.
 *
 * Workaround found on:
 * http://stackoverflow.com/questions/30525066/how-to-set-google-map-fragment-inside-scroll-view
 *
 * @author jg404
 */
public class InterceptTouchMapFragment extends SupportMapFragment {
    private OnTouchListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        TouchInterceptor ti = new TouchInterceptor(getActivity());
        ti.setBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(), android.R.color.transparent));
        ((ViewGroup) layout).addView(ti, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return layout;
    }

    public void setListener(OnTouchListener pListener) {
        listener = pListener;
    }

    public interface OnTouchListener {
        void onTouch();
    }

    public class TouchInterceptor extends FrameLayout   {

        public TouchInterceptor(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    listener.onTouch();
                    break;
                case MotionEvent.ACTION_UP:
                    listener.onTouch();
                    break;
            }
            return super.dispatchTouchEvent(event);
        }
    }
}
