package com.example.denis.nearbyplay;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by denis on 8/2/18.
 */

public class Advertizer {
    private String serviceId;
    private Activity activity;
    private EventListener eventListener;

    public static int EVENT_LOG = 1;

    public Advertizer(String serviceId, Activity activity, EventListener eventListener) {
        this.serviceId = serviceId;
        this.activity = activity;
        this.eventListener = eventListener;
    }
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {

        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

        }
    };
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    Advertizer.this.eventListener.trigger(EVENT_LOG, "Connection initiated " + endpointId);
                    Nearby.getConnectionsClient(activity).acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            Advertizer.this.eventListener.trigger(EVENT_LOG, "Connected " + endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            Advertizer.this.eventListener.trigger(EVENT_LOG, "Connection rejected " + endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            Advertizer.this.eventListener.trigger(EVENT_LOG, "Connection error with " + endpointId);
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    Advertizer.this.eventListener.trigger(EVENT_LOG, "Disconnected " + endpointId);
                }
            };

    public void startAdvertising() {
        Nearby.getConnectionsClient(activity).startAdvertising(
                "Advertizer",
                serviceId,
                mConnectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_STAR))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                Advertizer.this.eventListener.trigger(EVENT_LOG, "Start advertizing");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Advertizer.this.eventListener.trigger(EVENT_LOG, "Can't advertize: " + e.getMessage());
                            }
                        });
    }
}
