package edu.umass.cs.sensors.mhldemo.View;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;

import edu.umass.cs.sensors.mhldemo.Check.PermissionChecker;
import edu.umass.cs.sensors.mhldemo.R;

import edu.umass.cs.sensors.mhllibrary.Configuration.NetworkConfig;
import edu.umass.cs.sensors.mhllibrary.MHLClient.MHLClient;
import edu.umass.cs.sensors.mhllibrary.Configuration.IdentityConfig;
import edu.umass.cs.sensors.mhllibrary.Configuration.StudyConfig;
import edu.umass.cs.sensors.mhllibrary.MHLClient.MHLHandshake;


public class DemoActivity extends AppCompatActivity {

    private static String TAG = "DemoActivity";

    MHLClient client;
    DemoActivity instance;
    boolean streamState;
    private TextView textStatus;
    PermissionChecker check = new PermissionChecker(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        streamState = false;
        instance    = this;
        textStatus  = (TextView) findViewById(R.id.textStatus);

        //get configuration data
        EditText researchTokenEditText = (EditText) findViewById(R.id.researchTokenEditText);
        researchTokenEditText.setText(IdentityConfig.getResearchToken(this));

        EditText serverEditText = (EditText) findViewById(R.id.serverEditText);
        serverEditText.setText(NetworkConfig.getServerIP(this));

        EditText portEditText = (EditText) findViewById(R.id.portEditText);
        portEditText.setText(String.valueOf(NetworkConfig.getSecurePort(this)));

        //Check permissions if needed
        if(!checkAllPermissions()){
            checkBackground();
            checkCoarseLocationPermission();
            checkFineLocationPermission();
            checkBackgroundLocationPermission();
            checkBackground();
            checkBluetooth();
        }
    }

    public boolean checkCoarseLocationPermission() {
        boolean passed = true;
        // location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            passed = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    getResources().getInteger(R.integer.COARSE_LOCATION));
        } else {
            textStatus.setText("Coarse location permission already granted");
        }

        return passed;
    }

    public boolean checkFineLocationPermission() {
        boolean passed = true;
        // location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            passed = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    getResources().getInteger(R.integer.FINE_LOCATION));
        } else {
            textStatus.setText("Fine location permission already granted");
        }

        return passed;
    }

    public boolean checkBackgroundLocationPermission() {
        boolean passed = true;
        // location
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                passed = false;

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        getResources().getInteger(R.integer.BACKGROUND_LOCATION));
            } else {
                textStatus.setText("Background location permission already granted");
            }
        }
        return passed;
    }

    public boolean checkBackground() {
        boolean passed = true;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = this.getPackageName();
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) { // if you want to disable doze mode for this package
                Log.d(TAG, "already ignoring battery optimizations");
                textStatus.setText("Run in background permission already granted");
            }

            else { // if you want to enable doze mode
                Log.d(TAG, "need to ignore battery optimizations...");
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                this.startActivity(intent);
            }
        }

        return passed;
    }

    public boolean checkBluetooth() {
        boolean passed = true;
        // location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            passed = false;

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    getResources().getInteger(R.integer.BLUETOOTH_ADMIN));
        } else {
            textStatus.setText("Bluetooth permission already granted");
        }

        return passed;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        Log.d(TAG, "Received permission results: " + permissions + ", " + grantResults);

        final int FINE_LOCATION = getResources().getInteger(R.integer.FINE_LOCATION);
        final int COARSE_LOCATION = getResources().getInteger(R.integer.COARSE_LOCATION);
        final int BACKGROUND_LOCATION = getResources().getInteger(R.integer.BACKGROUND_LOCATION);
        final int BACKGROUND_PROCESSES = getResources().getInteger(R.integer.BACKGROUND_PROCESSES);
        final int BLUETOOTH_ADMIN = getResources().getInteger(R.integer.BLUETOOTH_ADMIN);

        if (FINE_LOCATION == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                textStatus.setText("Fine location permission granted");
            }
        } else if (COARSE_LOCATION == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                textStatus.setText("Coarse location permission granted");
            }
        } else if (BACKGROUND_LOCATION == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Location permission granted; proceed with background
                textStatus.setText("Background location permission granted");
            }
        } else if (BACKGROUND_PROCESSES == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Location permission granted; proceed with background
                textStatus.setText("Background processes permission granted");
            }
        } else if (BLUETOOTH_ADMIN == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Location permission granted; proceed with background
                textStatus.setText("Bluetooth admin permission granted");
            }
        } else {
            // some other permission granted -- not explicitly handled

        }

    }

    public void startStreaming(View view){

        if(!checkAllPermissions()){
            textStatus.setText("Some permissions are not granted.");
        }

        //Get research token
        EditText researchTokenEditText = (EditText) findViewById(R.id.researchTokenEditText);
        String research_token = researchTokenEditText.getText().toString();

        if(research_token.equals("")){
            textStatus.setText("Please enter a research token to continue.");
            return;
        }

        //Check research token against stored value
        if(!research_token.equals(IdentityConfig.getResearchToken(this.instance))){
            StudyConfig.setConfigVersion(this.instance,0);
            IdentityConfig.setBadgeID(this.instance,"");
        }
        IdentityConfig.setResearchToken(this.instance,research_token);

        //Get server address
        EditText serverEditText = (EditText) findViewById(R.id.serverEditText);
        String server = serverEditText.getText().toString();

        if(server.equals("")){
            textStatus.setText("Please enter an MHL server address to continue.");
            return;
        }

        //Get port string
        EditText portEditText = (EditText) findViewById(R.id.portEditText);
        String port_string = portEditText.getText().toString();

        if(port_string.equals("")){
            textStatus.setText("Please enter an MHL server port to continue.");
            return;
        }

        int port ;
        try{
            port = Integer.parseInt(port_string);
        } catch(NumberFormatException ex){
            textStatus.setText("Please enter a numerical value for the server port.");
            return;
        }

        //Store network config
        NetworkConfig.set_from_values(this, server, port,port);

        client = new MHLClient(this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "Connecting to client");
                if(!client.connect()){
                    textStatus.setText("Could not connect to server " + server +":" + port_string);
                }
                streamState=true;
                while(streamState==true) {

                    if(!client.isConnected()){
                        textStatus.setText("Connection error. Restarting...");
                        client.connect();
                    }
                    else {
                        textStatus.setText("Streaming...");
                        JSONObject obj = client.generateSyntheticDataMessage();
                        client.addMessage(obj.toString());
                    }

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
                client.disconnect();
            }
        }).start();

    }

    public void stopStreaming(View view){
        streamState=false;
        textStatus.setText("Stopped streaming.");
    }

    public boolean checkAllPermissions() {

        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissions = info.requestedPermissions;
            for (String p : permissions) {

                Log.w(TAG, "Checking for: " + p);

                //FULL_SCREEN_INTENT required, but granted automatically
                if(p.equals("android.permission.FULL_SCREEN_INTENT")) continue;

                //No background permissions on Android 28 and below
                if(p.equals("android.permission.ACCESS_BACKGROUND_LOCATION") && Build.VERSION.SDK_INT <= 28) continue;

                if (!check.hasPermission(p)) {
                    Log.w(TAG, "Failed: " + p);
                    return false;
                }
            }

            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }
}
