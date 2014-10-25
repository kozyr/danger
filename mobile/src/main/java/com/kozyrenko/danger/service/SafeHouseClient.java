package com.kozyrenko.danger.service;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.SimpleXMLConverter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;


public class SafeHouseClient {
    private static final String SAFE_URL = "http://childsafezone.herokuapp.com";

    private static final String TAG = "SafeHouseClient";

    private interface SafeHouseService {
        @GET("/alert")
        SafeHouse getHouse(@Query("lat") double lat, @Query("lon") double lon);
    }

    private static final RestAdapter REST_ADAPTER = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setLog(new AndroidLog(TAG))
            .setEndpoint(SAFE_URL)
            .build();
    private static final SafeHouseService SAFE_HOUSE_SERVICE = REST_ADAPTER.create(SafeHouseService.class);

    public static Observable<SafeHouse> getSafeHouse(final double lat, final double lon) {
        return Observable.create(new Observable.OnSubscribe<SafeHouse>() {
            @Override
            public void call(Subscriber<? super SafeHouse> subscriber) {
                try {
                    subscriber.onNext(SAFE_HOUSE_SERVICE.getHouse(lat, lon));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}
