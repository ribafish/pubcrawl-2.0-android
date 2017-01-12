package com.ws1617.iosl.pubcrawl20.Details;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.HashMap;

/**
 * Created by Gasper Kojek on 11. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class MapDialogFragment extends DialogFragment implements OnMapReadyCallback{
    private static final String TAG = "MapDialogFragment";
    private HashMap<Marker, Long> markerIds;


    public MapDialogFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, getTheme());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.popup_event_map, container, false);

        ImageView dismissBtn = (ImageView) v.findViewById(R.id.event_details_popup_dismiss);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.event_details_popup_map, mapFragment).commit();
        mapFragment.getMapAsync(this);

        return v;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        // googleMap.setPadding(left, top, right, bottom)
//        googleMap.setPadding(80, 160, 80, 80);
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                markerIds = ((EventDetailsActivity) getActivity()).drawOnMap(googleMap, 0.5f);
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d(TAG, "onInfoWindowClick");
                try {
                    long id = markerIds.get(marker);
                    Intent intent = new Intent(getContext(), PubDetailsActivity.class);
                    intent.putExtra("name", marker.getTitle());
                    intent.putExtra("id", id);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int padding = (int) (2 * 26 * getResources().getDisplayMetrics().density + 0.5f);

        Window window = getDialog().getWindow();
        window.setLayout(displayMetrics.widthPixels - padding/2, displayMetrics.heightPixels - padding);
        window.setGravity(Gravity.CENTER);
        if(Build.VERSION.SDK_INT >= 22){
            window.setElevation(10.0f);
        }
    }
}

