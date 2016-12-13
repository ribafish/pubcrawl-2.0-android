package com.ws1617.iosl.pubcrawl20.DisplayEvents;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PersonDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PubDetailsActivity;
import com.ws1617.iosl.pubcrawl20.DisplayEvents.MiniDataModels.EventMini;
import com.ws1617.iosl.pubcrawl20.DisplayEvents.MiniDataModels.EventMiniComparator;
import com.ws1617.iosl.pubcrawl20.DisplayEvents.MiniDataModels.PubMini;
import com.ws1617.iosl.pubcrawl20.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by gaspe on 8. 11. 2016.
 */

public class DisplayEventsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerClickListener{
    public static final String TITLE = "Home";
    private static final String TAG = "DisplayEventsFragment";
    private View rootView;
    private GoogleMap map;
    private SharedPreferences prefs;
    private RequestQueue requestQueue;
    private ArrayList<EventMini> eventList = new ArrayList<>();
    private EventAdapter eventAdapter;

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
            if (p==null) {
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

        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        getEvents();

    }

    private void getEvents () {
        String url = "http://" + prefs.getString("server_ip", null)+"/events";

        if (url == null) {
            Log.e(TAG, "server_ip == null");
            return;
        }

        if (map != null){
            map.clear();
        }
        eventList.clear();

        final Context context = getActivity();


        JsonObjectRequest eventsRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "got JSON object:\n" + response.toString());
                        try {
                            parseJSONResponseEvents(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "eventsRequest Error: " + error.toString());
                        error.printStackTrace();
                        Toast.makeText(context, "Can't connect to server.", Toast.LENGTH_SHORT).show();
                    }
                });
        // Add the request to the RequestQueue.
        requestQueue.add(eventsRequest);
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

    private void parseJSONResponseEvents (JSONObject response) throws JSONException, ParseException  {
        JSONObject embedded = response.getJSONObject("_embedded");
        JSONArray jsonEvents = embedded.getJSONArray("events");
        for (int i=0; i < jsonEvents.length(); i++) {
            JSONObject jsonEvent = jsonEvents.getJSONObject(i);
            Event e = parseJSONEvent(jsonEvent);
            Log.d(TAG, "Parsed event [" + i + "]: " + e);
            EventMini event = new EventMini(e);
            eventList.add(event);
        }
        Collections.sort(eventList, new EventMiniComparator(true, EventMiniComparator.NAME));
        eventAdapter.notifyDataSetChanged();
    }

    private Event parseJSONEvent (JSONObject jsonEvent) throws JSONException, ParseException {
        final Event event = new Event();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        event.setDate(dateFormat.parse(jsonEvent.getString("date")));
        event.setEventName(jsonEvent.getString("eventName"));
        event.setDescription(jsonEvent.getString("description"));
        event.setTracked(jsonEvent.getBoolean("tracked"));

        JSONObject jsonLinks = jsonEvent.getJSONObject("_links");
        String participantsListURL = jsonLinks.getJSONObject("participantsList").getString("href");
        String pubsListURL = jsonLinks.getJSONObject("pubsList").getString("href");
        final long eventId = parseIdFromHref(jsonLinks.getJSONObject("self").getString("href"));
        event.setEventId(eventId);

        JsonObjectRequest participantRequest = new JsonObjectRequest(Request.Method.GET, participantsListURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Long> participantsIds = new ArrayList<>();
                        try {
                            JSONArray participantsJson = response.getJSONObject("_embedded").getJSONArray("crawlers");
                            for (int i=0; i < participantsJson.length(); i++) {
                                JSONObject jsonParticipant = participantsJson.getJSONObject(i);
                                participantsIds.add(parsePersonJson(jsonParticipant).getId());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            eventList.get(getEventListIndex(eventId)).setParticipantIds(participantsIds);
                            Log.d(TAG, "parsed participants, event id: " + eventId + "; Event: " + eventList.get(getEventListIndex(eventId)));
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "participantRequest: " + error.getLocalizedMessage());
                    }
                });

        JsonObjectRequest pubsRequest = new JsonObjectRequest(Request.Method.GET, pubsListURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<PubMini> pubs = new ArrayList<>();
                        try {
                            JSONArray pubsJson = response.getJSONObject("_embedded").getJSONArray("pubs");
                            for (int i=0; i < pubsJson.length(); i++) {
                                JSONObject jsonPub = pubsJson.getJSONObject(i);
                                Pub p = parsePubJson(jsonPub);
                                PubMini pub = new PubMini(p);
                                pubs.add(pub);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            eventList.get(getEventListIndex(eventId)).setPubs(pubs);

                            drawEventOnMap(eventId, pubs);
                            Log.d(TAG, "parsed pubs, event id: " + eventId + "; Event: " + eventList.get(getEventListIndex(eventId)));
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "pubsRequest: " + error.getLocalizedMessage());
                    }
                });

        requestQueue.add(participantRequest);
        requestQueue.add(pubsRequest);

        return event;
    }

    private Pub parsePubJson (JSONObject jsonPub) throws JSONException {
        String name = jsonPub.getString("pubName");
        int prices = jsonPub.getInt("prices");
        int rating = jsonPub.getInt("rating");
        double lat = jsonPub.getDouble("lat");
        double lng = jsonPub.getDouble("lng");
        String description = jsonPub.getString("description");
        int size = jsonPub.getInt("size");
        long id = parseIdFromHref(jsonPub.getJSONObject("_links").getJSONObject("self").getString("href"));

        return new Pub(id, name, new LatLng(lat, lng), size);
    }

    private Person parsePersonJson (JSONObject jsonPerson) throws JSONException {
        String name = jsonPerson.getString("userName");
        String email = jsonPerson.getString("email");
        String description = jsonPerson.getString("description");
        long id = parseIdFromHref(jsonPerson
                .getJSONObject("_links")
                .getJSONObject("self")
                .getString("href"));
        return new Person(id, name, email, description);
    }

    private long parseIdFromHref(String href) {
        return Long.parseLong(href.split("\\/+")[3]);
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
