package com.ws1617.iosl.pubcrawl20.CrawlSearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PersonDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PubDetailsActivity;
import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by gaspe on 8. 11. 2016.
 */

public class CrawlSearchFragment extends Fragment implements OnMapReadyCallback{
    public static final String TITLE = "Home";
    private static final String TAG = "CrawlSearchFragment";
    private View rootView;
    private GoogleMap map;

    public CrawlSearchFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.homeMapView, mapFragment).commit();
        rootView = inflater.inflate(R.layout.fragment_crawl_search, container, false);

        mapFragment.getMapAsync(this);

        setRetainInstance(true);

        ((Button) rootView.findViewById(R.id.btn1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                intent.putExtra("name", "Test Event");
                intent.putExtra("id", (long) 14);
                startActivity(intent);
            }
        });

        ((Button) rootView.findViewById(R.id.btn2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PubDetailsActivity.class);
                intent.putExtra("name", "Test Pub");
                intent.putExtra("id", (long) 9);
                startActivity(intent);
            }
        });

        ((Button) rootView.findViewById(R.id.btn3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PersonDetailsActivity.class);
                intent.putExtra("name", "Test Person");
                intent.putExtra("id", (long) 1);
                startActivity(intent);
            }
        });


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
