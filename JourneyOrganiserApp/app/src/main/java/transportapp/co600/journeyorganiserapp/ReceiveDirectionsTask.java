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
import java.io.PrintWriter;
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
 * Reads the response of the server and parses it into HashMaps. If the request was successful, ResultsActivity
 * is started and the HashMaps containing the results are passed to it. If something went wrong, an error dialog
 * with an error code and an error message is shown. Also cleans up objects for both AsyncTasks.
 *
 * @author jg404
 */
public class ReceiveDirectionsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "ReceiveDIR";
    private final PrintWriter printwriter;
    private Activity activity;
    private Socket socket;
    private BufferedReader bufferedReader;
    private int status;
    private String errorMessage;
    private HashMap<String, String> info;
    private LinkedHashMap<Integer, HashMap<String, String>> results;

    public ReceiveDirectionsTask(Activity pActivity, Socket pSocket, PrintWriter pPrintwriter)   {
        activity = pActivity;
        socket = pSocket;
        printwriter = pPrintwriter;
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
            printwriter.close();
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

    /**
     * Parses the status out of the response XML doc
     * @param doc XML response
     */
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

    /**
     * Parses the error message out of the XML doc
     * @param doc XML response
     */
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

    /**
     * Parses data from under Info tag out of the XML doc into
     * the info HashMap
     * @param doc XML response
     */
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

    /**
     * Parses data from under the Results tag out of the XML doc into
     * the results HashMap
     * @param doc XML response
     */
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

    /**
     * Parses all children of the specified node into the returned HashMap
     * @param node parent of children to be parsed
     * @return HashMap of tags mapped to values
     */
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

    /**
     *
     * @param doc XML response
     * @return parsed map
     */
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
