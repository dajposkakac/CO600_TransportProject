package transportapp.co600.journeyorganiserapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
    private String errorMessage;
    private HashMap<String, String> info;
    private LinkedHashMap<Integer, HashMap<String, String>> results;

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
            }   else    {
                parseErrorMesage(xmlDoc);
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
            ArrayList<Integer> keys = new ArrayList<>();
            ArrayList<HashMap<String, String>> values = new ArrayList<>();
            Iterator<Integer> iterator = results.keySet().iterator();
            while(iterator.hasNext())   {
                Integer x = iterator.next();
                keys.add(x);
                values.add(results.get(x));
            }
            Intent intent = new Intent(activity, ResultsActivity.class);
            intent.putExtra("info", info);
            intent.putExtra("results_keys", keys);
            intent.putExtra("results_values", values);
            activity.startActivity(intent);
        }   else    {
            activity.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            ErrorDialogFragment.errorDialog(activity, "Error", status, errorMessage);
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
            i++;
        }
    }

    private void parseErrorMesage(Document doc) {
        NodeList nl = doc.getFirstChild().getChildNodes();
        boolean found = false;
        int i = 0;
        while(!found && i < nl.getLength()) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("errorMessage")) {
                errorMessage = n.getTextContent();
                found = true;
            }
            i++;
        }
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
        NodeList nl = doc.getFirstChild().getChildNodes();
        results = new LinkedHashMap<>(nl.getLength());
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

    private LinkedHashMap<String,String> parseToMap(Document doc)	{
        NodeList nodes = doc.getFirstChild().getChildNodes();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for(int i = 0; i < nodes.getLength(); i++)  {
            Node n = nodes.item(i);
            String nodeName = n.getNodeName();
            map.put(nodeName, n.getTextContent());
        }
        return map;
    }


}
