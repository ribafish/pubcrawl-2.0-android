package com.ws1617.iosl.pubcrawl20.CrawlSearch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ws1617.iosl.pubcrawl20.Event.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by gaspe on 8. 11. 2016.
 */

public class CrawlSearchFragment extends Fragment implements OnMapReadyCallback{
    public static final String TITLE = "Home";
    private static final String TAG = "CrawlSearchFragment";
    private View rootView;
    private GoogleMap map;
    private SharedPreferences prefs;
    private RequestQueue requestQueue;
    private HashMap<Long, Event> eventsMap = new HashMap<>();

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

        Button button = (Button) rootView.findViewById(R.id.btn1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                startActivity(intent);
            }
        });

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

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng tub = new LatLng(52.512626, 13.322238);
        map.addMarker(new MarkerOptions().position(tub).title("TUB - TEL"));
        map.moveCamera(CameraUpdateFactory.newLatLng(tub));
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
                        Log.e(TAG, "eventsRequest: " + error.getLocalizedMessage());
                    }
                });
        // Add the request to the RequestQueue.
        requestQueue.add(eventsRequest);
    }

    private void parseJSONResponseEvents (JSONObject response) throws JSONException, ParseException  {
        JSONObject embedded = response.getJSONObject("_embedded");
        JSONArray jsonEvents = embedded.getJSONArray("events");
        for (int i=0; i < jsonEvents.length(); i++) {
            JSONObject jsonEvent = jsonEvents.getJSONObject(i);
            Event event = parseJSONEvent(jsonEvent);
            Log.d(TAG, "Parsed event [" + i + "]: " + event);
            eventsMap.put(event.getEventId(), event);
        }
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
        String owenrURL = jsonLinks.getJSONObject("eventOwner").getString("href");
        final long eventId = parseIdFromHref(jsonLinks.getJSONObject("self").getString("href"));
        event.setEventId(eventId);

        JsonObjectRequest participantRequest = new JsonObjectRequest(Request.Method.GET, participantsListURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Person> participants = new ArrayList<>();
                        try {
                            JSONArray participantsJson = response.getJSONObject("_embedded").getJSONArray("crawlers");
                            for (int i=0; i < participantsJson.length(); i++) {
                                JSONObject jsonParticipant = participantsJson.getJSONObject(i);
                                participants.add(parsePersonJson(jsonParticipant));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        eventsMap.get(eventId).setParticipants(participants);
                        Log.d(TAG, "parsed participants, event id: " + eventId + "; Event: " + eventsMap.get(eventId));

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
                        ArrayList<Pub> pubs = new ArrayList<>();
                        try {
                            JSONArray pubsJson = response.getJSONObject("_embedded").getJSONArray("pubs");
                            for (int i=0; i < pubsJson.length(); i++) {
                                JSONObject jsonPub = pubsJson.getJSONObject(i);
                                pubs.add(parsePubJson(jsonPub));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        eventsMap.get(eventId).setPubs(pubs);
                        Log.d(TAG, "parsed pubs, event id: " + eventId + "; Event: " + eventsMap.get(eventId));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "pubsRequest: " + error.getLocalizedMessage());
                    }
                });

        JsonObjectRequest ownerRequest = new JsonObjectRequest(Request.Method.GET, owenrURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            eventsMap.get(eventId).setOwner(parsePersonJson(response));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "ownerRequest:" + error.getLocalizedMessage());
                    }
                });

        requestQueue.add(participantRequest);
        requestQueue.add(pubsRequest);
        requestQueue.add(ownerRequest);

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
}
