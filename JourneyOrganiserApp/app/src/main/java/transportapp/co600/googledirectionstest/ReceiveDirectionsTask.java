package transportapp.co600.googledirectionstest;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by daj on 18/11/2015.
 */
public class ReceiveDirectionsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "ReceiveDIR";
    private Activity activity;
    private Socket socket;
    private BufferedReader bufferedReader;
    private int status;
    private HashMap<String, String> info;
    private HashMap<Integer, HashMap<String, String>> results;

    public ReceiveDirectionsTask(Activity pActivity, Socket pSocket)   {
        activity = pActivity;
        socket = pSocket;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); //get the client message
            Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(bufferedReader.readLine())));
            parseStatus(xmlDoc);
            if(status == 0) {
                parseInfo(xmlDoc);
                parseResults(xmlDoc);
            }
            Log.d("result", "results parsed");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }   finally {
            try {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
                if(!socket.isClosed()) {
                    socket.close(); //closing the connection
                }
            } catch (IOException e) {}
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("ReceiveRES", result);
        if(status == 0) {
            Intent intent = new Intent(activity, ResultsActivity.class);
            intent.putExtra("info", info);
            intent.putExtra("results", results);
            activity.startActivity(intent);
        }   else    {
            activity.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            Bundle bundle = new Bundle();
            bundle.putInt("STATUS_KEY", status);
            ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
            errorDialogFragment.setArguments(bundle);
            activity.getFragmentManager().beginTransaction().add(errorDialogFragment, "errorDialog").commitAllowingStateLoss();
        }
    }

    private void parseStatus(Document doc)  {
        NodeList nl = doc.getFirstChild().getChildNodes();
        boolean found = false;
        int i = 0;
        while(!found && i < nl.getLength()) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("status")) {
                status = Integer.valueOf(n.getTextContent());
                found = true;
            }
        }
        i++;
    }

    private void parseInfo(Document doc)    {
        NodeList nl = doc.getFirstChild().getChildNodes();
        boolean found = false;
        int i = 0;
        while(!found && i < nl.getLength()) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("info")) {
                info = parseToMap(n);
                found = true;
            }
            i++;
        }
    }

    private void parseResults(Document doc) {
        results = new HashMap<>();
        NodeList nl = doc.getFirstChild().getChildNodes();
        boolean found = false;
        int i = 0;
        while(!found && i < nl.getLength()) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("results")) {
                NodeList resultsNodeList = n.getChildNodes();
                for(i = 0; i < resultsNodeList.getLength(); i++)    {
                    Node resultNode = resultsNodeList.item(i);
                    results.put(i, parseToMap(resultNode));
                }
                found = true;
            }
            i++;
        }
    }

    private HashMap<String,String> parseToMap(Node node)	{
        NodeList nodes = node.getChildNodes();
        HashMap<String, String> map = new HashMap<>();
        for(int i = 0; i < nodes.getLength(); i++)  {
            Node n = nodes.item(i);
            String nodeName = n.getNodeName();
            map.put(nodeName, n.getTextContent());
        }
        return map;
    }

    private HashMap<String,String> parseToMap(Document doc)	{
        NodeList nodes = doc.getFirstChild().getChildNodes();
        HashMap<String, String> map = new HashMap<>();
        for(int i = 0; i < nodes.getLength(); i++)  {
            Node n = nodes.item(i);
            String nodeName = n.getNodeName();
            map.put(nodeName, n.getTextContent());
        }
        return map;
    }


}