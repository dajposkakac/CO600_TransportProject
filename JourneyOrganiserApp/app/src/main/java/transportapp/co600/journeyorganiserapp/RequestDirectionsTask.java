package transportapp.co600.journeyorganiserapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by daj on 18/11/2015.
 */
public class RequestDirectionsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "RequestDIR";
    private static final String SERVER_IP_PREF_KEY = "pref_server_ip";
    private static final int SERVER_PORT = 4444;
    private static final int SERVER_TIMEOUT_MS = 5000;
    private final Activity activity;
    private final Request req;
    private Socket socket;
    private int status;
    private String errorMessage;

    public RequestDirectionsTask(Activity pActivity, Request pReq)   {
        activity = pActivity;
        req = pReq;
    }

    @Override
    protected String doInBackground(String... params) {
        String ip = PreferenceManager.getDefaultSharedPreferences(activity).getString(SERVER_IP_PREF_KEY, activity.getResources().getString(R.string.default_server_ip));
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, SERVER_PORT), SERVER_TIMEOUT_MS);
            PrintWriter printwriter = new PrintWriter(socket.getOutputStream(), true);
            String xmlReq = createXMLRequest(req);
            Log.d(TAG, xmlReq);
            printwriter.write(xmlReq); //write the message to output stream
            printwriter.flush();
        }   catch(SocketTimeoutException ste)   {
            status = -1;
            errorMessage = "Server at " + ip + ":" + SERVER_PORT + " is unavailable.";
            Log.e(TAG, errorMessage);
        }   catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if(status == 0) {
            activity.findViewById(R.id.loading).setVisibility(View.VISIBLE);
            new ReceiveDirectionsTask(activity, socket).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            //        printwriter.close();
            Log.d("RequestRES", result);
        }   else    {
            ErrorDialogFragment.errorDialog(activity, "Error", status, errorMessage);
        }
    }

    private String createXMLRequest(Request req) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element request = xmlDoc.createElement("request");
        xmlDoc.appendChild(request);

        Element origin = xmlDoc.createElement("origin");
        Element destination = xmlDoc.createElement("destination");
        Element transitMode = xmlDoc.createElement("transitMode");
        Element time = xmlDoc.createElement("time");
        Element date = xmlDoc.createElement("date");
        Element departureOption = xmlDoc.createElement("departureOption");

        origin.appendChild(xmlDoc.createTextNode(req.getOrigin()));
        destination.appendChild(xmlDoc.createTextNode(req.getDestination()));
        transitMode.appendChild(xmlDoc.createTextNode(req.getTransitMode()));
        time.appendChild(xmlDoc.createTextNode(req.getTime()));
        date.appendChild(xmlDoc.createTextNode(req.getDate()));
        departureOption.appendChild(xmlDoc.createTextNode(req.getDepartureOption()));

        request.appendChild(origin);
        request.appendChild(destination);
        request.appendChild(transitMode);
        request.appendChild(time);
        request.appendChild(date);
        request.appendChild(departureOption);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult sr = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlDoc);
        transformer.transform(source, sr);
        return sr.getWriter().toString() + "\n";
    }

}
