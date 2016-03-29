package transportapp.co600.journeyorganiserapp;

import android.app.Activity;
import android.os.AsyncTask;
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
 * AsyncTask which makes a socket connection to the server and writes the
 * request in XML form. If the connection was successful, then ReceiveDirectionsTask
 * is started, otherwise an error message is shown.
 *
 * @author jg404
 */
public class RequestDirectionsTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "REQUEST";
    private static final int SERVER_PORT = 4444;
    private static final int SERVER_TIMEOUT_MS = 5000;
    private final Activity activity;
    private final Request req;
    private Socket socket;
    private int status;
    private String errorMessage;
    private PrintWriter printwriter;

    public RequestDirectionsTask(final Activity pActivity, final Request pReq)   {
        activity = pActivity;
        req = pReq;
    }

    @Override
    protected String doInBackground(String... params) {
        String ip = PreferenceManager.getDefaultSharedPreferences(activity).getString(activity.getString(R.string.pref_ip_server_key), activity.getString(R.string.default_server_ip));
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, SERVER_PORT), SERVER_TIMEOUT_MS);
            printwriter = new PrintWriter(socket.getOutputStream(), true);
            final String xmlReq = createXMLRequest(req);
            Log.d(TAG, xmlReq);
            printwriter.write(xmlReq); //write the message to output stream
            printwriter.flush();
        }   catch(SocketTimeoutException ste)   {
            status = -1;
            errorMessage = "Server at " + ip + activity.getString(R.string.colon) + SERVER_PORT + " is unavailable.";
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
            new ReceiveDirectionsTask(activity, socket, printwriter).execute();
        }   else    {
            ErrorDialogFragment.errorDialog(activity, "Server Error", status, errorMessage);
        }
    }

    /**
     * Converts Request into a single line XML string ended with a new line.
     */
    private String createXMLRequest(final Request req) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        final Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        final Element request = xmlDoc.createElement(activity.getString(R.string.request_xml_tag));
        xmlDoc.appendChild(request);

        final Element origin = xmlDoc.createElement(activity.getString(R.string.origin_xml_tag));
        final Element destination = xmlDoc.createElement(activity.getString(R.string.destination_xml_tag));
        final Element transitMode = xmlDoc.createElement(activity.getString(R.string.transit_mode_xml_tag));
        final Element time = xmlDoc.createElement(activity.getString(R.string.time_xml_tag));
        final Element date = xmlDoc.createElement(activity.getString(R.string.date_xml_tag));
        final Element departureOption = xmlDoc.createElement(activity.getString(R.string.departure_option_xml_tag));
        final Element sortingPreferences = xmlDoc.createElement(activity.getString(R.string.sorting_preference_xml_tag));

        origin.appendChild(xmlDoc.createTextNode(req.getOrigin()));
        destination.appendChild(xmlDoc.createTextNode(req.getDestination()));
        transitMode.appendChild(xmlDoc.createTextNode(req.getTransitMode()));
        time.appendChild(xmlDoc.createTextNode(req.getTime()));
        date.appendChild(xmlDoc.createTextNode(req.getDate()));
        departureOption.appendChild(xmlDoc.createTextNode(req.getDepartureOption()));
        sortingPreferences.appendChild(xmlDoc.createTextNode(req.getSortingPreference()));

        request.appendChild(origin);
        request.appendChild(destination);
        request.appendChild(transitMode);
        request.appendChild(time);
        request.appendChild(date);
        request.appendChild(departureOption);
        request.appendChild(sortingPreferences);

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final StreamResult sr = new StreamResult(new StringWriter());
        final DOMSource source = new DOMSource(xmlDoc);
        transformer.transform(source, sr);
        return sr.getWriter().toString() + activity.getString(R.string.new_line);
    }

}
