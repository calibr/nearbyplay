package com.example.denis.nearbyplay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.Manifest;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import android.os.Build;

import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView mLog;
    private MessageListener mMessageListener;
    private String TAG = "APP";
    private Message mMessage;
    private String SERVICE_ID = "com.example.denis.nearbyplay";
    private Advertizer advertizer;
    private Discoverer discoverer;
    private OperationCompleteListener permissionsListener;
    private EventListener eventListener;

    public MainActivity() {
        eventListener = new EventListener() {
            @Override
            public void onEvent(int code, String data) {
                super.onEvent(code, data);
                mLog.append(data + "\n");
            }
        };
    }

    public void onStartDiscover(View view) {
        permissionsListener = new OperationCompleteListener() {
            @Override
            public void onComplete() {
                discoverer = new Discoverer(SERVICE_ID, MainActivity.this, MainActivity.this.eventListener);
                discoverer.startDiscovery();
                super.onComplete();
            }
        };
        PermissionsRequestor pr = new PermissionsRequestor(this, permissionsListener);
        pr.checkLocationPermission();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    public void onClickAdvertize(View view) {
        advertizer = new Advertizer(SERVICE_ID, this, eventListener);
        advertizer.startAdvertising();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        mLog = (TextView) findViewById(R.id.editText);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionsRequestor.MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        this.permissionsListener.onComplete();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    this.permissionsListener.onFailed();
                }
                return;
            }

        }
    }

}
