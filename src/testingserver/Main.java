/*
 * Main.java
 *
 * This file is part of a tutorial on making a chat application using Flash
 * for the clients and Java for the multi-client server.
 *
 * View the tutorial at http://www.broculos.net/
 */



package testingserver;

import java.util.Date;

/**
 * Main is used to start the servers and handle the debug messages.
 *
 * @author Peter Gerhat
 */

import java.text.SimpleDateFormat;

public class Main {

    public static final String SERVER_CONFIG_PATH = "./configuration/serverconfig.conf";
    public static final String TEST_CONFIG_PATH   = "./configuration/testconfig.conf";

    public static UIServer uiServer;
    public static PolicyServer policyServer;
    public static Date currentDate = new Date();
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     *  If debug is enabled writes the message through the GUI.
     *
     * @param label the label of the message to write
     * @param msg the message to write
     */
    public static void debug(String label, String msg) {
        currentDate = new Date();
        System.out.println( "[" + dateFormat.format(currentDate) + "] " + label + msg);
    }

    /**
     * Starts the chat server, the policy server and the GUI for debug messages.
     *
     * @param args the command line arguments (first is the chat server port and second is the policy server port)
     */
    public static void main(String[] args) {

            ConfigurationReader.getInstance();

            PolicyServer policyServer = new PolicyServer(Integer.valueOf(ConfigurationReader.query("policyport")));
            policyServer.start();

            UIServer uiServer = new UIServer(Integer.valueOf(ConfigurationReader.query("uiport")));
            uiServer.start();

            Main.uiServer = uiServer;
            Main.policyServer = policyServer;
       
    }
}
