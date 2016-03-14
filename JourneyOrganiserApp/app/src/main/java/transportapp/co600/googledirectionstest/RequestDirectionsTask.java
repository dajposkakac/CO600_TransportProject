package transportapp.co600.googledirectionstest;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by daj on 18/11/2015.
 */
public class RequestDirectionsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "RequestDIR";
    private static final String SERVER_IP = "86.130.133.72";
    private static final int SERVER_PORT = 4444;
    private final Activity activity;
    private final Request req;
    private Socket socket;

    public RequestDirectionsTask(Activity pActivity, Request pReq)   {
        activity = pActivity;
        req = pReq;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            PrintWriter printwriter = new PrintWriter(socket.getOutputStream(), true);
            String xmlReq = createXMLRequest(req);
            Log.d(TAG, xmlReq);
            printwriter.write(xmlReq); //write the message to output stream
            printwriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        activity.findViewById(R.id.loading).setVisibility(View.VISIBLE);
        new ReceiveDirectionsTask(activity, socket).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
//        printwriter.close();
        Log.d("RequestRES", result);
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
        transitMode.appendChild(xmlDoc.createTextNode(req.getTransitMode().toString()));
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
