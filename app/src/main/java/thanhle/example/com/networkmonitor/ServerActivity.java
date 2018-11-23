package thanhle.example.com.networkmonitor;

import de.uniba.ktr.Entities.Message;
import thanhle.example.com.networkmonitor.server.*;
import com.muddzdev.styleabletoast.StyleableToast;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;

public class ServerActivity extends AppCompatActivity {

    EditText serverListeningPort;
    EditText logView;

    Button startButton;
    Button stopButton;

    ServerActivityHandler myHandler;

    // threading
    private String protocol;
    private Thread producerThread;
    private Thread consumerThread;

    private Queue shareQ;
    private final int maxSize = 10;

    // processors
    private AndroidUdpServerProcessor udpServerProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        Intent intent = getIntent();
        protocol = (String)intent.getSerializableExtra("Protocol");
        setTitle(getResources().getString(R.string.app_name) + " - Server - " + protocol);

        // map controls
        serverListeningPort = findViewById(R.id.serverListeningPort);
        logView = findViewById(R.id.logView);

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        // create handler
        myHandler = new ServerActivityHandler(this);

        // create queue
        shareQ = new LinkedList<Message>();

        // handle control event methods
        startButton.setOnClickListener((v) -> {
            startServer();
        });

        stopButton.setOnClickListener((v -> {
            stopServer();
        }));
    }

    // start & stop server
    private void startServer() {

        // create & start consumer thread
        consumerThread = new ConsumerThread(shareQ, myHandler);
        consumerThread.setDaemon(true);
        consumerThread.start();

        // create & start producer thread
        if(protocol.equals("TCP")) {
            producerThread = new TcpProducerThread(
                    Integer.parseInt(serverListeningPort.getText().toString()),
                    shareQ, maxSize, myHandler);
        } else {
            udpServerProcessor = new AndroidUdpServerProcessor(
                Integer.parseInt(serverListeningPort.getText().toString()),
                shareQ, maxSize, myHandler);

            producerThread = new Thread(udpServerProcessor);
        }

        producerThread.setDaemon(true);
        producerThread.start();
    }

    private void stopServer() {
        if(protocol.equals("TCP")) {
            producerThread.interrupt();
        } else {
            udpServerProcessor.terminate();
        }
        consumerThread.interrupt();
    }

    // show toast
    private void showToast(String msg, boolean success) {
        StyleableToast.makeText(this, msg, Toast.LENGTH_LONG,
                success? R.style.mySuccessToast: R.style.myFailedToast).show();
    }

    // append log
    private void appendLog(String msg) {
        logView.append(msg + "\n");
    }

    // handler extension
    public static class ServerActivityHandler extends Handler {
        public static final int NOTIFY_RECEIVE_MESSAGE = 0;
        public static final int APPEND_LOG_MESSAGE = 1;
        public static final int NOTIFY_ERROR_MESSAGE = 2;

        private ServerActivity parent;

        public ServerActivityHandler(ServerActivity parent) {
            this.parent = parent;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case NOTIFY_RECEIVE_MESSAGE:
                    parent.showToast((String)msg.obj, true);
                    break;
                case APPEND_LOG_MESSAGE:
                    parent.appendLog((String)msg.obj);
                    break;
                case NOTIFY_ERROR_MESSAGE:
                    parent.showToast((String)msg.obj, false);
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
