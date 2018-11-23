package thanhle.example.com.networkmonitor.client;

import de.uniba.ktr.Entities.Message;
import thanhle.example.com.networkmonitor.ClientActivity;

import java.io.*;
import java.net.*;

public class AndroidTcpClientProcessor implements Runnable {

    private final String serverHostname;
    private final int serverListeningPort;
    private final Message message;
    ClientActivity.ClientActivityHandler activityHandler;

    public AndroidTcpClientProcessor(Message message, String serverHostname,
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

            // create socket & object output stream
            Socket sSocket = new Socket(serverAddr, serverListeningPort);
            ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(sSocket.getOutputStream()));

            // send packet
            out.writeObject(this.message);
            out.flush();

            // close stream and socket
            out.close();
            sSocket.close();

            // show dialog via handler
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
