package testingserver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Peter Gerhat
 * Date: May 5, 2010
 * Time: 3:49:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationReader {

    private static ConfigurationReader instance;
    private static BufferedReader serverConfig;
    private static BufferedReader testConfig;

    public ConfigurationReader(String serverConfigPath, String testConfigPath) {
        try {
            serverConfig = new BufferedReader(new FileReader(serverConfigPath));
            testConfig = new BufferedReader(new FileReader(testConfigPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static ConfigurationReader getInstance() {
        if (instance == null) {
            instance = new ConfigurationReader(Main.SERVER_CONFIG_PATH, Main.TEST_CONFIG_PATH);
        }
        return instance;
    }

    public static String query(String query) {
        //Main.debug(query,"");
        String ret = "";
        ret = read(testConfig,query);
        if (!ret.equals("")){
            return ret;    
        }
        ret = read(serverConfig,query);
        return ret;
    }

    private static String read(BufferedReader reader, String query) {
        String ret = "";
        String name = "";
        try {
            while (true) {
                ret = reader.readLine();
                if (ret == null) {
                    break;
                }
                name = ret.split("=")[0].trim();
                if (name.contains(query)) {
                    //Main.debug(ret.split("=")[1].trim(),"");
                    return ret.split("=")[1].trim();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }

}
