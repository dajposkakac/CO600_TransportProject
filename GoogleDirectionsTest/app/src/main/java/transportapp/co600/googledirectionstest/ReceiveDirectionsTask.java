package transportapp.co600.googledirectionstest;

import android.os.AsyncTask;
import android.util.Log;

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
    private Socket socket;
    private BufferedReader bufferedReader;

    public ReceiveDirectionsTask(Socket pSocket)   {
        socket = pSocket;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
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
        
    }
}
