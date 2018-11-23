package thanhle.example.com.networkmonitor;

import de.uniba.ktr.Entities.Message;
import thanhle.example.com.networkmonitor.client.*;
import com.muddzdev.styleabletoast.StyleableToast;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

public class ClientActivity extends AppCompatActivity {

    EditText serverIPAddress;
    EditText serverListeningPort;
    EditText clientSendingPort;

    Button sendButton;
    EditText messageSubject;
    EditText messageContent;
    ClientActivityHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        Intent intent = getIntent();
        String protocol = (String)intent.getSerializableExtra("Protocol");
        setTitle(getResources().getString(R.string.app_name) + " - Client - " + protocol);

        // map controls
        serverIPAddress = findViewById(R.id.serverIPAddress);
        serverListeningPort = findViewById(R.id.serverListeningPort);
        clientSendingPort = findViewById(R.id.clientSendingPort);
        sendButton = findViewById(R.id.sendButton);
        messageSubject = findViewById(R.id.messageSubject);
        messageContent = findViewById(R.id.messageContent);

        // create handler
        myHandler = new ClientActivityHandler(this);

        // handle controls event
        sendButton.setOnClickListener((v) -> {
            if(protocol.equals("TCP")) {
                sendTcpPacket();
            }
            else {
                sendUdpPacket();
            }
        });
    }

    // private methods
    private void sendTcpPacket() {
        // create message
        Message message = new Message("abcd", messageSubject.getText().toString(),
                messageContent.getText().toString(), new Date());
        // create thread that handle sending TCP packet
        Thread tcpThread = new Thread(new AndroidTcpClientProcessor(
                message, serverIPAddress.getText().toString(),
                Integer.parseInt(serverListeningPort.getText().toString()),
                myHandler
        ));

        tcpThread.setDaemon(true);
        tcpThread.start();
    }

    private void sendUdpPacket() {
        // create message
        Message message = new Message("abcd", messageSubject.getText().toString(),
                messageContent.getText().toString(), new Date());

        // create thread that handle sending UDP packet
        Thread udpThread = new Thread(new AndroidUdpClientProcessor(
                message, serverIPAddress.getText().toString(),
                Integer.parseInt(serverListeningPort.getText().toString()),
                myHandler
        ));

        udpThread.setDaemon(true);
        udpThread.start();
    }

    private void showToast(String msg, boolean success) {
        StyleableToast.makeText(this, msg, Toast.LENGTH_LONG,
                success ? R.style.mySuccessToast : R.style.myFailedToast).show();
    }

    public static class ClientActivityHandler extends Handler {
        public static final int UPDATE_STATUS_SUCCESS = 0;
        public static final int UPDATE_STATUS_FAILED = 1;

        private ClientActivity parent;

        public ClientActivityHandler(ClientActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            String message = (String) msg.obj;
            switch (msg.what) {
                case UPDATE_STATUS_SUCCESS:
                    parent.showToast(message, true);
                    break;
                case UPDATE_STATUS_FAILED:
                    parent.showToast(message, false);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
