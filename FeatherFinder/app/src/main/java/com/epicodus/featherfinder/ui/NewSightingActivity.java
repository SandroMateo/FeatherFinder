package com.epicodus.featherfinder.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epicodus.featherfinder.Constants;
import com.epicodus.featherfinder.R;
import com.epicodus.featherfinder.models.Sighting;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewSightingActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.birdOrderEditText) EditText mBirdOrderEditText;
    @Bind(R.id.birdFamilyEditText) EditText mBirdFamilyEditText;
    @Bind(R.id.birdGenusEditText) EditText mBirdGenusEditText;
    @Bind(R.id.birdSpeciesEditText) EditText mBirdSpeciesEditText;
    @Bind(R.id.birdDescriptionEditText) EditText mBirdDescriptionEditText;
    @Bind(R.id.birdDetailsEditText) EditText mBirdDetailsEditText;
    @Bind(R.id.birdSightImageView) ImageView mBirdSightImageView;
    @Bind(R.id.dropPinButton) Button mDropPinButton;
    @Bind(R.id.uploadImageButton) Button mUploadImageButton;
    @Bind(R.id.takePhotoButton) ImageButton mTakePhotoButton;
    @Bind(R.id.getLocationButton) ImageButton mGetLocationButton;
    @Bind(R.id.birdLogButton) Button mBirdLogButton;

    private Bitmap mImage;
    private String mLocationProvider;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastKnownLocation;
    private Location mLocation;
    private Location mCurrentLocation;
    private DatabaseReference ref;
    private int mCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sighting);
        ButterKnife.bind(this);

        mCounter = 0;
        mDropPinButton.setOnClickListener(this);
        mUploadImageButton.setOnClickListener(this);
        mTakePhotoButton.setOnClickListener(this);
        mGetLocationButton.setOnClickListener(this);
        mBirdLogButton.setOnClickListener(this);

        ref = FirebaseDatabase.getInstance().getReference();

        mLocationProvider = LocationManager.NETWORK_PROVIDER;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

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
            String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Calendar.getInstance().getTime());
            String description = mBirdDescriptionEditText.getText().toString();
            String details = mBirdDetailsEditText.getText().toString();
            boolean imageIsValid = imageIsValid(mImage);
            boolean speciesIsValid = isValid(species, mBirdSpeciesEditText);
            boolean descriptionIsValid = isValid(description, mBirdDescriptionEditText);
            if(speciesIsValid && descriptionIsValid && imageIsValid) {
                String image = encodeBitmap(mImage);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uId = user.getUid();
                Sighting newSighting = new Sighting(species, description, image, latitude, longitude, timestamp);
                saveSightingToDatabase(newSighting);
                saveSightingToUser(uId, newSighting);
                Intent intent = new Intent(NewSightingActivity.this, MainActivity.class);
                if(ContextCompat.checkSelfPermission(NewSightingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.removeUpdates(mLocationListener);
                }
                Toast.makeText(NewSightingActivity.this, "Sighting saved!", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        } else if(v == mTakePhotoButton) {
            onLaunchCamera();
        } else if(v == mGetLocationButton) {
            mLocationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    mCounter++;
                    mLocation = location;
                    Log.v("location", mLocation.toString());
                    if(isBetterLocation(mLocation, mLastKnownLocation)) {
                        mCurrentLocation = mLocation;
                    } else {
                        mCurrentLocation = mLastKnownLocation;
                    }

                    if(mCounter == 1) {
                        Toast.makeText(NewSightingActivity.this, "Location Recorded!", Toast.LENGTH_SHORT).show();
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras){}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            if(ContextCompat.checkSelfPermission(NewSightingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                mLastKnownLocation = mLocationManager.getLastKnownLocation(mLocationProvider);

            } else {
                ActivityCompat.requestPermissions(NewSightingActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0 );
            }
        } else if(v == mUploadImageButton) {
            if (Environment.getExternalStorageState().equals("mounted")) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image: "), Constants.PICK_IMAGE_FROM_LIBRARY);

            }
        } else if(v == mDropPinButton) {

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case 0: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)  {
                    if(ContextCompat.checkSelfPermission(NewSightingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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

    private boolean isValid(String text, EditText editText) {
        if(text.equals("")) {
            editText.setError("Please fill out this field.");
            return false;
        }
        return true;
    }

    private boolean imageIsValid(Bitmap image) {
        if(image != null) {
            return true;
        } else {
            Toast.makeText(NewSightingActivity.this, "Please upload an image or take a picture.", Toast.LENGTH_LONG).show();
            return false;
        }
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

    public void onLaunchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, Constants.REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mBirdSightImageView.setImageBitmap(imageBitmap);
            mImage = imageBitmap;
        } else if(requestCode == Constants.PICK_IMAGE_FROM_LIBRARY && resultCode == RESULT_OK) {
            Uri pickedImage = data.getData();

            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImage);
                mBirdSightImageView.setImageBitmap(imageBitmap);
                mImage = imageBitmap;
            } catch(IOException e) {
                e.printStackTrace();
            }

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
            Intent intent = new Intent(NewSightingActivity.this, FeatherMapActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(NewSightingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
