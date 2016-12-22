package com.epicodus.featherfinder.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.epicodus.featherfinder.Constants;
import com.epicodus.featherfinder.R;
import com.epicodus.featherfinder.models.Sighting;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcel;
import org.parceler.Parcels;

public class FeatherMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private DatabaseReference mSightingReference;
    private String mLocationProvider;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastKnownLocation;
    private Location mLocation;
    private LatLng mCurrentLocation;
    private boolean mSetToCurrentLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feather_map);
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

                if(mMap != null && mSetToCurrentLocation) {
                    mMap.addMarker(new MarkerOptions().position(mCurrentLocation).title("You").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).flat(true));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, Constants.MAPS_ZOOM_LEVEL));
                    if(ContextCompat.checkSelfPermission(FeatherMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.removeUpdates(mLocationListener);
                    }
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
                    Marker birdMarker = mMap.addMarker(new MarkerOptions().position(location).title(species));
                    birdMarker.setTag(sighting);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if(marker.getTitle().equals("You")) {
            Toast.makeText(FeatherMapActivity.this, "You found yourself!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Sighting sighting = (Sighting) marker.getTag();
            Intent intent = new Intent(FeatherMapActivity.this, SightingDetailActivity.class);
            Parcelable parcel = Parcels.wrap(sighting);
            Log.v("sighting", "sighting: " + parcel);
            intent.putExtra("sighting", parcel);
            startActivity(intent);
        }
        return false;
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
        boolean isSignificantlyNewer = timeDiff < Constants.TWO_MINUTES;
        boolean isSignificantlyOlder = timeDiff > Constants.TWO_MINUTES;
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

    @Override
    protected void onStop() {
        super.onStop();
        if(mLocationListener != null && ContextCompat.checkSelfPermission(FeatherMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

}
