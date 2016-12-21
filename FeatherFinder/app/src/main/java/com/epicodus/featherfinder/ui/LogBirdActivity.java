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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epicodus.featherfinder.Constants;
import com.epicodus.featherfinder.R;
import com.epicodus.featherfinder.models.Sighting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LogBirdActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    @Bind(R.id.birdOrderEditText) EditText mBirdOrderEditText;
    @Bind(R.id.birdFamilyEditText) EditText mBirdFamilyEditText;
    @Bind(R.id.birdGenusEditText) EditText mBirdGenusEditText;
    @Bind(R.id.birdSpeciesEditText) EditText mBirdSpeciesEditText;
    @Bind(R.id.birdDescriptionEditText) EditText mBirdDescriptionEditText;
    @Bind(R.id.birdDetailsEditText) EditText mBirdDetailsEditText;
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
    private Location mCurrentLocation;
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
                    mCurrentLocation = mLocation;
                } else {
                    mBirdLocationTextView.setText("Lat: " + mLastKnownLocation.getLatitude() + ", Long: " + mLastKnownLocation.getLongitude());
                    mCurrentLocation = mLastKnownLocation;
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
            String order = mBirdOrderEditText.getText().toString();
            String family = mBirdFamilyEditText.getText().toString();
            String genus = mBirdGenusEditText.getText().toString();
            String species = mBirdSpeciesEditText.getText().toString();
            String latitude = Double.toString(mCurrentLocation.getLatitude());
            String longitude = Double.toString(mCurrentLocation.getLongitude());
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            String description = mBirdDescriptionEditText.getText().toString();
            String details = mBirdDetailsEditText.getText().toString();
            String image = encodeBitmap(mImage);
            boolean speciesIsValid = isValid(species, mBirdDescriptionEditText);
            if(speciesIsValid) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uId = user.getUid();
                Sighting newSighting = new Sighting(species, image, latitude, longitude, timestamp);
                saveSightingToDatabase(newSighting);
                saveSightingToUser(uId, newSighting);
                Intent intent = new Intent(LogBirdActivity.this, MainActivity.class);
                if(ContextCompat.checkSelfPermission(LogBirdActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.removeUpdates(mLocationListener);
                }
                Toast.makeText(LogBirdActivity.this, "Sighting saved!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
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

    private boolean isValid(String text, EditText editText) {
        if(text.equals("")) {
            editText.setError("Please fill out this field.");
            return false;
        }
        return true;
    }

    private String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void saveSightingToUser(String userId, Sighting sighting) {
        DatabaseReference userRef = FirebaseDatabase
                .getInstance()
                .getReference(Constants.FIREBASE_CHILD_USERS)
                .child(userId);
        DatabaseReference pushRef = userRef.push();
        String pushId = pushRef.getKey();
        sighting.setPushId(pushId);
        pushRef.setValue(sighting);
    }

    private void saveSightingToDatabase(Sighting sighting) {
        ref.child(Constants.FIREBASE_CHILD_ALL).push().setValue(sighting);
    }
}
