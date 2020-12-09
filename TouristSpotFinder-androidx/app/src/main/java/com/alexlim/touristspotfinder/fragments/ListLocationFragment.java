package com.alexlim.touristspotfinder.fragments;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexlim.touristspotfinder.activities.LocationDetailActivity;
import com.alexlim.touristspotfinder.activities.MainActivity;
import com.alexlim.touristspotfinder.adapter.LocationAdapter;
import com.alexlim.touristspotfinder.model.LocationItem;
import com.alexlim.touristspotfinder.R;
import com.alexlim.touristspotfinder.util.FirestoreDict;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * @link ListLocationFragment.OnFragmentInteractionListener interface
 * to handle interaction events.
 * Use the {@link ListLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListLocationFragment extends Fragment {
    private static final String TAG = MainActivity.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private OnFragmentInteractionListener mListener;

    private FirebaseFirestore db;

    private ArrayList<LocationItem> locationList;

    private View mView;
    private RecyclerView mRecyclerView;
    private LocationAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ProgressDialog mProgressDialog;
    private String mLocationId;
    private Location mCurrentLoc;

//    @BindView(R.id.textCurrentSearch)
//    TextView mCurrentSearchView;
//
//    @BindView(R.id.textCurrentSortBy)
//    TextView mCurrentSortByView;

    public ListLocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListLocationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListLocationFragment newInstance(String param1, String param2) {
        ListLocationFragment fragment = new ListLocationFragment();
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

        db = FirebaseFirestore.getInstance();

        FusedLocationProviderClient client;
        client = new FusedLocationProviderClient(getContext());
        try {
            client.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    mCurrentLoc = location;
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_list_location, container, false);

        locationList = new ArrayList<>();

        getDataFromFirestore();

        mProgressDialog = new ProgressDialog(mView.getContext());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();

        mRecyclerView = mView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new LocationAdapter(locationList);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter.setOnItemClickListener(position -> {
            Intent mIntent = new Intent(getActivity(), LocationDetailActivity.class);
            mIntent.putExtra(FirestoreDict.COLL_LOCATION_ID,
                    locationList.get(position).getLocationId());
            mIntent.putExtra(FirestoreDict.COLL_LOCATION_NAME,
                    locationList.get(position).getLocationName());
            mIntent.putExtra(FirestoreDict.COLL_LOCATION_ADDRESS,
                    locationList.get(position).getLocationAddress());
            mIntent.putExtra(FirestoreDict.COLL_LOCATION_DESC,
                    locationList.get(position).getLocationDesc());
            mIntent.putExtra(FirestoreDict.COLL_CATEGORY,
                    locationList.get(position).getCategory());
            mIntent.putExtra(FirestoreDict.COLL_LATITUDE,
                    locationList.get(position).getLatitude());
            mIntent.putExtra(FirestoreDict.COLL_LONGITUDE,
                    locationList.get(position).getLongitude());
            mIntent.putExtra(FirestoreDict.COLL_EXIST,
                    locationList.get(position).getLocationExist());
            startActivity(mIntent);
        });

        return mView;
    }

    private void getDataFromFirestore() {
        Location mNewLoc = new Location("");
        db.collection(FirestoreDict.COLL_LOCATIONS)
                .get()
                .addOnCompleteListener(task -> {
                    for (DocumentSnapshot querySnapshot: task.getResult()) {
                        final double mLocLat = querySnapshot.getDouble(FirestoreDict.COLL_LATITUDE);
                        final double mLocLng = querySnapshot.getDouble(FirestoreDict.COLL_LONGITUDE);
                        mNewLoc.setLatitude(mLocLat);
                        mNewLoc.setLongitude(mLocLng);
                        final float mDist = mCurrentLoc.distanceTo(mNewLoc);

                        LocationItem locationItem = new LocationItem(querySnapshot.getId(),
                                querySnapshot.getString(FirestoreDict.COLL_LOCATION_NAME),
                                querySnapshot.getString(FirestoreDict.COLL_LOCATION_ADDRESS),
                                querySnapshot.getString(FirestoreDict.COLL_LOCATION_DESC),
                                querySnapshot.getString(FirestoreDict.COLL_CATEGORY),
                                mLocLat,
                                mLocLng,
                                querySnapshot.getBoolean(FirestoreDict.COLL_EXIST),
                                mDist);

                        locationList.add(locationItem);
                    }

                    Collections.sort(locationList, new Comparator<LocationItem>() {
                        @Override
                        public int compare(LocationItem o1, LocationItem o2) {
                            int result = Float.compare(o1.getDistance(), o2.getDistance());
                            return result;
                        }
                    });
                    mRecyclerView.setAdapter(mAdapter);
                    mProgressDialog.dismiss();
                });
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
}
