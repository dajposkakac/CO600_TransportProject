package maly.daj.nationalrailtest;

/**
 * Created by daj on 18/10/2015.
 */
import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;

import java.io.IOException;

import javax.security.auth.login.LoginException;

public class MyClient {

    //Network Rail ActiveMQ server
    private static final String SERVER = "datafeeds.networkrail.co.uk";

    // Server port for STOMP clients
    private static final int PORT = 61618;

    // Your account username, typically an email address
    private static final String USERNAME = "jg404@kent.ac.uk";

    // Your account password
    private static final String PASSWORD = "Eminem4life!";

    // Example topic (this one is for Southern Train Movements)
    private static final String TOPIC = "/topic/TRAIN_MVT_ALL_TOC";

    private Client client;

    public MyClient() {
        try {
            client = new Client(SERVER, PORT, USERNAME, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Connect to a single topic and subscribe a listener
     * @throws Exception Too lazy to implement exception handling....
     */
    public void go() throws Exception {
        System.out.println("| Connecting...");

        if (client.isConnected()) {
            System.out.println("| Connected to " + SERVER + ":" + PORT);
        } else {
            System.out.println("| Could not connect");
            return;
        }

    }

    public void setListener(Listener listener)  {
        client.subscribe(TOPIC , listener);
        System.out.println("| Subscribing...");
        System.out.println("| Subscribed to " + TOPIC);
        System.out.println("| Waiting for message...");
    }
}