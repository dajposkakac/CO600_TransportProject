package transportapp.co600.googledirectionstest;

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
public class ReceiveDirectionsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "ReceiveDIR";
    private final Request req;
    private String result = "hey";
    private Socket socket;

    public ReceiveDirectionsTask(Socket pSocket, Request pReq)   {
        socket = pSocket;
        req = pReq;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            PrintWriter printwriter = new PrintWriter(socket.getOutputStream(), true);
            String s = req.getOrigin() + "!.!" + req.getDestination() + "!.!" + req.getTransitMode();
            printwriter.write(s); //write the message to output stream
            printwriter.flush();
            printwriter.close();
            //Log.d(TAG, s);
            //client.close(); //closing the connection
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("ReceiveRES", result);
    }
}
