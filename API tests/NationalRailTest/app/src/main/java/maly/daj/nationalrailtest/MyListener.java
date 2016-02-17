package maly.daj.nationalrailtest;

/**
 * Created by daj on 18/10/2015.
 */
import android.content.Context;
import android.util.Log;

import net.ser1.stomp.Listener;

import java.io.FileOutputStream;
import java.util.Map;

public class MyListener implements Listener {

    private Context context;
    public MyListener(Context pContext)  {
        context = pContext;
    }


    @Override
    public void message(Map header, String body) {
        System.out.println("| Got header: " + header);
        passOn(body);
    }

    private String passOn(String data)   {
        return data;
        try {
            outputStream = context.openFileOutput.(filename, Context.MODE_PRIVATE);
        }
    }
}
