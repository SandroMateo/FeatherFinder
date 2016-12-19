package com.epicodus.featherfinder.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import com.epicodus.featherfinder.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LogBirdActivity extends AppCompatActivity {
    @Bind(R.id.birdOrderEditText) EditText mBirdOrderEditText;
    @Bind(R.id.birdFamilyEditText) EditText mBirdFamilyEditText;
    @Bind(R.id.birdGenusEditText) EditText mBirdGenusEditText;
    @Bind(R.id.birdSpeciesEditText) EditText mBirdSpeciesEditText;
    @Bind(R.id.birdSightImageView) ImageView mBirdSightImageView;

    private Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_bird);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mImage = intent.getParcelableExtra("image");
        mBirdSightImageView.setImageBitmap(mImage);
    }
}
