package transportapp.co600.googledirectionstest;

import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import org.w3c.dom.Document;
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
    private static final String SERVER_IP = "5.81.182.39";
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
        Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(activity.getResources().openRawResource(R.raw.request_template));
        NodeList nodes = xmlDoc.getFirstChild().getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++)  {
            Node n = nodes.item(i);
            String nn = n.getNodeName();
            if(nn.equals("origin"))    {
                n.setTextContent(req.getOrigin());
            }   else if(nn.equals("destination"))   {
                n.setTextContent(req.getDestination());
            }   else if(nn.equals("transitMode"))  {
                n.setTextContent(req.getTransitMode().toString());
            }
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult sr = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlDoc);
        transformer.transform(source, sr);
        return sr.getWriter().toString() + "\n";
    }

}
