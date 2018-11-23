package thanhle.example.com.networkmonitor.client;

import de.uniba.ktr.Entities.Message;
import de.uniba.ktr.Libs.Helper;
import thanhle.example.com.networkmonitor.ClientActivity;

import java.io.*;
import java.net.*;

public class AndroidUdpClientProcessor implements Runnable {
    private final String serverHostname;
    private final int serverListeningPort;
    private final Message message;
    ClientActivity.ClientActivityHandler activityHandler;

    public AndroidUdpClientProcessor(Message message, String serverHostname,
                                     int listeningPort, ClientActivity.ClientActivityHandler handler) {
        this.serverHostname = serverHostname;
        this.serverListeningPort = listeningPort;
        this.message = message;
        this.activityHandler = handler;
    }

    @Override
    public void run() {
        try {
            // create server information
            InetAddress serverAddr = InetAddress.getByName(serverHostname);

            // create socket
            DatagramSocket sSocket = new DatagramSocket(serverListeningPort);

            // convert message to byte array
            byte[] msg_contain = Helper.objectToStream(message);
            DatagramPacket packet = new DatagramPacket(msg_contain, msg_contain.length,
                    serverAddr, serverListeningPort);

            // send packet
            sSocket.send(packet);

            // close socket
            sSocket.close();

            // display dialog
            activityHandler.sendMessage(android.os.Message.obtain(activityHandler,
                    ClientActivity.ClientActivityHandler.UPDATE_STATUS_SUCCESS,
                    "Send packet successfully!"));
        }
        catch (UnknownHostException e) {
            activityHandler.sendMessage(android.os.Message.obtain(activityHandler,
                    ClientActivity.ClientActivityHandler.UPDATE_STATUS_FAILED,
                    "Unknown hostname error!"));
        }
        catch (SocketException e) {
            activityHandler.sendMessage(android.os.Message.obtain(activityHandler,
                    ClientActivity.ClientActivityHandler.UPDATE_STATUS_FAILED,
                    "Socket exception error"));
        }
        catch (IOException e) {
            activityHandler.sendMessage(android.os.Message.obtain(activityHandler,
                    ClientActivity.ClientActivityHandler.UPDATE_STATUS_FAILED,
                    "Socket exception error"));
        }
    }
}
