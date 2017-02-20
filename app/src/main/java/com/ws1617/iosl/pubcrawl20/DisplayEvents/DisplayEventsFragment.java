package com.ws1617.iosl.pubcrawl20.DisplayEvents;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.RequestQueueHelper;
import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PersonDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PubDetailsActivity;
import com.ws1617.iosl.pubcrawl20.MainActivity;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.resetWholeDatabase;

/**
 * Created by gaspe on 8. 11. 2016.
 */

public class DisplayEventsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerClickListener {
    public static final String TITLE = "Home";
    private static final String TAG = "DisplayEventsFragment";
    private View rootView;
    private GoogleMap map;
    private SharedPreferences prefs;
    private ArrayList<EventMini> eventList = new ArrayList<>();
    private EventAdapter eventAdapter;
    private BroadcastReceiver receiver;
    private RecyclerView eventsRecyclerView;

    public DisplayEventsFragment() {
    }


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

        RelativeLayout hide = (RelativeLayout) rootView.findViewById(R.id.display_events_map_expand);
        hide.setClickable(true);
        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View map = rootView.findViewById(R.id.homeMapView);
                ImageView more = (ImageView) rootView.findViewById(R.id.display_events_map_expand_more);
                ImageView less = (ImageView) rootView.findViewById(R.id.display_events_map_expand_less);
                if (map.getVisibility() == View.GONE) {
                    map.setVisibility(View.VISIBLE);
                    more.setVisibility(View.GONE);
                    less.setVisibility(View.VISIBLE);
                } else {
                    map.setVisibility(View.GONE);
                    more.setVisibility(View.VISIBLE);
                    less.setVisibility(View.GONE);
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
                SharedPreferences sharedPref = view.getContext().getSharedPreferences(getString(R.string.preference_user), Context.MODE_PRIVATE);
                long id = sharedPref.getLong(getString(R.string.user_id), -1);
                Intent intent = new Intent(getContext(), PersonDetailsActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        eventsRecyclerView = (RecyclerView) rootView.findViewById(R.id.eventsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        eventsRecyclerView.setLayoutManager(mLayoutManager);
        eventsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        eventAdapter = new EventAdapter(eventList, getContext());
        eventsRecyclerView.addItemDecoration(new RecyclerViewDivider(getContext(), LinearLayoutManager.VERTICAL));
        eventsRecyclerView.setAdapter(eventAdapter);
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
        eventAdapter.setMap(map);

        LatLng startpos = new LatLng(52.525387, 13.38595);
//        map.addMarker(new MarkerOptions().position(tub).title("TUB - TEL"));

        map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(startpos).zoom(13).build()));
        map.setOnPolylineClickListener(this);
        map.setOnMarkerClickListener(this);
        activateMyLocation();
        for (EventMini e : eventList) {
            drawEventOnMap(e.getEventId(), e.getPubs());
        }
    }

    private void activateMyLocation() {
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            try
            {
                LatLng latLng = new LatLng(MainActivity.getLocation().getLatitude(), MainActivity.getLocation().getLongitude());
                map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(latLng).zoom(13).build()));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.d(TAG, "onPolylineClick" + polyline);
        int i = 0;

        for (EventMini event : eventList) {

            Polyline p = event.getPolyline();
            if (p == null) {
                event.setSelected(false, getContext());
                continue;
            }
            if (event.getPolyline().equals(polyline)) {
                Log.d(TAG, "Clicked event: " + event);
                event.setSelected(true, getContext());
                i = eventList.indexOf(event);
            } else {
                event.setSelected(false, getContext());
            }
        }
        eventAdapter.notifyDataSetChanged();
        eventsRecyclerView.smoothScrollToPosition(i);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        try {
            getEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }
        resetWholeDatabase(getContext());
    }

    private void getEvents () {
        if (map != null){
            map.clear();
        }
        eventList.clear();

        final Context context = getActivity();
        EventDbHelper eventDbHelper = new EventDbHelper();
        PubDbHelper pubDbHelper = new PubDbHelper();
        try {
            ArrayList<Event> el = eventDbHelper.getAllEvents();
            for (Event e : el) {
                ArrayList<TimeSlot> timeSlots = e.getTimeSlotList();
                if (e.getDate().after(new Date(System.currentTimeMillis()-24*60*60*1000)) ||
                        TimeSlot.getCombinedTimeSlot(timeSlots).isDateIncluded(new Date(System.currentTimeMillis()))) {
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
            }
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
                    .color(ContextCompat.getColor(getContext(), R.color.polyline_gray)));
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
