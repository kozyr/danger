package com.kozyrenko.danger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.kozyrenko.danger.service.KidLocator;
import com.kozyrenko.danger.service.SafeHouse;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


public class Danger extends Activity {

    private TextView house;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger);

        house = (TextView) findViewById(R.id.house);
    }


    @Override
    protected void onStart() {
        // Intent intent = new Intent(this, CTAService.class);
        // startService(intent);

        KidLocator locator = new KidLocator(this);
        locator.getSafeHouse()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SafeHouse>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("DangerApp", "Could not get arrivals", e);
                    }

                    @Override
                    public void onNext(SafeHouse arrival) {
                        showHouse(arrival);
                    }
                });

        super.onStart();
    }

    private void showHouse(SafeHouse arrival) {
        Log.i("DangerApp", "Arrival: " + arrival);

        house.setText(arrival.getLatitude() + "," + arrival.getLongitude());

        /*
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + arrival.getLatitude() + "," + arrival.getLongitude() + "&mode=w"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        */
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.danger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
