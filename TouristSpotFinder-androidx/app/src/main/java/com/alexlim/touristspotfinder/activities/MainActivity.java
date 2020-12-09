package com.alexlim.touristspotfinder.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.alexlim.touristspotfinder.R;
import com.alexlim.touristspotfinder.fragments.SignInFragment;
import com.alexlim.touristspotfinder.fragments.ListLocationFragment;
import com.alexlim.touristspotfinder.fragments.AddLocationFragment;
import com.alexlim.touristspotfinder.util.FireAuthUiManager;
import com.alexlim.touristspotfinder.util.FirestoreDict;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int CONTAINER_ID = R.id.activity_main_fragment_container;
    private static final int PERMISSIONS_REQUEST = 1;

    private FirebaseUser user;

    private Fragment fragment;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;

    private LocationManager mLocationManager;
    private LatLng mCurrLatLng;

    private static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainActivity.setMainActivity(this);

        // Floating Action Button
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        // Drawer Layout
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        checkIfLoggedIn();

        fragment = null;

        if (savedInstanceState == null) {
            user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                try {
                    fragment = ListLocationFragment.newInstance("", "");
                    View headerView = navigationView.getHeaderView(0);
                    TextView tvName = headerView.findViewById(R.id.tv_name);
                    TextView tvEmail = headerView.findViewById(R.id.tv_email);
                    tvName.setText(user.getDisplayName());
                    tvEmail.setText(user.getEmail());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    fragment = SignInFragment.newInstance("", "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(CONTAINER_ID, fragment).commit();
        }

        // Check if GPS is enabled
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this,
                    "Please enable location services",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
    }

    private static void setMainActivity(MainActivity mainActivity) {
        MainActivity.mainActivity = mainActivity;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_listspots) {
            fragment = getSupportFragmentManager().findFragmentById(CONTAINER_ID);
            if (fragment != null) {
                fragment = ListLocationFragment.newInstance("", "");
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fui_slide_in_right, 0,
                                R.anim.fui_slide_in_right, 0)
                        .replace(CONTAINER_ID, fragment)
                        .commit();
                getSupportActionBar().setTitle("Tourist Spot Finder");
            }
        } else if (id == R.id.nav_addspot) {
            fragment = getSupportFragmentManager().findFragmentById(CONTAINER_ID);
            if (fragment != null) {
                Bundle mBundle = new Bundle();

                mBundle.putDouble(FirestoreDict.COLL_LATITUDE, mCurrLatLng.latitude);
                mBundle.putDouble(FirestoreDict.COLL_LONGITUDE, mCurrLatLng.longitude);

                fragment = AddLocationFragment.newInstance("", "");
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fui_slide_in_right, 0,
                                R.anim.fui_slide_in_right, 0)
                        .replace(CONTAINER_ID, fragment)
                        .commit();
                getSupportActionBar().setTitle("Add New Location");
                fragment.setArguments(mBundle);
            }
        } else if (id == R.id.nav_signout) {
            Toast.makeText(this, "You have signed out", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();

            fragment = getSupportFragmentManager().findFragmentById(CONTAINER_ID);
            if (fragment != null) {
                fragment = SignInFragment.newInstance("", "");
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fui_slide_in_right, 0,
                                R.anim.fui_slide_in_right, 0)
                        .replace(CONTAINER_ID, fragment)
                        .commit();
                checkIfLoggedIn();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;   // return false for unchecked state; return true (default) for checked state
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FireAuthUiManager.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                checkIfLoggedIn();
                user = FirebaseAuth.getInstance().getCurrentUser();
                fragment = getSupportFragmentManager().findFragmentById(CONTAINER_ID);

                if (fragment != null) {
                    if (user != null) {
                        fragment = ListLocationFragment.newInstance("", "");
                        View headerView = navigationView.getHeaderView(0);
                        TextView tvName = headerView.findViewById(R.id.tv_name);
                        TextView tvEmail = headerView.findViewById(R.id.tv_email);
                        tvName.setText(user.getDisplayName());
                        tvEmail.setText(user.getEmail());
                    }
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.fui_slide_in_right, 0,
                                    R.anim.fui_slide_in_right, 0)
                            .replace(CONTAINER_ID, fragment)
                            .commit();
                }

            }
        }
    }

    public void checkIfLoggedIn() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            unlockDrawer(true);
        } else {
            unlockDrawer(false);
        }
    }

    public void unlockDrawer(boolean unlock) {
        if (unlock) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        mCurrLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
            }, null);
        }
    }
}
