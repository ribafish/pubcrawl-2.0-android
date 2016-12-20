package com.ws1617.iosl.pubcrawl20.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.*;


/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PubCrawl20.db";


    public static byte[] bitmapToBytes (Bitmap bmp) {
        if (bmp == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Bitmap bytesToBitmap (byte[] image) {
        if (image == null) return null;
        else if (image.length == 0) return null;
        else return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static void resetEventsDatabase(Context context) {
        EventDbHelper db = new EventDbHelper(context);
        db.onUpgrade(db.getWritableDatabase(), 0, 0);   // resets database

        downloadEvents(context);
    }

    public static void downloadEvents(final Context context) {
        final EventDbHelper db = new EventDbHelper(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        if (prefs.getString("server_ip", null) == null) {
            Log.e(TAG, "server_ip == null");
            return;
        }

        final String url = "http://" + prefs.getString("server_ip", null) + "/events";

        JsonObjectRequest eventsRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "got JSON object:\n" + response.toString());
                        try {
                            ArrayList<Event> events = parseJSONResponseEvents(response);
                            for (final Event event : events) {
                                db.addEvent(event);

                                String participantsListURL = url + "/" + event.getEventId() + "/" + EVENT_PARTICIPANTS;
                                JsonObjectRequest participantRequest = new JsonObjectRequest(Request.Method.GET, participantsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> participantsIds = new ArrayList<>();
                                                try {
                                                    JSONArray participantsJson = response.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
                                                    for (int i=0; i < participantsJson.length(); i++) {
                                                        JSONObject jsonParticipant = participantsJson.getJSONObject(i);
                                                        participantsIds.add(parsePersonJson(jsonParticipant).getId());
                                                    }
                                                    Event e = new Event(event.getEventId());
                                                    e.setParticipantIds(participantsIds);
                                                    db.addParticipants(e);
                                                } catch (Exception e) {
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

                                String pubsListURL = url + "/" + event.getEventId() + "/" + EVENT_PUBS;
                                JsonObjectRequest pubsRequest = new JsonObjectRequest(Request.Method.GET, pubsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> pubs = new ArrayList<>();
                                                try {
                                                    JSONArray pubsJson = response.getJSONObject("_embedded").getJSONArray("pubs");
                                                    for (int i=0; i < pubsJson.length(); i++) {
                                                        JSONObject jsonPub = pubsJson.getJSONObject(i);
                                                        Pub p = parsePubJson(jsonPub);
                                                        pubs.add(p.getId());
                                                    }
                                                    Event e = new Event(event.getEventId());
                                                    e.setPubIds(pubs);
                                                    db.addPubs(e);
                                                } catch (Exception e) {
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

                                String ownerUrl = url + "/" + event.getEventId() + "/" + EVENT_OWNER;
                                JsonObjectRequest ownerRequest = new JsonObjectRequest(Request.Method.GET, ownerUrl, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    Person owner = parsePersonJson(response);
                                                    db.updateEventOwner(event.getEventId(), owner.getId());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "ownerRequest: " + error.getLocalizedMessage());
                                            }
                                        });

                                requestQueue.add(participantRequest);
                                requestQueue.add(pubsRequest);
                                requestQueue.add(ownerRequest);
                            }
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

}
