package com.epicodus.featherfinder.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.epicodus.featherfinder.R;
import com.epicodus.featherfinder.models.Sighting;

import org.parceler.Parcels;

public class SightingDetailActivity extends AppCompatActivity {
    private Sighting mSighting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sighting_detail);

        Intent intent = getIntent();
        mSighting = Parcels.unwrap(intent.getParcelableExtra("sighting"));
    }
}
