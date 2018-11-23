package thanhle.example.com.networkmonitor.server;

import de.uniba.ktr.Entities.Message;
import thanhle.example.com.networkmonitor.ServerActivity;

import java.util.Queue;

public class ConsumerThread extends Thread {

    private Queue shareQ;
    private ServerActivity.ServerActivityHandler activityHandler;

    public ConsumerThread(Queue shareQ, ServerActivity.ServerActivityHandler handler) {
        this.shareQ = shareQ;
        this.activityHandler = handler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (shareQ) {
                    while (shareQ.isEmpty()) {
                        shareQ.wait();
                    }
                }

                // get message
                Message message = (Message) shareQ.poll();

                // append to log
                activityHandler.sendMessage(android.os.Message.obtain(
                        activityHandler, ServerActivity.ServerActivityHandler.APPEND_LOG_MESSAGE,
                        message.shortDescription()));

                synchronized (shareQ) {
                    shareQ.notify();
                }

            } catch (InterruptedException ie) {
                activityHandler.sendMessage(android.os.Message.obtain(activityHandler,
                        ServerActivity.ServerActivityHandler.NOTIFY_RECEIVE_MESSAGE,
                        "Thread interrupted!"));
            }
        }
    }
}
