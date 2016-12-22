package com.epicodus.featherfinder.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.epicodus.featherfinder.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SightingLocationActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    @Bind(R.id.pinLocationButton) Button mPinLocationButton;

    private GoogleMap mMap;
    private LatLng mSightingLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sighting_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.pinSightingMap);
        mapFragment.getMapAsync(this);
        ButterKnife.bind(this);

        mPinLocationButton.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point).title("Sighting location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                mSightingLocation = point;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == mPinLocationButton) {
            boolean sightingLocationIsValid = isValid(mSightingLocation);
            if(sightingLocationIsValid) {
                String latitude = Double.toString(mSightingLocation.latitude);
                String longitude = Double.toString(mSightingLocation.longitude);
                Intent intent = new Intent(SightingLocationActivity.this, NewSightingActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        }
    }

    private boolean isValid(LatLng location) {
        if(location != null) {
            return true;
        } else {
            Toast.makeText(SightingLocationActivity.this, "Please pin a location.", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
