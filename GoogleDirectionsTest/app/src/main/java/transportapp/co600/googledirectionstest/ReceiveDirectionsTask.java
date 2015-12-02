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
    private String result = "hey";

    @Override
    protected String doInBackground(String... params) {
        try {
            Socket client = new Socket("86.170.118.205", 4444); //connect to server
            PrintWriter printwriter = new PrintWriter(client.getOutputStream(), true);
            printwriter.write(params[0] + "!.!" + params[1]); //write the message to output stream
            printwriter.flush();
            printwriter.close();

            //client.close(); //closing the connection
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("ReceiveRES", result);
    }
}
