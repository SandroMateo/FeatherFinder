package com.epicodus.featherfinder.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.epicodus.featherfinder.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LogBirdActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    @Bind(R.id.birdOrderEditText) EditText mBirdOrderEditText;
    @Bind(R.id.birdFamilyEditText) EditText mBirdFamilyEditText;
    @Bind(R.id.birdGenusEditText) EditText mBirdGenusEditText;
    @Bind(R.id.birdSpeciesEditText) EditText mBirdSpeciesEditText;
    @Bind(R.id.birdSightImageView) ImageView mBirdSightImageView;
    @Bind(R.id.birdLocationTextView) TextView mBirdLocationTextView;
    @Bind(R.id.birdCallTextView) TextView mBirdCallTextView;
    @Bind(R.id.birdLogButton) Button mBirdLogButton;

    private Bitmap mImage;
    private String mLocationProvider;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastKnownLocation;
    private Location mLocation;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_bird);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mImage = intent.getParcelableExtra("image");
        mBirdSightImageView.setImageBitmap(mImage);
        mBirdLogButton.setOnClickListener(this);

        ref = FirebaseDatabase.getInstance().getReference();

        mLocationProvider = LocationManager.NETWORK_PROVIDER;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mLocation = location;
                Log.v("location", mLocation.toString());
                if(isBetterLocation(mLocation, mLastKnownLocation)) {
                    mBirdLocationTextView.setText("Lat: " + mLocation.getLatitude() + ", Long: " + mLocation.getLongitude());
                } else {
                    mBirdLocationTextView.setText("Lat: " + mLastKnownLocation.getLatitude() + ", Long: " + mLastKnownLocation.getLongitude());
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras){}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if(ContextCompat.checkSelfPermission(LogBirdActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            mLastKnownLocation = mLocationManager.getLastKnownLocation(mLocationProvider);

        } else {
            ActivityCompat.requestPermissions(LogBirdActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0 );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case 0: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)  {
                    if(ContextCompat.checkSelfPermission(LogBirdActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                        mLastKnownLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
                    }
                } else {

                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v == mBirdLogButton) {

        }
    }

    private boolean isBetterLocation(Location location, Location lastKnownLocation) {
        if(lastKnownLocation == null) {
            return true;
        }

        long timeDiff = location.getTime() - lastKnownLocation.getTime();
        boolean isSignificantlyNewer = timeDiff < TWO_MINUTES;
        boolean isSignificantlyOlder = timeDiff > TWO_MINUTES;
        boolean isNew = timeDiff > 0;

        if(isSignificantlyNewer) {
            return true;
        } else if(isSignificantlyOlder){
            return false;
        }

        float accuracyDiff = (location.getAccuracy() - lastKnownLocation.getAccuracy());
        boolean isMoreAccurate = accuracyDiff > 0;
        boolean isLessAccurate = accuracyDiff < 0;
        boolean isSignificantlyLessAccurate = accuracyDiff > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(), lastKnownLocation.getProvider());

        if(isMoreAccurate) {
            return true;
        } else if(isNew && !isLessAccurate) {
            return true;
        } else if(isNew && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;

    }

    private boolean isSameProvider(String provider1, String provider2) {
        if(provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
