package com.kozyrenko.danger.service;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class HelpService extends WearableListenerService {

    private KidLocator kidLocator;
    private GoogleApiClient googleApiClient;

    private static final String TAG = "HelpService";

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
        kidLocator = new KidLocator(this);
    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.i(TAG, "onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.i(TAG, "onPeerDisconnected: " + peer);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i(TAG, "Received message!");
        String path = messageEvent.getPath();
        if ("/helpMe".equals(path)) {
            processArrivalRequest(messageEvent.getSourceNodeId(), new String(messageEvent.getData()));
        }

        super.onMessageReceived(messageEvent);

    }

    private void processArrivalRequest(String sourceNodeId, String requestJson) {
        Gson gson = new Gson();

        Log.i(TAG, "Request arrived " + requestJson);
        kidLocator.getSafeHouse()
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<SafeHouse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Could not get houses", e);
                    }

                    @Override
                    public void onNext(SafeHouse house) {
                        Log.i(TAG, "SafeHouse: " + house);
                        sendHouseToWear(house);
                    }
                });
    }

    private void sendHouseToWear(SafeHouse house) {
        /*
        if(!googleApiClient.isConnected()) {
            ConnectionResult connectionResult = googleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "DataLayerListenerService failed to connect to GoogleApiClient.");
                return;
            }
        }
        PutDataMapRequest dataMap = PutDataMapRequest.create(DataLayer.ARRIVAL_PATH);

        Gson gson = new Gson();
        dataMap.getDataMap().putString(DataLayer.ARRIVAL_INFO, gson.toJson(arrival));

        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(googleApiClient, request);
                */

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + house.getLatitude() + "," + house.getLongitude()+"&mode=w"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        if(!googleApiClient.isConnected()) {
            ConnectionResult connectionResult = googleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "DataLayerListenerService failed to connect to GoogleApiClient.");
                return;
            }
        }
        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : events) {
            DataItem item = event.getDataItem();
        }
    }

    @Override
    public void onDestroy() {
        googleApiClient.disconnect();
        super.onDestroy();
    }
}