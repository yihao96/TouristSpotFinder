package com.alexlim.touristspotfinder.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.alexlim.touristspotfinder.R;
import com.alexlim.touristspotfinder.util.FirestoreDict;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *
 * @link AddLocationFragment.OnFragmentInteractionListener interface
 * to handle interaction events.
 * Use the {@link AddLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddLocationFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = AddLocationFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private OnFragmentInteractionListener mListener;

    private final static int PLACE_PICKER_REQUEST = 1;
    private final static float CAMERA_ZOOM = 18;

    private FirebaseUser user;
    private FirebaseFirestore db;

    private View mView;
    private TextInputLayout mLocationNameInputLayout;
    private TextInputEditText mLocationNameInputText;
    private TextInputLayout mLocationAddressInputLayout;
    private TextInputEditText mLocationAddressInputText;
    private TextInputLayout mLocationDescInputLayout;
    private TextInputEditText mLocationDescInputText;
    private MaterialButton mAddLocationButton;
    private MaterialButton mGetLocationButton;
    private Spinner mCategorySpinner;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private Marker mMarker;
    private ProgressDialog mProgressDialog;

    private LatLng mLatLng;
    private LatLng mCurrentLatLng;
    private String locationName;
    private String locationAddress;
    private String locationDesc;
    private String locationCategory;

    public AddLocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddLocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddLocationFragment newInstance(String param1, String param2) {
        AddLocationFragment fragment = new AddLocationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_add_location, container, false);

        mLocationNameInputLayout = mView.findViewById(R.id.addLocationNameInputLayout);
        mLocationNameInputText = mView.findViewById(R.id.addLocationNameInputText);
        mLocationAddressInputLayout = mView.findViewById(R.id.addLocationAddressInputLayout);
        mLocationAddressInputText = mView.findViewById(R.id.addLocationAddressInputText);
        mLocationDescInputLayout = mView.findViewById(R.id.addLocationDescInputLayout);
        mLocationDescInputText = mView.findViewById(R.id.addLocationDescInputText);

        // Get current location sent by MainActivity.java
        mCurrentLatLng = new LatLng(getArguments().getDouble(FirestoreDict.COLL_LATITUDE),
                getArguments().getDouble(FirestoreDict.COLL_LONGITUDE));

        // Setup Category spinner and populate spinner
        mCategorySpinner = (Spinner) mView.findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mView.getContext(),
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                locationCategory = getResources().getStringArray(R.array.category_array)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Setup Get Location Button and display Place Picker
        mGetLocationButton = mView.findViewById(R.id.getLocationButton);
        mGetLocationButton.setOnClickListener(v -> {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                Intent intent = builder.build(getActivity());
                startActivityForResult(intent, PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        });

        // Setup Add Button
        mAddLocationButton = mView.findViewById(R.id.addLocationAddButton);
        mAddLocationButton.setOnClickListener(v -> {
            mAddLocationButton.setEnabled(false);
            locationName = mLocationNameInputText.getText().toString();
            locationAddress = mLocationAddressInputText.getText().toString();
            locationDesc = mLocationDescInputText.getText().toString();
            addLocation(locationName, locationAddress, locationDesc);
        });

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (MapView) mView.findViewById(R.id.mapViewFragment);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (data != null) {
                Place place = PlacePicker.getPlace(getActivity(), data);
                mLocationNameInputText.setText(place.getName());
                mLocationAddressInputText.setText(place.getAddress());
                mGoogleMap.clear();
                mMarker = mGoogleMap.addMarker(new MarkerOptions().
                        position(place.getLatLng()).title(place.getName().toString()));
                mGoogleMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(place.getLatLng(), CAMERA_ZOOM));
                mLatLng = place.getLatLng();
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
    private void addLocation(final String locationName, final String locationAddress, final String locationDesc) {
        if (locationName.isEmpty() || locationAddress.isEmpty() || locationDesc.isEmpty()) {
            if (locationName.isEmpty() && locationAddress.isEmpty() && locationDesc.isEmpty()) {
                mLocationNameInputLayout.setError("Please enter the location name");
                mLocationAddressInputLayout.setError("Please enter the location address");
                mLocationDescInputLayout.setError("Please provide a short description");
            } else if (locationName.isEmpty() && locationAddress.isEmpty()) {
                mLocationNameInputLayout.setError("Please enter the location name");
                mLocationAddressInputLayout.setError("Please enter the location address");
                mLocationDescInputLayout.setError(null);
            } else if (locationName.isEmpty() && locationDesc.isEmpty()) {
                mLocationNameInputLayout.setError("Please enter the location name");
                mLocationAddressInputLayout.setError(null);
                mLocationDescInputLayout.setError("Please provide a short description");
            } else if (locationAddress.isEmpty() && locationDesc.isEmpty()) {
                mLocationNameInputLayout.setError(null);
                mLocationAddressInputLayout.setError("Please enter the location address");
                mLocationDescInputLayout.setError("Please provide a short description");
            } else if (locationName.isEmpty()) {
                mLocationNameInputLayout.setError("Please enter the location name");
                mLocationAddressInputLayout.setError(null);
                mLocationDescInputLayout.setError(null);
            } else if (locationAddress.isEmpty()) {
                mLocationNameInputLayout.setError(null);
                mLocationAddressInputLayout.setError("Please enter the location address");
                mLocationDescInputLayout.setError(null);
            } else if (locationDesc.isEmpty()) {
                mLocationNameInputLayout.setError(null);
                mLocationAddressInputLayout.setError(null);
                mLocationDescInputLayout.setError("Please provide a short description");
            }
            mAddLocationButton.setEnabled(true);
        } else {
            try {
                final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                        .getSystemService(mView.getContext().INPUT_METHOD_SERVICE);
                if (inputMethodManager.isActive()) {
                    inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Set up and display a ProgressDialog when data being added
            mProgressDialog = new ProgressDialog(mView.getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Adding location...");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();

            // Clear the TextInputLayout error
            mLocationNameInputLayout.setError(null);
            mLocationAddressInputLayout.setError(null);
            mLocationDescInputLayout.setError(null);

            final Map<String, Object> locationDetails = new HashMap<>();
            locationDetails.put(FirestoreDict.COLL_LOCATION_NAME, locationName);
            locationDetails.put(FirestoreDict.COLL_LOCATION_ADDRESS, locationAddress);
            locationDetails.put(FirestoreDict.COLL_LOCATION_DESC, locationDesc);
            locationDetails.put(FirestoreDict.COLL_CATEGORY, locationCategory);
            locationDetails.put(FirestoreDict.COLL_EXIST, FirestoreDict.EXIST_TRUE);

            if (mLatLng != null) {
                locationDetails.put(FirestoreDict.COLL_LATITUDE, mLatLng.latitude);
                locationDetails.put(FirestoreDict.COLL_LONGITUDE, mLatLng.longitude);
            } else {
                locationDetails.put(FirestoreDict.COLL_LATITUDE, mCurrentLatLng.latitude);
                locationDetails.put(FirestoreDict.COLL_LONGITUDE, mCurrentLatLng.longitude);
            }

            db.collection(FirestoreDict.COLL_LOCATIONS)
                    .document()
                    .set(locationDetails)
                    .addOnSuccessListener(aVoid -> {
                        clearInputs();
                        dismissProgressDialog(locationName);
                        mMarker.remove();
                    })
                    .addOnFailureListener(e -> dismissProgressDialog());

            mAddLocationButton.setEnabled(true);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mCurrentLatLng != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, CAMERA_ZOOM));
            try {
                mGoogleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearInputs() {
        mLocationNameInputText.setText("");
        mLocationNameInputText.setSelected(false);
        mLocationNameInputLayout.setSelected(false);
        mLocationAddressInputText.setText("");
        mLocationAddressInputText.setSelected(false);
        mLocationAddressInputLayout.setSelected(false);
        mLocationDescInputText.setText("");
        mLocationDescInputText.setSelected(false);
        mLocationDescInputLayout.setSelected(false);
    }

    public void dismissProgressDialog(String locName) {
        mProgressDialog.dismiss();
        Toast.makeText(mView.getContext(),
                locName + " successfully added!",
                Toast.LENGTH_SHORT).show();
    }

    public void dismissProgressDialog() {
        mProgressDialog.dismiss();
        Toast.makeText(mView.getContext(),
                "Failed to add location",
                Toast.LENGTH_SHORT).show();
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.
                getFusedLocationProviderClient(getActivity());
        int permission = ContextCompat.checkSelfPermission(mView.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.d(TAG, "onLocationResult: location acquired.");
                        mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
            }, null);
        }
    }
}
