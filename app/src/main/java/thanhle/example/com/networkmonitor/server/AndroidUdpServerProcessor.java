package thanhle.example.com.networkmonitor.server;

import de.uniba.ktr.Processors.UdpServerProcessor;
import de.uniba.ktr.Entities.Message;
import de.uniba.ktr.Libs.Helper;
import thanhle.example.com.networkmonitor.ServerActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;

public class AndroidUdpServerProcessor extends UdpServerProcessor {

    private int listeningPort;
    private ServerActivity.ServerActivityHandler activityHandler;

    private Queue shareQ;
    private int maxSize;

    // constructors
    public AndroidUdpServerProcessor(int port, Queue shareQ, int maxSize,
                                          ServerActivity.ServerActivityHandler handler) {
        this.listeningPort = port;
        this.shareQ = shareQ;
        this.maxSize = maxSize;
        this.activityHandler = handler;
    }

    // overridden methods
    @Override
    public void run() {
        if(createSocket()) {
            owner = Thread.currentThread();

            // print to handler
            activityHandler.sendMessage(android.os.Message.obtain(
                activityHandler, ServerActivity.ServerActivityHandler.NOTIFY_RECEIVE_MESSAGE,
                "Begin listening to UDP packets at port:" + listeningPort));

            try {
                while (true) {
                    // simulate incoming datagram packet
                    byte[] buffer = new byte[super.BUFFER_SIZE];
                    DatagramPacket receivingPacket = new DatagramPacket(buffer, buffer.length);

                    // receive packet
                    this.lSocket.receive(receivingPacket);

                    // convert datagram packet to message
                    Message message = Helper.streamToMessage(receivingPacket.getData());

                    // check if queue is still having enough space
                    synchronized (shareQ) {
                        while (shareQ.size() == maxSize) {
                            shareQ.wait();
                        }
                    }

                    shareQ.add(message);

                    synchronized (shareQ) {
                        shareQ.notify();
                    }
                }
            } catch (InterruptedException e) {
                // print exception to handler
                activityHandler.sendMessage(android.os.Message.obtain(
                        activityHandler, ServerActivity.ServerActivityHandler.NOTIFY_ERROR_MESSAGE,
                        "Thread interrupted"));

            } catch (IOException e) {
                // print exception to handler
                activityHandler.sendMessage(android.os.Message.obtain(
                        activityHandler, ServerActivity.ServerActivityHandler.NOTIFY_ERROR_MESSAGE,
                        "IO Exception"));
            }
        } else {
            // print exception to handler
            activityHandler.sendMessage(android.os.Message.obtain(
                    activityHandler, ServerActivity.ServerActivityHandler.NOTIFY_ERROR_MESSAGE,
                    "Cannot create UDP socket"));
        }
    }

    @Override
    public void terminate() {
        lSocket.close();
        owner.interrupt();
    }

    // private methods
    private boolean createSocket() {
        try {
            lSocket = new DatagramSocket(listeningPort);
            return true;
        } catch (SocketException se) {
            return false;
        }
    }
}
