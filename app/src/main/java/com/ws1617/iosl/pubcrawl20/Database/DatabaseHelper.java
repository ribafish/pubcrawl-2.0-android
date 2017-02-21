package com.ws1617.iosl.pubcrawl20.Database;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ws1617.iosl.pubcrawl20.App;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Details.DetailsCallback;
import com.ws1617.iosl.pubcrawl20.NewEvent.NewEventActivity;
import com.ws1617.iosl.pubcrawl20.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EMBEDDED;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EVENTS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EVENT_OWNER;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EVENT_PARTICIPANTS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.EVENT_PUBS;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.HREF;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.LINKS;
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
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.SELF;
import static com.ws1617.iosl.pubcrawl20.Database.JsonParser.parseIdFromHref;
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

    protected static void resetEventsDatabase(Context context) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        EventDbHelper.onUpgrade(db, 0, 0);
        downloadEvents(context);
    }

    private static JSONObject prepareEventObj(Event event, Context context, Long currentUserID) {
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
            object.put("pubsList", pubLinksToJsonArray(event.getPubIds(), context));
           if(currentUserID != null) object.put("eventOwner", "https://vm-74243.lal.in2p3.fr:8443/crawlers/" + currentUserID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private static JSONArray pubLinksToJsonArray(ArrayList<Long> PubIds, Context context) {
        JSONArray pubsList = new JSONArray();
        for (Long id : PubIds) {
            String link = getServerUrl(context) + PUBS + "/" + id;
            pubsList.put(link);
        }
        return pubsList;
    }

    private static void onLocalErrorResponse(VolleyError error, NewEventActivity.EventCreation eventCreation) {
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

    public static void updateEvent(final Context context, final Event event,
                                final NewEventActivity.EventCreation eventCreation) {
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;
        try {
            url = getServerUrl(context) + EVENTS + "/" +event.getId();
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }

       //owner wont be changed
         JSONObject object = prepareEventObj(event, context, null);

        EmptyJsonObjectRequest jsonObjectRequest = new EmptyJsonObjectRequest(Request.Method.PUT, url,
                object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                eventCreation.onSuccess();
                //patchPubsListToEvent(context,event,eventCreation);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onLocalErrorResponse(error, eventCreation);
            }
        });
        requestQueue.add(jsonObjectRequest);
    }




    public static void deleteEvent(final Context context, final Event event,
                                   final NewEventActivity.EventCreation eventCreation) {
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;
        try {
            url = getServerUrl(context) + EVENTS +"/"+ event.getId();
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }
        EmptyJsonObjectRequest jsonObjectRequest = new EmptyJsonObjectRequest(Request.Method.DELETE, url,
                 null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                eventCreation.onSuccess();
                //patchPubsListToEvent(context,event,eventCreation);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onLocalErrorResponse(error, eventCreation);
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public static void addEvent(final Context context, final Event event,
                                final NewEventActivity.EventCreation eventCreation) {
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;
        try {
            url = getServerUrl(context) + EVENTS;
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_user), Context.MODE_PRIVATE);
        long currentUserID = sharedPref.getLong(context.getString(R.string.user_id), -1);
        if (currentUserID == -1) {
            eventCreation.onFail();
            return;
        }
        JSONObject object = prepareEventObj(event, context, currentUserID);

        EmptyJsonObjectRequest jsonObjectRequest = new EmptyJsonObjectRequest(Request.Method.POST, url,
                object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                eventCreation.onSuccess();
                //patchPubsListToEvent(context,event,eventCreation);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onLocalErrorResponse(error, eventCreation);
            }
        });
        requestQueue.add(jsonObjectRequest);
    }



    public static JSONArray timeSlotToJsonArray(List<TimeSlot> timeSlotList) {
        JSONArray timeSlot = new JSONArray();
        for (TimeSlot ts : timeSlotList) {
            JSONObject object = new JSONObject();
            try {
                object.put("startingTime", String.valueOf(ts.getStartTime().getTime()));
                object.put("endingTime", String.valueOf(ts.getEndTime().getTime()));
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

        JsonObjectRequest eventsRequest = new SecureJsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "got JSON object:\n" + response.toString());
                        try {
                            ArrayList<Event> events = parseJSONResponseEvents(response);
                            for (final Event event : events) {
                                db.addEvent(event);

                                String participantsListURL = url + "/" + event.getId() + "/" + EVENT_PARTICIPANTS;
                                JsonObjectRequest participantRequest = new SecureJsonObjectRequest(Request.Method.GET, participantsListURL, null,
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
                                JsonObjectRequest pubsRequest = new SecureJsonObjectRequest(Request.Method.GET, pubsListURL, null,
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
                                JsonObjectRequest ownerRequest = new SecureJsonObjectRequest(Request.Method.GET, ownerUrl, null,
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

    protected static void resetPubsDatabase(Context context) {
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

        JsonObjectRequest pubsRequest = new SecureJsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "got JSON object:\n" + response.toString());
                        try {
                            ArrayList<Pub> pubs = parseJsonResponsePubs(response);
                            for (final Pub pub : pubs) {
                                db.addPub(pub);

                                String eventsListURL = url + "/" + pub.getId() + "/" + PUB_EVENTS;
                                JsonObjectRequest pubEventsRequest = new SecureJsonObjectRequest(Request.Method.GET, eventsListURL, null,
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
                                JsonObjectRequest topPersonsRequest = new SecureJsonObjectRequest(Request.Method.GET, topPersonsURL, null,
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
                                JsonObjectRequest ownerRequest = new SecureJsonObjectRequest(Request.Method.GET, ownerUrl, null,
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
                                                Log.e(TAG, "VolleyError for pub " + pub.getPubName() + " id " + pub.getId() + " ownerRequest: " + error.getLocalizedMessage());
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

    protected static void resetPersonsDatabase(Context context) {
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

        JsonObjectRequest personsRequest = new SecureJsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "got JSON object:\n" + response.toString());
                        try {
                            ArrayList<Person> persons = parseJsonResponsePersons(response);
                            for (final Person person : persons) {
                                db.addPerson(person);
                                downloadExternalPersonImage(context, person);

                                String ownEventsListURL = url + "/" + person.getId() + "/" + PERSON_OWNED_EVENTS;
                                JsonObjectRequest ownEventsRequest = new SecureJsonObjectRequest(Request.Method.GET, ownEventsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> events = new ArrayList<>();
                                                try {
                                                    JSONArray eventsJson = response.getJSONObject(EMBEDDED).getJSONArray(EVENTS);
                                                    for (int i = 0; i < eventsJson.length(); i++) {
                                                        JSONObject jsonEvent = eventsJson.getJSONObject(i);
                                                        long id = parseJSONEvent(jsonEvent).getId();
                                                        if (!db.getPerson(person.getId()).getEventIds().contains(id)) events.add(id);
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
                                JsonObjectRequest favouritePubsRequest = new SecureJsonObjectRequest(Request.Method.GET, favouritePubsURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> pubs = new ArrayList<>();
                                                try {
                                                    JSONArray pubsJson = response.getJSONObject(EMBEDDED).getJSONArray(PUBS);
                                                    for (int i = 0; i < pubsJson.length(); i++) {
                                                        JSONObject jsonPub = pubsJson.getJSONObject(i);
                                                        long id = parsePubJson(jsonPub).getId();
                                                        if (!db.getPerson(person.getId()).getFavouritePubIds().contains(id)) pubs.add(id);
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
                                JsonObjectRequest ownedPubsRequest = new SecureJsonObjectRequest(Request.Method.GET, ownPubsURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> pubs = new ArrayList<>();
                                                try {
                                                    JSONArray pubsJson = response.getJSONObject(EMBEDDED).getJSONArray(PUBS);
                                                    for (int i = 0; i < pubsJson.length(); i++) {
                                                        JSONObject jsonPub = pubsJson.getJSONObject(i);
                                                        long id = parsePubJson(jsonPub).getId();
                                                        if (!db.getPerson(person.getId()).getOwnedPubIds().contains(id)) pubs.add(id);
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
                                JsonObjectRequest friendsRequest = new SecureJsonObjectRequest(Request.Method.GET, firendsURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> topPersonIds = new ArrayList<>();
                                                try {
                                                    JSONArray participantsJson = response.getJSONObject(EMBEDDED).getJSONArray(PERSONS);
                                                    for (int i = 0; i < participantsJson.length(); i++) {
                                                        JSONObject jsonParticipant = participantsJson.getJSONObject(i);
                                                        long id = parsePersonJson(jsonParticipant).getId();
                                                        if (!db.getPerson(person.getId()).getFriendIds().contains(id)) topPersonIds.add(id);
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
                                JsonObjectRequest eventsRequest = new SecureJsonObjectRequest(Request.Method.GET, eventsListURL, null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                ArrayList<Long> events = new ArrayList<>();
                                                try {
                                                    JSONArray eventsJson = response.getJSONObject(EMBEDDED).getJSONArray(EVENTS);
                                                    for (int i = 0; i < eventsJson.length(); i++) {
                                                        JSONObject jsonEvent = eventsJson.getJSONObject(i);
                                                        long id = parseJSONEvent(jsonEvent).getId();
                                                        if (!db.getPerson(person.getId()).getEventIds().contains(id)) events.add(id);
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
                }
        );
        requestQueue.add(personsRequest);
    }

    public static void downloadExternalPersonImage(final Context context, final Person person) {
        String url = person.getImageUrl();
        if (url == null || url.length() == 0) return;
        final PersonDbHelper personDbHelper = new PersonDbHelper();
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                person.setImage(response);
                personDbHelper.updatePerson(person);
            }
        }, 600, 400, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(imageRequest);
    }

    public static String getServerUrl(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String url;
        boolean test = prefs.getString("server_ip", null) == null;
        int size = prefs.getString("server_ip", "").length();

        if (prefs.getString("server_ip", null) == null || prefs.getString("server_ip", "").length() < 10) {
            Log.w(TAG, "Server url not set, setting default");
            url = context.getResources().getString(R.string.url);
        } else {
            url = prefs.getString("server_ip", "").replace(" ", "") + "/";
        }

        if (!url.startsWith("http")) url = "http://" + url;
        if (!url.endsWith("/")) url = url + "/";

        return url;
    }

    public static void addFavouritePub(final Context context, final long pubId, final boolean favourite, DetailsCallback detailsCallback) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_user), Context.MODE_PRIVATE);
        long id = sharedPref.getLong(context.getString(R.string.user_id), -1);
        if (id == -1) {
            detailsCallback.onFail();
            return;
        }
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;
        try {
            url = getServerUrl(context) + EVENTS;
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            detailsCallback.onFail();
            return;
        }

        //TODO: make it happen
        detailsCallback.onFail();
    }

    public static void joinEvent(final Context context, final long eventId, final boolean join, final DetailsCallback detailsCallback) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_user), Context.MODE_PRIVATE);
        long id = sharedPref.getLong(context.getString(R.string.user_id), -1);
        if (id == -1) {
            detailsCallback.onFail();
            return;
        }
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;
        try {
            url = getServerUrl(context) + PERSONS + "/" + id + "/" + PERSON_EVENTS;
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            detailsCallback.onFail();
            return;
        }

        final ArrayList<Long> events = new ArrayList<>();
        if (join) {
            events.add(eventId);
            JsonObjectRequest jsonObjectRequest = new EmptyJsonObjectRequest(Request.Method.PATCH, url,
                    eventsListToJson(context, events), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    detailsCallback.onSuccess();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    detailsCallback.onFail();
                    Log.e(TAG, "JoinEvent:onErrorResponse: " + error.toString());
                    error.printStackTrace();
                }
            });
            requestQueue.add(jsonObjectRequest);
        } else {
            JsonObjectRequest getEventsListRequest = new SecureJsonObjectRequest(Request.Method.GET, url,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    JSONArray jsonEvents;
                    try {
                        jsonEvents = response.getJSONObject(EMBEDDED).getJSONArray(EVENTS);
                        for (int i = 0; i < jsonEvents.length(); i++) {
                            try {
                                JSONObject event = jsonEvents.getJSONObject(i);
                                JSONObject jsonLinks = event.getJSONObject(LINKS);
                                long id = parseIdFromHref(jsonLinks.getJSONObject(SELF).getString(HREF));
                                if (eventId != id) {
                                    events.add(id);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsonObjectRequest = new EmptyJsonObjectRequest(Request.Method.PUT, url,
                            eventsListToJson(context, events), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            detailsCallback.onSuccess();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            detailsCallback.onFail();
                            Log.e(TAG, "JoinEvent:onErrorResponse: " + error.toString());
                            error.printStackTrace();
                        }
                    });
                    requestQueue.add(jsonObjectRequest);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    detailsCallback.onFail();
                    Log.e(TAG, "JoinEvent:onErrorResponse: " + error.toString());
                    error.printStackTrace();
                }
            });
            requestQueue.add(getEventsListRequest);
        }

    }


    public void addEventOwner(final Context context, final long eventId, final NewEventActivity.SetOwner setOwnerInterface) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_user), Context.MODE_PRIVATE);
        long id = sharedPref.getLong(context.getString(R.string.user_id), -1);
        if (id == -1) {
            setOwnerInterface.onFail();
            return;
        }
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        final String url;
        try {
            //url = getServerUrl(context) + PERSONS + "/" + id + "/" + PERSON_OWNED_EVENTS;
            url = getServerUrl(context) + EVENTS + "/" + eventId + "/" + EVENT_OWNER;
        } catch (StringIndexOutOfBoundsException e) {
            Log.e(TAG, e.getLocalizedMessage());
            setOwnerInterface.onFail();
            return;
        }

        JSONObject obj = prepareEventOwner(eventId);

        JsonObjectRequest jsonObjectRequest = new EmptyJsonObjectRequest(Request.Method.PUT, url,
                obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setOwnerInterface.onSuccess();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setOwnerInterface.onFail();
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);


    }

    private JSONObject prepareEventOwner(long eventid) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("", "https://vm-74243.lal.in2p3.fr:8443/crawlers/13");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        /*
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(eventid);


        try {
            jsonObject.put("events",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/
        return jsonObject;

    }

    private static JSONObject eventsListToJson(Context context, ArrayList<Long> events) {
        JSONObject object = new JSONObject();
        try {
            JSONArray items = new JSONArray();
            for (Long id : events) {
                String link = getServerUrl(context) + EVENTS + "/" + id;
                JSONObject o = new JSONObject();
                try {
                    o.put("href", link);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                items.put(o);
            }
            JSONObject links = new JSONObject();
            links.put("items", items);
            object.put("_links", links);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        return object;
    }


}
