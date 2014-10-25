package com.kozyrenko.danger.service;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;

import java.util.concurrent.TimeUnit;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by dev on 10/25/14.
 */
public class KidLocator {
    private Context context;
    private Subscription locator;

    private static final int INITIAL_LOCATION_TIMEOUT = 5000;
    private static final int LOCATION_UPDATE_INTERVAL = 1000;
    private static final int SUFFICIENT_INITIAL_ACCURACY = 200;

    private static final String TAG = KidLocator.class.getSimpleName();

    public KidLocator(Context context) {
        this.context = context;
    }


    private Observable<Location> getMyLocation() {
        LocationRequest req = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setExpirationDuration(INITIAL_LOCATION_TIMEOUT)
                .setInterval(LOCATION_UPDATE_INTERVAL);

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
        Observable<Location> goodEnoughQuicklyOrNothingObservable = locationProvider.getUpdatedLocation(req)
                .filter(new Func1<Location, Boolean>() {
                    @Override
                    public Boolean call(Location location) {
                        return location.getAccuracy() < SUFFICIENT_INITIAL_ACCURACY;
                    }
                })
                .timeout(INITIAL_LOCATION_TIMEOUT,
                        TimeUnit.MILLISECONDS,
                        locationProvider.getLastKnownLocation(),
                        Schedulers.io())
                .first()
                .observeOn(Schedulers.io());

        return goodEnoughQuicklyOrNothingObservable;
    }

    public Observable<SafeHouse> getSafeHouse() {
        return getMyLocation().flatMap(new Func1<Location, Observable<SafeHouse>>() {
            @Override
            public Observable<SafeHouse> call(Location location) {
                return SafeHouseClient.getSafeHouse(location.getLatitude(), location.getLongitude());
            }
        });
    }
}
