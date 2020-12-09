package com.alexlim.touristspotfinder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.alexlim.touristspotfinder.R;
import com.alexlim.touristspotfinder.util.FirestoreDict;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = LocationDetailActivity.class.getSimpleName();

    private final static float CAMERA_ZOOM = 18;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private FirebaseFirestore db;

    private TextView mNameTextView;
    private TextView mAddressTextView;
    private TextView mCategoryTextView;
    private TextView mDescTextView;
    private MapView mDetailMapView;
    private GoogleMap mGoogleMap;
    private MaterialButton mReportButton;
    private TextView mReportTextView;
    private ProgressDialog mProgressDialog;

    private LatLng mLatLng;
    private String mId;
    private boolean mIsExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_detail);

        db = FirebaseFirestore.getInstance();

        mNameTextView = findViewById(R.id.locationNameTextView);
        mAddressTextView = findViewById(R.id.locationAddressTextView);
        mCategoryTextView = findViewById(R.id.locationCategoryTextView);
        mDescTextView = findViewById(R.id.locationDescTextView);
        mReportButton = findViewById(R.id.locationExistButton);
        mReportButton.setOnClickListener(v -> reportLocationInvalid());
        mReportTextView = findViewById(R.id.invalidTextView);

        Bundle bunbun = getIntent().getExtras();
        if (bunbun != null) {
            mId = bunbun.getString(FirestoreDict.COLL_LOCATION_ID);
            mNameTextView.setText(bunbun.getString(FirestoreDict.COLL_LOCATION_NAME));
            mAddressTextView.setText(bunbun.getString(FirestoreDict.COLL_LOCATION_ADDRESS));
            mCategoryTextView.setText(bunbun.getString(FirestoreDict.COLL_CATEGORY));
            mDescTextView.setText(bunbun.getString(FirestoreDict.COLL_LOCATION_DESC));
            mLatLng = new LatLng(bunbun.getDouble(FirestoreDict.COLL_LATITUDE),
                    bunbun.getDouble(FirestoreDict.COLL_LONGITUDE));
            mIsExist = bunbun.getBoolean(FirestoreDict.COLL_EXIST);

            if (mIsExist) {
                mReportButton.setEnabled(true);
                mReportTextView.setText(null);
            } else {
                mReportButton.setEnabled(false);
                mReportTextView.setText("Location reported as invalid.");
            }
        }

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mDetailMapView = findViewById(R.id.locationMapViewFragment);
        mDetailMapView.onCreate(mapViewBundle);

        mDetailMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mDetailMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDetailMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDetailMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDetailMapView.onStop();
    }

    @Override
    protected void onPause() {
        mDetailMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mDetailMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mDetailMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mLatLng != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, CAMERA_ZOOM));
            mGoogleMap.addMarker(new MarkerOptions().position(mLatLng));
            try {
                mGoogleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void reportLocationInvalid() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Reporting invalid...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();

        DocumentReference locationRef = db.collection(FirestoreDict.COLL_LOCATIONS).document(mId);

        locationRef.update(FirestoreDict.COLL_EXIST, FirestoreDict.EXIST_FALSE)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Successfully reported", Toast.LENGTH_SHORT)
                            .show();
                    mReportButton.setEnabled(false);
                    mReportTextView.setText("Location reported as invalid.");
                    mProgressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to report", Toast.LENGTH_SHORT)
                            .show();
                    mProgressDialog.dismiss();
                });
    }
}
