/*
 * ChatServerConnection.java
 *
 * This file is part of a tutorial on making a chat application using Flash
 * for the clients and Java for the multi-client server.
 *
 * View the tutorial at http://www.broculos.net/
 */

package testingserver;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

/**
 * The ChatServerConnection handles individual client connections to the chat server.
 *
 * @author Nuno Freitas (nunofreitas@gmail.com)
 */
public class UIServerConnection extends Thread {
    protected Socket socket;
    protected BufferedReader in;
    protected ZeroByteSeparatedXMLInputStream socketIn;
    protected PrintWriter socketOut;
    protected Scanner systemIn;
    protected UIServer server;
    protected DocumentBuilderFactory dbf;
    protected DocumentBuilder db;
    protected Document d;
    private byte[] line;
    private int status;

    /*
     * Creates a new instance of UIServerConnection.
     *
     * @param socket the client's socket connection
     * @param server the server to each the client is connected
     **/

    public UIServerConnection(Socket socket, UIServer server) {
        this.socket = socket;
        this.server = server;

    }

    /**
     * Gets the remote address of the client.
     *
     * @return the socket address of the client connection
     */
    public SocketAddress getRemoteAddress() {
        return this.socket.getRemoteSocketAddress();
    }

    /**
     * Roots a debug message to the main application.
     *
     * @param msg The debug message to be sent to the main application
     */
    protected void debug(String msg) {
        Main.debug("UIServerConnection (" + this.socket.getRemoteSocketAddress().toString().split(":")[8] + ")", msg);
    }

    private boolean test(String xml, String[] expressions) {

        if (expressions == null) {
            return false;
        }

        if (expressions.length == 0) {
            return false;
        }

        for (int i = 0; i < expressions.length; i++) {
            if (xml.indexOf(expressions[i]) == -1) {
                return false;
            }
        }
        return true;
    }

    private String[] getExpressions(File f) {
        String tmp = findSubstring(f, "<!--Expressions:");
        return (tmp == null) ? new String[0] : tmp.split(",");
    }

    private String getDescription(File f) {
        String ret = findSubstring(f, "<!--Description:");
        return (ret == null ? "Undefined" : ret);
    }

    private String findSubstring(File f, String substr) {
        String ret = null;
        int start = -1;
        int end = -1;
        try {
            in = new BufferedReader(new FileReader(f));
            String xmlLine;
            xmlLine = in.readLine();
            while (xmlLine != null) {
                if (xmlLine.length() > 19) {
                    start = xmlLine.indexOf(substr);
                    if (start != -1) {
                        start += substr.length();
                        end = xmlLine.indexOf("-->");
                        if (start < end && start > 0 && end > 0) {
                            return xmlLine.substring(start, end);
                        }
                        break;
                    }
                }
                xmlLine = in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return ret;
    }

    private String shorten(String str) {
        str = str.replaceAll("^<UIProtocol version=\"1.0\">\n","");
        str = str.replaceAll("\n</UIProtocol>","");
        str = str.replaceAll(">[^<>]*<","><");
        str = str.replaceAll("^[^<]*<","<");
        str = str.replaceAll(">[^>]*$",">");
        return str;
    }

    /**
     * Waits from messages from the client and then instructs the server to send the messages to all clients.
     */
    public void run() {
        String xml = "";
        status = 0;
        boolean foundReply;
        char action;
        String test;
        String[] expressions;
        File mydir = new File("./testxmls/");
        FilenameFilter select = new FileListFilter("uip.xml");
        File[] directoryList = mydir.listFiles(select);

        debug("init");
        do {
            try {

                initialize();
                line = new byte[500];
                socketIn.read(line);
                xml = new String(line);

                if (Boolean.parseBoolean(ConfigurationReader.query("debug"))) {
                    debug("Received:\n" + xml);
                } 

                foundReply = false;

                while (!foundReply) {

                    for (File f : directoryList) {
                        expressions = getExpressions(f);
                        if (test(xml, expressions)) {
                            sendXML(f);
                            foundReply = true;
                            break;
                        }
                    }
                    if (!foundReply) {
                        debug("Reply not found:\n" + shorten(xml)+"\nRetry? (y/n)");
                        action = systemIn.nextLine().charAt(0);
                        switch (action) {
                            case 'y':
                                continue;
                            case 'n':
                                foundReply = true;
                                break;
                            case 'i':
                                debug("Please input a valid filename from the \"./testxmls\" directory");
                                sendXML(new File("./testxmls/"+systemIn.nextLine()));
                                foundReply = true;
                                break;
                        }
                    } else {
                        break;
                    }

                }

            }
            catch (IOException e) {
                debug("Exception (IO): " + e.getMessage());
            }
        } while (xml.charAt(0) != '\u0000');
    }

    private void sendXML(File f) {

        try {
            debug("Sending:" + f.getName() + ", Description:" + getDescription(f));
            d = db.parse(f);
            String str;
            str = xmlToString(d.getLastChild()) + "\n" + "\u0000";
            socketOut.write(str);
            socketOut.flush();

        } catch (SAXException e) {

            debug("Can't read XML: SAX Exception");

        } catch (IOException e) {

            debug("Can't read XML: IO Exception");

        }

    }

    public static String xmlToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initialize() {

        try {

            this.dbf = DocumentBuilderFactory.newInstance();
            this.db = dbf.newDocumentBuilder();

            this.socketIn = new ZeroByteSeparatedXMLInputStream(this.socket.getInputStream());
            this.socketOut = new PrintWriter(this.socket.getOutputStream(), true);
            this.systemIn = new Scanner(System.in);

        } catch (IOException e) {

            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.

        } catch (ParserConfigurationException e) {

            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.

        }
    }

    /**
     * Sends a message to the connected party.
     *
     * @param msg the message to send
     */
    public void write(String msg) {
        try {

            this.socketOut.write(msg + "\u0000");
            this.socketOut.flush();

        }
        catch (Exception e) {

            debug("Exception (write): " + e.getMessage());

        }
    }

    /**
     * Closes the reader, the writer and the socket.
     */
    protected void finalize() {
        try {

            this.socketIn.close();
            this.socketOut.close();
            this.socket.close();
            debug("connection closed");

        }
        catch (Exception e) {

            debug("Exception (finalize): " + e.getMessage());

        }
    }
}