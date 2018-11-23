package thanhle.example.com.networkmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    Spinner screenSpinner;
    Spinner protocolSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // setup screen
        screenSpinner = findViewById(R.id.screenMode);
        protocolSpinner = findViewById(R.id.protocolMode);

        setupSpinner(screenSpinner, R.array.screen_mode, R.layout.spinner_item);
        setupSpinner(protocolSpinner, R.array.protocol_mode, R.layout.spinner_item);
    }

    private void setupSpinner(Spinner spinner, int adapterArrayId, int itemLayoutId) {
        String[] arrayList = getResources().getStringArray(adapterArrayId);

        // Initializing screen array adapter
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                itemLayoutId, arrayList) {
            @Override
            public boolean isEnabled(int position) {
                if(position == 0) {
                    return false;
                }
                return true;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                }
                else {
                    tv.setTextColor(getResources().getColor(R.color.colorSecondary));
                }
                return view;
            }
        };

        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ProceedScreen();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void ProceedScreen() {
        int chosenModeIndex = screenSpinner.getSelectedItemPosition();
        int chosenProtocolIndex = protocolSpinner.getSelectedItemPosition();

        if(chosenProtocolIndex == 0 || chosenModeIndex == 0) {
            Toast.makeText
                    (getApplicationContext(), "Please choose both Screen and Protocol mode", Toast.LENGTH_SHORT)
                    .show();
        }
        else {

            Intent newScreen;

            if(screenSpinner.getSelectedItem().toString().equals("Server")) {
                newScreen = new Intent(SplashActivity.this, ServerActivity.class);
            }
            else {
                newScreen = new Intent(SplashActivity.this, ClientActivity.class);
            }

            newScreen.putExtra("Protocol", protocolSpinner.getSelectedItem().toString());
            startActivity(newScreen);
        }
    }
}
