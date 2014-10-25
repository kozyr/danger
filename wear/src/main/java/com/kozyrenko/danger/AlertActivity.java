package com.kozyrenko.danger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by dev on 10/25/14.
 */
public class AlertActivity extends Activity {

    private static final String TAG = "AlertActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        TextView helpView = (TextView) findViewById(R.id.helpView);
        helpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                help();
            }
        });
        TextView cancelView = (TextView) findViewById(R.id.cancelView);
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    private void cancel() {
        Log.i(TAG, "Cancel clicked");

        finish();
    }

    private void help() {
        Log.i(TAG, "Help clicked");

        Intent findNear = new Intent(this, SafeHouseLocator.class);
        startService(findNear);

        finish();
    }
}
