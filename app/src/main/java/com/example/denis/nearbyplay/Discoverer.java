package com.example.denis.nearbyplay;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateInterpolator;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by denis on 8/2/18.
 */

import android.os.Build;

public class Discoverer {

    private String service;
    private Activity activity;
    private EventListener eventListener;

    public static int EVENT_LOG = 1;

    public Discoverer(String service, Activity activity, EventListener eventListener) {
        this.service = service;
        this.activity = activity;
        this.eventListener = eventListener;
    }

    private void tryConnect(final String endpointId) {
        Nearby.getConnectionsClient(activity).requestConnection(
                Build.MODEL,
                endpointId,
                mConnectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We successfully requested a connection. Now both sides
                                // must accept before the connection is established.
                                Discoverer.this.eventListener.trigger(EVENT_LOG, "Connection requested with: " + endpointId);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Nearby Connections failed to request the connection.
                                Discoverer.this.eventListener.trigger(EVENT_LOG, "Fail to request connection: " + e.getMessage());
                            }
                        });

    }

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(
                        String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    Discoverer.this.eventListener.trigger(EVENT_LOG, "Connection initiated " + endpointId);
                    Nearby.getConnectionsClient(activity).acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            Discoverer.this.eventListener.trigger(EVENT_LOG, "Connected " + endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            Discoverer.this.eventListener.trigger(EVENT_LOG, "Connection rejected " + endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            Discoverer.this.eventListener.trigger(EVENT_LOG, "Connection error with " + endpointId);
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    Discoverer.this.eventListener.trigger(EVENT_LOG, "Disconnected " + endpointId);
                }
            };

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {

        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Discoverer.this.eventListener.trigger(
                            EVENT_LOG, "Found endpoint " + endpointId + ", " + discoveredEndpointInfo.getEndpointName()
                    );
                    tryConnect(endpointId);
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                    Discoverer.this.eventListener.trigger(
                            EVENT_LOG, "Lost endpoint " + endpointId
                    );
                }
            };

    public void startDiscovery() {
        Nearby.getConnectionsClient(activity).startDiscovery(
                service,
                mEndpointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_STAR))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We're discovering!
                                Discoverer.this.eventListener.trigger(
                                        EVENT_LOG, "Start discovering"
                                );
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start discovering.
                                Discoverer.this.eventListener.trigger(
                                        EVENT_LOG, "Fail to run discovery: " + e.getMessage()
                                );
                            }
                        });
    }
}
