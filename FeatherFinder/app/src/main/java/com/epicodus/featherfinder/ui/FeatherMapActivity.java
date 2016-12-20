package com.epicodus.featherfinder.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.epicodus.featherfinder.Constants;
import com.epicodus.featherfinder.Manifest;
import com.epicodus.featherfinder.R;
import com.epicodus.featherfinder.models.Sighting;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FeatherMapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private GoogleMap mMap;
    private DatabaseReference mSightingReference;
    private FirebaseRecyclerAdapter mFirebaseAdapter;
    private String mLocationProvider;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastKnownLocation;
    private Location mLocation;
    private LatLng mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feather_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSightingReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_ALL);

        mLocationProvider = LocationManager.NETWORK_PROVIDER;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mLocation = location;
                if(isBetterLocation(mLocation, mLastKnownLocation)) {
                    mCurrentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                } else {
                    mCurrentLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                }

                if(mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras){}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if(ContextCompat.checkSelfPermission(FeatherMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            mLastKnownLocation = mLocationManager.getLastKnownLocation(mLocationProvider);

        } else {
            ActivityCompat.requestPermissions(FeatherMapActivity.this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION }, 0 );
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mSightingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Sighting sighting = snapshot.getValue(Sighting.class);
                    Double latitude = Double.parseDouble(sighting.getLatitude());
                    Double longitude = Double.parseDouble(sighting.getLongitude());
                    String species = sighting.getSpecies();
                    LatLng location = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(location).title(species));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case 0: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)  {
                    if(ContextCompat.checkSelfPermission(FeatherMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                        mLastKnownLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
                    }
                } else {

                }
                break;
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
}
