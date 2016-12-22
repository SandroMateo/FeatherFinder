package com.epicodus.featherfinder.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.epicodus.featherfinder.R;
import com.epicodus.featherfinder.models.Sighting;
import com.google.firebase.auth.FirebaseAuth;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SightingDetailActivity extends AppCompatActivity {
    @Bind(R.id.sightingImageView) ImageView mSightingImageView;
    @Bind(R.id.sightingCallTextView) TextView mSightingCallTextView;
    @Bind(R.id.sightingLocationTextView) TextView mSightingLocationTextView;
    @Bind(R.id.sightingSpeciesTextView) TextView mSightingSpeciesTextView;
    @Bind(R.id.sightingTimestampTextView) TextView mSightingTimestampTextView;
    @Bind(R.id.sightingDescriptionTextView) TextView mSightingDescriptionTextView;
    @Bind(R.id.sightingDetailsTextView) TextView mSightingDetailsTextView;

    private Sighting mSighting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sighting_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mSighting = Parcels.unwrap(intent.getParcelableExtra("sighting"));

        mSightingLocationTextView.setText("Lat: " + mSighting.getLatitude() +  ", Long: " + mSighting.getLongitude());
        mSightingSpeciesTextView.setText(mSighting.getSpecies());
        mSightingTimestampTextView.setText(mSighting.getTimestamp());
        mSightingDescriptionTextView.setText(mSighting.getDescription());
        mSightingDetailsTextView.setText(mSighting.getDetails());
        try {
            Bitmap image = decodeFromBase64(mSighting.getImage());
            mSightingImageView.setImageBitmap(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout) {
            logout();
            return true;
        } else if(id == R.id.action_find) {
            Intent intent = new Intent(SightingDetailActivity.this, FeatherMapActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_sighting) {
            Intent intent = new Intent(SightingDetailActivity.this, NewSightingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SightingDetailActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private Bitmap decodeFromBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }
}
