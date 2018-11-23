package thanhle.example.com.networkmonitor.server;

import android.os.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

import thanhle.example.com.networkmonitor.ServerActivity;

public class TcpProducerThread extends Thread {

    private int serverPort;
    private ServerActivity.ServerActivityHandler activityHandler;

    private Queue shareQ;
    private int maxSize;

    // socket property
    private ServerSocket tcpSocket;

    // constructors
    public TcpProducerThread(int port, Queue shareQ, int maxSize,
                             ServerActivity.ServerActivityHandler handler) {
        this.serverPort = port;
        this.shareQ = shareQ;
        this.maxSize = maxSize;
        this.activityHandler = handler;
    }

    @Override
    public void run() {
        if(createSocket()) {
            activityHandler.sendMessage(Message.obtain(
                    activityHandler, ServerActivity.ServerActivityHandler.NOTIFY_RECEIVE_MESSAGE,
                    "Begin listening to tcp packets at port:" + serverPort));
            try {
                while (true) {
                    Socket socket = tcpSocket.accept();

                    // print to handler
                    activityHandler.sendMessage(Message.obtain(
                        activityHandler, ServerActivity.ServerActivityHandler.APPEND_LOG_MESSAGE,
                        "New client connected"));

                    Thread newClientThread = new Thread(new AndroidTcpServerProcessor(socket,
                            shareQ, maxSize, activityHandler));
                    newClientThread.setDaemon(true);
                    newClientThread.start();
                }
            } catch (IOException e) {
                activityHandler.sendMessage(Message.obtain(
                        activityHandler, ServerActivity.ServerActivityHandler.NOTIFY_ERROR_MESSAGE,
                        "Error: " + e.getMessage()));
            }
        } else {
            activityHandler.sendMessage(Message.obtain(
                activityHandler, ServerActivity.ServerActivityHandler.NOTIFY_ERROR_MESSAGE,
                "Cannot create TCP socket"));
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        try {
            tcpSocket.close();
        } catch (IOException e) { }
    }

    private boolean createSocket() {
        try {
            tcpSocket = new ServerSocket(this.serverPort);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
