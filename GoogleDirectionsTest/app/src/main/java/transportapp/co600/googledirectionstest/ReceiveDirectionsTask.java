package transportapp.co600.googledirectionstest;

import android.app.Activity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by daj on 18/11/2015.
 */
public class ReceiveDirectionsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "ReceiveDIR";
    private Activity activity;
    private Socket socket;
    private BufferedReader bufferedReader;

    public ReceiveDirectionsTask(Activity pActivity, Socket pSocket)   {
        activity = pActivity;
        socket = pSocket;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
//            XmlResourceParser xmlFile = activity.getResources().getXml(R.xml.RequestTemplate);
//            if (xmlFile.getEventType() == XmlPullParser.START_TAG)  {
//                String name = xmlFile.getName();
//                if(name.equals("request"))  {
//                    xmlFile.next();
//                    if(xmlFile.getName() != null && xmlFile.getName().equals("origin")) {
//                        req.setOrigin(xmlFile.getText());
//                        xmlFile.next();
//                    }
//                    if(xmlFile.getName() != null && xmlFile.getName().equals("destination"))    {
//                        req.setOrigin(xmlFile.getText());
//                        xmlFile.next();
//                    }
//                    if(xmlFile.getName() != null && xmlFile.getName().equals("transitMode"))    {
//                        req.setTransitMode(xmlFile.getText());
//                        xmlFile.next();
//                    }
//                }
//            }
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); //get the client message
            Log.d("result", bufferedReader.readLine());
            //Log.d(TAG, s);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }   finally {
            try {
                bufferedReader.close();
                socket.close(); //closing the connection
            } catch (IOException e) {}
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("ReceiveRES", result);
        activity.startActivity(new Intent(activity, ResultsActivity.class));
    }
}
