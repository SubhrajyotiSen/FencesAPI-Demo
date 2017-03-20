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
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

public class ActivityFenceActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    private static final String FENCE_RECEIVER_ACTION = "FENCE_RECEIVE";
    private static final String FENCE_WALKING_KEY = "walkingKey";
    private static final String FENCE_RUNNING_KEY = "runningKey";
    private static final String TAG = ActivityFenceActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private TextView activityTextView;
    private BroadcastReceiver activityFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Toast.makeText(context, "Recieved", Toast.LENGTH_SHORT).show();
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), FENCE_WALKING_KEY)) {
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        activityTextView.setText("User is walking");
                        break;
                    case FenceState.FALSE:
                        activityTextView.setText("User is not walking");
                        break;
                    case FenceState.UNKNOWN:
                        activityTextView.setText("Activity state unknown");
                        break;
                }
            } else if (TextUtils.equals(fenceState.getFenceKey(), FENCE_RUNNING_KEY)) {
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        activityTextView.setText("User is running");
                        break;
                    case FenceState.FALSE:
                        activityTextView.setText("User is not running");
                        break;
                    case FenceState.UNKNOWN:
                        activityTextView.setText("Activity state unknown");
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_fence);

        activityTextView = (TextView) findViewById(R.id.activityTextView);

        googleApiClient = new GoogleApiClient.Builder(ActivityFenceActivity.this)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .build();
        googleApiClient.connect();

        findViewById(R.id.register_fence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerActivityFence();
            }
        });

        findViewById(R.id.unregister_fence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterActivityFence();
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

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(activityFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(activityFenceReceiver);
        unregisterActivityFence();
    }

    private void registerActivityFence() {
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);
        AwarenessFence runningFence = DetectedActivityFence.during(DetectedActivityFence.RUNNING);

        PendingIntent fencePendingIntent = PendingIntent.getBroadcast(this,
                0,
                new Intent(FENCE_RECEIVER_ACTION),
                0);

        Awareness.FenceApi.updateFences(googleApiClient, new FenceUpdateRequest.Builder()
                .addFence(FENCE_WALKING_KEY, walkingFence, fencePendingIntent).build())
                .setResultCallback(new ResultCallbacks<Status>() {
                    @Override
                    public void onSuccess(@NonNull final Status status) {
                        Toast.makeText(ActivityFenceActivity.this,
                                "Fence registered successfully",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull final Status status) {
                        Toast.makeText(ActivityFenceActivity.this,
                                "Cannot register activity fence.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        Awareness.FenceApi.updateFences(googleApiClient, new FenceUpdateRequest.Builder()
                .addFence(FENCE_RUNNING_KEY, runningFence, fencePendingIntent).build());

    }

    private void unregisterActivityFence() {
        Awareness.FenceApi.updateFences(
                googleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(FENCE_WALKING_KEY)
                        .removeFence(FENCE_RUNNING_KEY)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Toast.makeText(ActivityFenceActivity.this,
                        "Fence unregistered successfully.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Toast.makeText(ActivityFenceActivity.this,
                        "Cannot unregister headphone fence.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
