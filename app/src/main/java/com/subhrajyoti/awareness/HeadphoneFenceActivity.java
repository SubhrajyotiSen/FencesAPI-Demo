package com.subhrajyoti.awareness;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

public class HeadphoneFenceActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    private static final String FENCE_RECEIVER_ACTION = "FENCE_RECEIVE";
    private static final String FENCE_KEY = "headphoneKey";
    private static final String TAG = HeadphoneFenceActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private TextView headphoneTextView;
    private BroadcastReceiver mHeadPhoneFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        headphoneTextView.setText("Headphones connected.");
                        //Intent musicIntent = new Intent("android.intent.action.MUSIC_PLAYER");
                        //startActivity(musicIntent);
                        break;
                    case FenceState.FALSE:
                        headphoneTextView.setText("Headphones disconnected.");
                        break;
                    case FenceState.UNKNOWN:
                        headphoneTextView.setText("Headphone state unknown");
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_headphone_fence);

        headphoneTextView = (TextView) findViewById(R.id.fenceTextView);

        googleApiClient = new GoogleApiClient.Builder(HeadphoneFenceActivity.this)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .build();
        googleApiClient.connect();

        findViewById(R.id.register_fence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerHeadphoneFence();
            }
        });

        findViewById(R.id.unregister_fence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterHeadPhoneFence();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mHeadPhoneFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mHeadPhoneFenceReceiver);
        unregisterHeadPhoneFence();
    }

    private void registerHeadphoneFence() {
        AwarenessFence headphonePlugFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);

        PendingIntent fencePendingIntent = PendingIntent.getBroadcast(this,
                10001,
                new Intent(FENCE_RECEIVER_ACTION),
                0);

        Awareness.FenceApi.updateFences(googleApiClient, new FenceUpdateRequest.Builder()
                .addFence(FENCE_KEY, headphonePlugFence, fencePendingIntent).build())
                .setResultCallback(new ResultCallbacks<Status>() {
                    @Override
                    public void onSuccess(@NonNull final Status status) {
                        Toast.makeText(HeadphoneFenceActivity.this,
                                "Fence registered successfully",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull final Status status) {
                        Toast.makeText(HeadphoneFenceActivity.this,
                                "Cannot register headphone fence.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void unregisterHeadPhoneFence() {
        Awareness.FenceApi.updateFences(
                googleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(FENCE_KEY)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Toast.makeText(HeadphoneFenceActivity.this,
                        "Fence unregistered successfully.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Toast.makeText(HeadphoneFenceActivity.this,
                        "Cannot unregister headphone fence.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnected(@Nullable final Bundle bundle) {
        Log.d(TAG, "Google API connected");

    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.d(TAG, "Google API connection suspended");

    }
}