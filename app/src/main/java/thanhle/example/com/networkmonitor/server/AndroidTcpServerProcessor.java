package thanhle.example.com.networkmonitor.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;

import de.uniba.ktr.Processors.TcpServerProcessor;
import de.uniba.ktr.Entities.Message;
import thanhle.example.com.networkmonitor.ServerActivity;

public class AndroidTcpServerProcessor extends TcpServerProcessor {

    private ServerActivity.ServerActivityHandler activityHandler;

    private Queue shareQ;
    private int maxSize;

    public AndroidTcpServerProcessor(Socket socket, Queue shareQ, int maxSize,
                                     ServerActivity.ServerActivityHandler handler) {
        super(socket);
        this.shareQ = shareQ;
        this.maxSize = maxSize;
        this.activityHandler = handler;
    }

    // overridden methods
    @Override
    public void run() {
        this.owner = Thread.currentThread();

        // each instance of this TcpServerProcessor will serve exactly one TCP connection
        try {
            ObjectOutputStream out = new ObjectOutputStream(this.lSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(
                    new BufferedInputStream(this.lSocket.getInputStream()));

            // convert stream into message
            Message message = (Message) in.readObject();

            // check if the queue is still having enough space
            synchronized (this.shareQ) {
                while (this.shareQ.size() == this.maxSize) {
                    this.shareQ.wait();
                }
            }

            this.shareQ.add(message);

            synchronized (this.shareQ) {
                this.shareQ.notify();
            }

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            activityHandler.sendMessage(android.os.Message.obtain(
                    activityHandler, ServerActivity.ServerActivityHandler.NOTIFY_ERROR_MESSAGE,
                    "Exception " + e.getMessage()));
        }
    }

    @Override
    public void terminate() {
        this.owner.interrupt();
    }
}
