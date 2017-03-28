package uom.synergen.fyp.smart_insole.gait_analysis.wifi;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import uom.synergen.fyp.smart_insole.gait_analysis.constants.SmartInsoleConstants;

public class WifiHandler {

    private static final String TAG = "Wifi Handler";

    private static WifiHandler wifiHandler;
    private ServerSocket serverSocket;
    private Socket[] clients;

    private WifiHandler() throws IOException {
        serverSocket = new ServerSocket(SmartInsoleConstants.WIFI_PORT);
        clients = new Socket[2];
    }

    public static WifiHandler getInstance() throws IOException {
        if (wifiHandler == null) {
            wifiHandler = new WifiHandler();
        }
        return wifiHandler;
    }

    public boolean clientConnect() throws IOException {

        for (int i = 0 ; i < 2 ; i++) {
            clients[i] = serverSocket.accept();
            Log.i(TAG, "Client " + (i +1) + " Connected.");
        }
        return true;
    }

    public Socket[] getClients() {
        return clients;
    }

}
