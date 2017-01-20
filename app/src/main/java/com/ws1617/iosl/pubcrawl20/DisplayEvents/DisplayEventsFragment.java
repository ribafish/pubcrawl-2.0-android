package com.ws1617.iosl.pubcrawl20.DisplayEvents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.RequestQueueHelper;
import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PersonDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PubDetailsActivity;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.Collections;

import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.resetWholeDatabase;

/**
 * Created by gaspe on 8. 11. 2016.
 */

public class DisplayEventsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerClickListener{
    public static final String TITLE = "Home";
    private static final String TAG = "DisplayEventsFragment";
    private View rootView;
    private GoogleMap map;
    private SharedPreferences prefs;
    private ArrayList<EventMini> eventList = new ArrayList<>();
    private EventAdapter eventAdapter;
    private BroadcastReceiver receiver;

    public DisplayEventsFragment() {}



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.homeMapView, mapFragment).commit();
        rootView = inflater.inflate(R.layout.fragment_display_events, container, false);

        mapFragment.getMapAsync(this);

        setRetainInstance(true);

        Button refresh = (Button) rootView.findViewById(R.id.btn_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetWholeDatabase(getContext());
                getEvents();
            }
        });

        Button hide = (Button) rootView.findViewById(R.id.btn_hide);
        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View map = rootView.findViewById(R.id.homeMapView);
                if (map.getVisibility() == View.GONE) {
                    map.setVisibility(View.VISIBLE);
                } else {
                    map.setVisibility(View.GONE);
                }
            }
        });

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

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.eventsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        eventAdapter = new EventAdapter(eventList);
        recyclerView.addItemDecoration(new RecyclerViewDivider(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(eventAdapter);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(RequestQueueHelper.BROADCAST_INTENT)) {
                    Log.d(TAG, "Got database refreshed broadcast");
                    getEvents();
                }
            }
        };
        IntentFilter filter = new IntentFilter(RequestQueueHelper.BROADCAST_INTENT);
        getContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng startpos = new LatLng(52.525387,13.38595);
//        map.addMarker(new MarkerOptions().position(tub).title("TUB - TEL"));
        map.moveCamera(CameraUpdateFactory.newCameraPosition( new CameraPosition.Builder().target(startpos).zoom(13).build()));
        map.setOnPolylineClickListener(this);
        map.setOnMarkerClickListener(this);


        for (EventMini e : eventList) {
            drawEventOnMap(e.getEventId(), e.getPubs());
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.d(TAG, "onPolylineClick" + polyline);

        for (EventMini event : eventList) {

            Polyline p = event.getPolyline();
            if (p == null) {
                event.setSelected(false);
                continue;
            }
            if (event.getPolyline().equals(polyline)) {
                Log.d(TAG, "Clicked event: " + event);
                event.setSelected(true);
            } else {
                event.setSelected(false);
            }
        }
        eventAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        getEvents();
        resetWholeDatabase(getContext());
    }

    private void getEvents () {
        if (map != null){
            map.clear();
        }
        eventList.clear();

        final Context context = getActivity();
        EventDbHelper eventDbHelper = new EventDbHelper(context);
        PubDbHelper pubDbHelper = new PubDbHelper(context);
        try {
            ArrayList<Event> el = eventDbHelper.getAllEvents();
            for (Event e : el) {
                EventMini eventMini = new EventMini(e);
                for (long pubId : e.getPubIds()) {
                    try {
                        Pub p = pubDbHelper.getPub(pubId);
                        eventMini.addPub(new PubMini(p));
                    } catch (Exception exx) {
                        exx.printStackTrace();
                    }
                }
                eventList.add(eventMini);
            }
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
        if (eventAdapter != null) {
            Log.d(TAG, "eventAdapter.notifyDataSetChanged()");
            Collections.sort(eventList, new EventMiniComparator(true, EventMiniComparator.NAME));
            eventAdapter.notifyDataSetChanged();
        }
        if (map != null) {
            Log.d(TAG, "drawing events on map...");
            for (EventMini e : eventList) {
                drawEventOnMap(e.getEventId(), e.getPubs());
            }
        }

    }

    /**
     * drawEventOnMap
     * @param eventId
     * @param pubs
     */
    private void drawEventOnMap(final long eventId, ArrayList<PubMini> pubs) {
        if (map != null && pubs != null) {
            ArrayList<LatLng> latLngs = new ArrayList<>();
            for (PubMini pub : pubs) {
                latLngs.add(pub.getLatLng());
            }

            Polyline polyline = map.addPolyline(new PolylineOptions()
                    .addAll(latLngs)
                    .width(10)
                    .clickable(true)
                    .color(Color.GRAY));
            for (EventMini e : eventList) {
                if (e.getEventId() == eventId) {
                    eventList.get(eventList.indexOf(e)).setPolyline(polyline);
                }
            }
        }
        eventAdapter.notifyDataSetChanged();
    }

    /**
     * getMarkerIconFromDrawable
     * @param resId int resource ID
     * @param color int Color
     * @param size int size
     * @return BitmapDescriptor to be used for Marker.setIcon
     */
    @NonNull
    private BitmapDescriptor getMarkerIconFromDrawable(int resId, int color, int size) {
        Drawable drawable = ContextCompat.getDrawable(getActivity(), resId);
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, size, size);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public int getEventListIndex (long eventId) throws IndexOutOfBoundsException {
        for (EventMini e : eventList) {
            if (e.getEventId() == eventId) {
                return eventList.indexOf(e);
            }
        }
        throw new IndexOutOfBoundsException("Can't find eventId " + eventId);
    }
}
