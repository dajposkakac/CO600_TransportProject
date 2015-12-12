package transportapp.co600.googledirectionstest;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by daj on 18/11/2015.
 */
public class RequestDirectionsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "RequestDIR";
    private final Activity activity;
    private final Request req;
    private Socket socket;
    private PrintWriter printwriter;

    public RequestDirectionsTask(Activity pActivity, Request pReq)   {
        activity = pActivity;
        req = pReq;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            socket = new Socket("109.155.198.170", 4444);
            printwriter = new PrintWriter(socket.getOutputStream(), true);
            String s = req.getOrigin() + "!.!" + req.getDestination() + "!.!" + req.getTransitMode() + "\n";
            Log.d(TAG, s);
            printwriter.write(s); //write the message to output stream
            printwriter.flush();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        new ReceiveDirectionsTask(activity, socket).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
//        printwriter.close();
        Log.d("RequestRES", result);
    }
}
