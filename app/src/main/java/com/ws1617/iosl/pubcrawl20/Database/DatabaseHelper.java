package com.ws1617.iosl.pubcrawl20.Database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.NewEvent.NewEventActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EMBEDDED;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EVENTS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EVENT_OWNER;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EVENT_PARTICIPANTS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EVENT_PUBS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSONS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSON_EVENTS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSON_FAVOURITE_PUBS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSON_FRIENDS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSON_OWNED_EVENTS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PERSON_OWNED_PUBS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PUBS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PUB_EVENTS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PUB_OWNER;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.PUB_TOP_PERSONS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.parseJSONEvent;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.parseJSONResponseEvents;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.parseJsonResponsePersons;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.parseJsonResponsePubs;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.parsePersonJson;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.parsePubJson;


/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";

    public static byte[] bitmapToBytes(Bitmap bmp) {

        if (bmp == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public static Bitmap bytesToBitmap(byte[] image) {
        if (image == null) return null;
        else if (image.length == 0) return null;
        else return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static void resetWholeDatabase(Context context) {
        resetPubsDatabase(context);
        resetPersonsDatabase(context);
        resetEventsDatabase(context);
    }

    @Deprecated
    public static void resetEventsDatabase(Context context) {
        //EventDbHelper db = new EventDbHelper();
        //TODO
        //db.onUpgrade(db.getWritableDatabase(), 0, 0);   // resets database
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        EventDbHelper.onUpgrade(db, 0, 0);
        downloadEvents(context);
    }

    public static void addEvent(Context context, final Event event,
                                final NewEventActivity.EventCreation eventCreation) {
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;
        try {
            url = getServerUrl(context) + EVENTS;
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }
        JSONObject object = new JSONObject();
        try {
            object.put("eventName", event.getEventName());
            object.put("date", event.getDate().getTime());
            object.put("description", event.getDescription());
            object.put("latmax", event.getMaxLatLng().latitude);
            object.put("lngmax", event.getMaxLatLng().longitude);
            object.put("latmin", event.getMinLatLng().latitude);
            object.put("lngmin", event.getMinLatLng().longitude);
            object.put("tracked", false);
            object.put("eventImage", null);
            object.put("timeslotList", timeSlotToJsonArray(event.getTimeSlotList()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        PubJsonObjectRequest jsonObjectRequest = new PubJsonObjectRequest(Request.Method.POST, url,
                object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                addPubsListToEvent();


                //should be called after successfully adding the pubsList
                // should show spinner until every thing is created successfully
                eventCreation.onSuccess();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // As of f605da3 the following should work
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONObject obj = new JSONObject(res);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        eventCreation.onFail();
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        eventCreation.onFail();
                        e2.printStackTrace();
                    }
                }

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public static void addPubsListToEvent(){

    }


    public static JSONArray timeSlotToJsonArray(List<TimeSlot> timeSlotList) {
        JSONArray timeSlot = new JSONArray();
        for (TimeSlot ts : timeSlotList) {
            JSONObject object = new JSONObject();
            try {
                object.put("startingTime", String.valueOf(ts.getStartTime().getTime()));
                object.put("endingTime",  String.valueOf(ts.getEndTime().getTime()));
                object.put("pubId", ts.getPubId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            timeSlot.put(object);
        }
        return timeSlot;
    }

    public static void downloadEvents(final Context context) {
        final String tag = TAG;
        final String TAG = tag + ".downloadEvents";
        final EventDbHelper db = new EventDbHelper();
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);

        final String url;

        try {
            url = getServerUrl(context) + EVENTS;
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }

        JsonObjectRequest eventsRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "got JSON object:\n" + response.toString());
                        try {
                            ArrayList<Event> events = parseJSONResponseEvents(response);
                            for (final Event event : events) {
                                db.addEvent(event);

                                String participantsListURL = url + "/" + event.getId() + "/" + EVENT_PARTICIPANTS;
                                JsonObjectRequest participantRequest = new JsonObjectRequest(Request.Method.GET, participantsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> participantsIds = new ArrayList<>();
                                                try {
                                                    JSONArray participantsJson = response.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
                                                    for (int i = 0; i < participantsJson.length(); i++) {
                                                        JSONObject jsonParticipant = participantsJson.getJSONObject(i);
                                                        participantsIds.add(parsePersonJson(jsonParticipant).getId());
                                                    }
                                                    Event e = new Event(event.getId());
                                                    e.setParticipantIds(participantsIds);
                                                    db.addParticipants(e);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for event id " + event.getId());
                                                Log.e(TAG, "participantRequest: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                String pubsListURL = url + "/" + event.getId() + "/" + EVENT_PUBS;
                                JsonObjectRequest pubsRequest = new JsonObjectRequest(Request.Method.GET, pubsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> pubs = new ArrayList<>();
                                                try {
                                                    JSONArray pubsJson = response.getJSONObject(EMBEDDED).getJSONArray(PUBS);
                                                    for (int i = 0; i < pubsJson.length(); i++) {
                                                        JSONObject jsonPub = pubsJson.getJSONObject(i);
                                                        Pub p = parsePubJson(jsonPub);
                                                        pubs.add(p.getId());
                                                    }
                                                    Event e = new Event(event.getId());
                                                    e.setPubIds(pubs);
                                                    db.addPubs(e);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for event id " + event.getId());
                                                Log.e(TAG, "pubsRequest: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                String ownerUrl = url + "/" + event.getId() + "/" + EVENT_OWNER;
                                JsonObjectRequest ownerRequest = new JsonObjectRequest(Request.Method.GET, ownerUrl, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    Person owner = parsePersonJson(response);
                                                    db.updateEventOwner(event.getId(), owner.getId());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for event id " + event.getId());
                                                Log.e(TAG, "ownerRequest: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                requestQueue.add(participantRequest);
                                requestQueue.add(pubsRequest);
                                requestQueue.add(ownerRequest);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        requestQueue.gotResponse();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "VolleyError eventsRequest: " + error.toString());
                        error.printStackTrace();
                        Toast.makeText(context, "Can't connect to server.", Toast.LENGTH_SHORT).show();
                        requestQueue.gotResponse();
                    }
                });
        // Add the request to the RequestQueue.
        requestQueue.add(eventsRequest);

    }

    @Deprecated
    public static void resetPubsDatabase(Context context) {
        //PubDbHelper db = new PubDbHelper();
        //TODO
        //db.onUpgrade(db.getWritableDatabase(), 0, 0);   // resets database
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        PubDbHelper.onUpgrade(db, 0, 0);
        downloadPubs(context);
    }

    public static void downloadPubs(final Context context) {
        final String tag = TAG;
        final String TAG = tag + ".downloadPubs";
        final PubDbHelper db = new PubDbHelper();
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;

        try {
            url = getServerUrl(context) + PUBS;
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }

        JsonObjectRequest pubsRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "got JSON object:\n" + response.toString());
                        try {
                            ArrayList<Pub> pubs = parseJsonResponsePubs(response);
                            for (final Pub pub : pubs) {
                                db.addPub(pub);

                                String eventsListURL = url + "/" + pub.getId() + "/" + PUB_EVENTS;
                                JsonObjectRequest pubEventsRequest = new JsonObjectRequest(Request.Method.GET, eventsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> events = new ArrayList<>();
                                                try {
                                                    JSONArray eventsJson = response.getJSONObject(EMBEDDED).getJSONArray(EVENTS);
                                                    for (int i = 0; i < eventsJson.length(); i++) {
                                                        JSONObject jsonEvent = eventsJson.getJSONObject(i);
                                                        events.add(parseJSONEvent(jsonEvent).getId());
                                                    }
                                                    Pub p = new Pub(pub.getId());
                                                    p.setEventsListIds(events);
                                                    db.addPubEvents(p);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error for pub id " + pub.getId());
                                                    Log.e(TAG, "pubEventsRequest error: " + e.getLocalizedMessage());
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for pub id " + pub.getId());
                                                Log.e(TAG, "pubEventsRequest error: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                String topPersonsURL = url + "/" + pub.getId() + "/" + PUB_TOP_PERSONS;
                                JsonObjectRequest topPersonsRequest = new JsonObjectRequest(Request.Method.GET, topPersonsURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> topPersonIds = new ArrayList<>();
                                                try {
                                                    JSONArray participantsJson = response.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
                                                    for (int i = 0; i < participantsJson.length(); i++) {
                                                        JSONObject jsonParticipant = participantsJson.getJSONObject(i);
                                                        topPersonIds.add(parsePersonJson(jsonParticipant).getId());
                                                    }
                                                    Pub p = new Pub(pub.getId());
                                                    p.setTopsListIds(topPersonIds);
                                                    db.addTopPersons(p);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error for pub id " + pub.getId());
                                                    Log.e(TAG, "topPersonsRequest error: " + e.getLocalizedMessage());
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for pub id " + pub.getId());
                                                Log.e(TAG, "topPersonsRequest: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                String ownerUrl = url + "/" + pub.getId() + "/" + PUB_OWNER;
                                JsonObjectRequest ownerRequest = new JsonObjectRequest(Request.Method.GET, ownerUrl, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    Person owner = parsePersonJson(response);
                                                    db.updatePubOwner(pub.getId(), owner.getId());
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error for pub id " + pub.getId());
                                                    Log.e(TAG, "ownerRequest error: " + e.getLocalizedMessage());
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for pub id " + pub.getId());
                                                Log.e(TAG, "ownerRequest: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                requestQueue.add(pubEventsRequest);
                                requestQueue.add(topPersonsRequest);
                                requestQueue.add(ownerRequest);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        requestQueue.gotResponse();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VolleyError pubsRequest: " + error.toString());
                error.printStackTrace();
                Toast.makeText(context, "Can't connect to server.", Toast.LENGTH_SHORT).show();
                requestQueue.gotResponse();
            }
        });
        requestQueue.add(pubsRequest);
    }

    @Deprecated
    public static void resetPersonsDatabase(Context context) {
        //PersonDbHelper db = new PersonDbHelper();
        //TODO
        //db.onUpgrade(db.getWritableDatabase(), 0, 0);   // resets database
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        PersonDbHelper.onUpgrade(db, 0, 0);
        downloadPersons(context);
    }

    public static void downloadPersons(final Context context) {
        final String tag = TAG;
        final String TAG = tag + ".downloadPersons";
        final PersonDbHelper db = new PersonDbHelper();
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;

        try {
            url = getServerUrl(context) + PERSONS;
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }

        JsonObjectRequest personsRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "got JSON object:\n" + response.toString());
                        try {
                            ArrayList<Person> persons = parseJsonResponsePersons(response);
                            for (final Person person : persons) {
                                db.addPerson(person);

                                String ownEventsListURL = url + "/" + person.getId() + "/" + PERSON_OWNED_EVENTS;
                                JsonObjectRequest ownEventsRequest = new JsonObjectRequest(Request.Method.GET, ownEventsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> events = new ArrayList<>();
                                                try {
                                                    JSONArray eventsJson = response.getJSONObject(EMBEDDED).getJSONArray(EVENTS);
                                                    for (int i = 0; i < eventsJson.length(); i++) {
                                                        JSONObject jsonEvent = eventsJson.getJSONObject(i);
                                                        events.add(parseJSONEvent(jsonEvent).getId());
                                                    }
                                                    Person p = new Person(person.getId());
                                                    p.setOwnedEventIds(events);
                                                    db.addOwnedEventIds(p);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error for person id " + person.getId());
                                                    Log.e(TAG, "ownEventsRequest error: " + e.getLocalizedMessage());
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for person id " + person.getId());
                                                Log.e(TAG, "ownEventsRequest error: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                String favouritePubsURL = url + "/" + person.getId() + "/" + PERSON_FAVOURITE_PUBS;
                                JsonObjectRequest favouritePubsRequest = new JsonObjectRequest(Request.Method.GET, favouritePubsURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> pubs = new ArrayList<>();
                                                try {
                                                    JSONArray pubsJson = response.getJSONObject(EMBEDDED).getJSONArray(PUBS);
                                                    for (int i = 0; i < pubsJson.length(); i++) {
                                                        JSONObject jsonPub = pubsJson.getJSONObject(i);
                                                        Pub p = parsePubJson(jsonPub);
                                                        pubs.add(p.getId());
                                                    }
                                                    Person p = new Person(person.getId());
                                                    p.setFavouritePubIds(pubs);
                                                    db.addFavouritePubIds(p);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error for person id " + person.getId());
                                                    Log.e(TAG, "favouritePubsRequest error: " + e.getLocalizedMessage());
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for person id " + person.getId());
                                                Log.e(TAG, "favouritePubsRequest error: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                String ownPubsURL = url + "/" + person.getId() + "/" + PERSON_OWNED_PUBS;
                                JsonObjectRequest ownedPubsRequest = new JsonObjectRequest(Request.Method.GET, ownPubsURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> pubs = new ArrayList<>();
                                                try {
                                                    JSONArray pubsJson = response.getJSONObject(EMBEDDED).getJSONArray(PUBS);
                                                    for (int i = 0; i < pubsJson.length(); i++) {
                                                        JSONObject jsonPub = pubsJson.getJSONObject(i);
                                                        Pub p = parsePubJson(jsonPub);
                                                        pubs.add(p.getId());
                                                    }
                                                    Person p = new Person(person.getId());
                                                    p.setOwnedPubIds(pubs);
                                                    db.addOwnedPubIds(p);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error for person id " + person.getId());
                                                    Log.e(TAG, "ownedPubsRequest error: " + e.getLocalizedMessage());
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for person id " + person.getId());
                                                Log.e(TAG, "ownedPubsRequest error: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });


                                String firendsURL = url + "/" + person.getId() + "/" + PERSON_FRIENDS;
                                JsonObjectRequest friendsRequest = new JsonObjectRequest(Request.Method.GET, firendsURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> topPersonIds = new ArrayList<>();
                                                try {
                                                    JSONArray participantsJson = response.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
                                                    for (int i = 0; i < participantsJson.length(); i++) {
                                                        JSONObject jsonParticipant = participantsJson.getJSONObject(i);
                                                        topPersonIds.add(parsePersonJson(jsonParticipant).getId());
                                                    }
                                                    Person p = new Person(person.getId());
                                                    p.setFriendIds(topPersonIds);
                                                    db.addFriendIds(p);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error for person id " + person.getId());
                                                    Log.e(TAG, "topPersonsRequest error: " + e.getLocalizedMessage());
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for person id " + person.getId());
                                                Log.e(TAG, "topPersonsRequest: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });

                                String eventsListURL = url + "/" + person.getId() + "/" + PERSON_EVENTS;
                                JsonObjectRequest eventsRequest = new JsonObjectRequest(Request.Method.GET, eventsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> events = new ArrayList<>();
                                                try {
                                                    JSONArray eventsJson = response.getJSONObject(EMBEDDED).getJSONArray(EVENTS);
                                                    for (int i = 0; i < eventsJson.length(); i++) {
                                                        JSONObject jsonEvent = eventsJson.getJSONObject(i);
                                                        events.add(parseJSONEvent(jsonEvent).getId());
                                                    }
                                                    Person p = new Person(person.getId());
                                                    p.setEventIds(events);
                                                    db.addEventIds(p);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "Error for person id " + person.getId());
                                                    Log.e(TAG, "eventsRequest error: " + e.getLocalizedMessage());
                                                }
                                                requestQueue.gotResponse();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e(TAG, "VolleyError for person id " + person.getId());
                                                Log.e(TAG, "eventsRequest error: " + error.getLocalizedMessage());
                                                requestQueue.gotResponse();
                                            }
                                        });
                                requestQueue.add(ownEventsRequest);
                                requestQueue.add(favouritePubsRequest);
                                requestQueue.add(ownedPubsRequest);
                                requestQueue.add(friendsRequest);
                                requestQueue.add(eventsRequest);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        requestQueue.gotResponse();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "VolleyError personsRequest: " + error.toString());
                        error.printStackTrace();
                        Toast.makeText(context, "Can't connect to server.", Toast.LENGTH_SHORT).show();
                        requestQueue.gotResponse();
                    }
                });
        requestQueue.add(personsRequest);
    }

    public static String getServerUrl(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String url;

        if (prefs.getString("server_ip", null) == null || prefs.getString("server_ip", "").length() < 10) {
            Log.e(TAG, "Server url not set, setting default");
            url = "https://vm-74243.lal.in2p3.fr:8443/";
        } else {
            url = prefs.getString("server_ip", "").replace(" ", "") + "/";
        }

        if (!url.startsWith("http")) url = "http://" + url;

        return url;
    }

}
