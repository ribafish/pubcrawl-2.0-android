package com.ws1617.iosl.pubcrawl20.Home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by gaspe on 8. 11. 2016.
 */

public class HomeFragment extends Fragment implements OnMapReadyCallback{
    public static final String TITLE = "Home";
    private static final String TAG = "HomeFragment";
    private View rootView;
    private GoogleMap map;

    public HomeFragment () {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.homeMapView, mapFragment).commit();
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng tub = new LatLng(52.512626, 13.322238);
        map.addMarker(new MarkerOptions().position(tub).title("TUB - TEL"));
        map.moveCamera(CameraUpdateFactory.newLatLng(tub));
    }
}
