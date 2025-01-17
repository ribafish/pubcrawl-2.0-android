package com.ws1617.iosl.pubcrawl20.Database;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.EventComparator;
import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class JsonParser {
    private static final String TAG = "JsonParser";

    // JSON keywords
    public static final String EMBEDDED = "_embedded";
    public static final String LINKS = "_links";
    public static final String HREF = "href";
    public static final String SELF = "self";


    // Event
    public static final String EVENTS = "events";
    private static final String EVENT_NAME = "eventName";
    private static final String EVENT_DATE = "date";
    private static final String EVENT_DATE_FORMAT = "dd.MM.yyyy";
    private static final String EVENT_DESCRIPTION = "description";
    private static final String EVENT_TRACKED = "tracked";
    private static final String EVENT_IMAGE = "eventImage";
    private static final String EVENT_LAT_MIN = "latmin";
    private static final String EVENT_LAT_MAX = "latmax";
    private static final String EVENT_LONG_MIN = "lngmin";
    private static final String EVENT_LONG_MAX = "lngmax";

    private static final String EVENT_TIMESLOTS = "timeslotList";
    private static final String EVENT_TIMESLOT_TIME_FORMAT = "HH:mm:ss";
    private static final String EVENT_TIMESLOT_PUB_ID = "pubId";
    private static final String EVENT_TIMESLOT_START = "startingTime";
    private static final String EVENT_TIMESLOT_END = "endingTime";

    public static final String EVENT = "event";
    public static final String EVENT_PARTICIPANTS = "participantsList";
    public static final String EVENT_OWNER = "eventOwner";
    public static final String EVENT_PUBS = "pubsList";


    // Pub
    public static final String PUBS = "pubs";
    private static final String PUB_NAME = "pubName";
    private static final String PUB_PRICES = "price";
    private static final String PUB_RATING = "rating";
    private static final String PUB_LAT = "lat";
    private static final String PUB_LONG = "lng";
    private static final String PUB_DESCRIPTION = "description";
    private static final String PUB_SIZE = "size";
    private static final String PUB_OPENING_TIME = "openingTime";
    private static final String PUB_CLOSING_TIME = "closingTime";

    public static final String PUB = "pub";
    public static final String PUB_OWNER = "pubOwner";
    public static final String PUB_TOP_PERSONS = "topsList";
    public static final String PUB_EVENTS = "eventsList";
    public static final String PUB_IMAGES = "images";
    public static final String PUB_IMAGE = "pubImage";



    // Person
    public static final String PERSONS = "crawlers";
    public static final String PERSON_NAME = "userName";
    public static final String PERSON_PROFILE = "profileID";
    public static final String PERSON_DESCRIPTION = "description";
    public static final String PERSON_IMAGE = "userImage";

    public static final String PERSON = "crawler";
    public static final String PERSON_OWNED_EVENTS = "ownEvents";
    public static final String PERSON_OWNED_PUBS = "ownPubs";
    public static final String PERSON_FRIENDS = "friendsList";
    public static final String PERSON_EVENTS = "eventsList";
    public static final String PERSON_FAVOURITE_PUBS = "favourites";


    public static ArrayList<Event> parseJSONResponseEvents (JSONObject response) throws JSONException  {
        ArrayList<Event> events = new ArrayList<>();
        JSONObject embedded = response.getJSONObject(EMBEDDED);
        JSONArray jsonEvents = embedded.getJSONArray(EVENTS);
        for (int i=0; i < jsonEvents.length(); i++) {
            JSONObject jsonEvent = jsonEvents.getJSONObject(i);
            try {
                Event e = parseJSONEvent(jsonEvent);
                events.add(e);
            } catch (Exception e) {
                Log.e(TAG, "Error for parsing event json " + jsonEvent);
                Log.e(TAG, "Error message: " + e.getLocalizedMessage());
            }
        }
        Collections.sort(events, new EventComparator(true, EventComparator.NAME));
        Log.d(TAG, "Parsed all events");
        return events;
    }

    public static Event parseJSONEvent (JSONObject jsonEvent) throws JSONException, ParseException {
        final Event event = new Event();
        JSONObject jsonLinks = jsonEvent.getJSONObject(LINKS);
        long eventId = parseIdFromHref(jsonLinks.getJSONObject(SELF).getString(HREF));
        event.setId(eventId);
        DateFormat dateFormat = new SimpleDateFormat(EVENT_DATE_FORMAT, Locale.ENGLISH);
        try {
//            event.setDate(dateFormat.parse(jsonEvent.getString(EVENT_DATE)));
            event.setDate(new Date(jsonEvent.getLong(EVENT_DATE)));
//        } catch (ParseException e) {
//            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            event.setEventName(jsonEvent.getString(EVENT_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            event.setDescription(jsonEvent.getString(EVENT_DESCRIPTION));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            event.setTracked(jsonEvent.getBoolean(EVENT_TRACKED));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            event.setImage(decodeBitmapBase64(jsonEvent.getString(EVENT_IMAGE)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            event.setMinLatLng(new LatLng(
                    jsonEvent.getDouble(EVENT_LAT_MIN),
                    jsonEvent.getDouble(EVENT_LONG_MIN)));
            event.setMaxLatLng(new LatLng(
                    jsonEvent.getDouble(EVENT_LAT_MAX),
                    jsonEvent.getDouble(EVENT_LONG_MAX)));
        } catch (Exception e) {
            Log.w(TAG, "Can't parse event " + event.getEventName() + " id " + event.getId() + " boundary box:" + e.getLocalizedMessage());
        }

        try {
            ArrayList<TimeSlot> timeSlots = new ArrayList<>();
            DateFormat timeSlotFormat = new SimpleDateFormat(EVENT_TIMESLOT_TIME_FORMAT, Locale.ENGLISH);
            JSONArray jsonTimeSlots = jsonEvent.getJSONArray(EVENT_TIMESLOTS);
            for (int i = 0; i < jsonTimeSlots.length(); i++) {
                try {
                    JSONObject slot = jsonTimeSlots.getJSONObject(i);
//                    TimeSlot ts = new TimeSlot(
//                            slot.getLong(EVENT_TIMESLOT_PUB_ID),
//                            timeSlotFormat.parse(slot.getString(EVENT_TIMESLOT_START)),
//                            timeSlotFormat.parse(slot.getString(EVENT_TIMESLOT_END))
//                    );
                    TimeSlot ts = new TimeSlot(
                            slot.getLong(EVENT_TIMESLOT_PUB_ID),
                            new Date(slot.getLong(EVENT_TIMESLOT_START)),
                            new Date(slot.getLong(EVENT_TIMESLOT_END))
                    );
                    timeSlots.add(ts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                catch (ParseException e) {
//                    e.printStackTrace();
//                }
            }
            event.setTimeSlotList(timeSlots);
        } catch (Exception e) {
            Log.e(TAG, "Can't parse event " + event.getEventName() + " id " + event.getId() + " timeslot list:" + e.getLocalizedMessage());
        }

        Log.d(TAG, "Parsed event: " + event);

        return event;
    }

    public static ArrayList<Pub> parseJsonResponsePubs (JSONObject response) throws JSONException {
        ArrayList<Pub> pubs = new ArrayList<>();
        Log.v(TAG, "Got Pubs JSON response: " + response);
        JSONObject embedded = response.getJSONObject(EMBEDDED);
        JSONArray jsonPubs = embedded.getJSONArray(PUBS);
        for (int i=0; i < jsonPubs.length(); i++) {
            JSONObject jsonPub = jsonPubs.getJSONObject(i);
            try {
                Pub p = parsePubJson(jsonPub);
                pubs.add(p);
            } catch (Exception e) {
                Log.e(TAG, "Error for parsing pub json " + jsonPub);
                Log.e(TAG, "Error message: " + e.getLocalizedMessage());
            }
        }

        Log.d(TAG, "Parsed all pubs");

        return pubs;
    }

    public static Pub parsePubJson (JSONObject jsonPub) throws JSONException {
        Pub pub = new Pub();

        try {
            pub.setId(parseIdFromHref(jsonPub.getJSONObject(LINKS).getJSONObject(SELF).getString(HREF)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pub.setPubName(jsonPub.getString(PUB_NAME));
        try {
            pub.setPrices(jsonPub.getInt(PUB_PRICES));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            pub.setRating(jsonPub.getInt(PUB_RATING));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            pub.setLatLng(new LatLng(
                    jsonPub.getDouble(PUB_LAT),
                    jsonPub.getDouble(PUB_LONG)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            pub.setDescription(jsonPub.getString(PUB_DESCRIPTION));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            pub.setSize(jsonPub.getInt(PUB_SIZE));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String s = jsonPub.getString(PUB_OPENING_TIME) +
                    " - " +
                    jsonPub.getString(PUB_CLOSING_TIME);
            pub.setOpeningTimes(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            pub.addImage(decodeBitmapBase64(jsonPub.getString(PUB_IMAGE)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Parsed Pub: " + pub);

        return pub;
    }

    public static ArrayList<Person> parseJsonResponsePersons (JSONObject response) throws JSONException {
        ArrayList<Person> persons = new ArrayList<>();

        JSONObject embedded = response.getJSONObject(EMBEDDED);
        JSONArray json = embedded.getJSONArray(PERSONS);
        for (int i=0; i < json.length(); i++) {
            JSONObject jsonPerson = json.getJSONObject(i);
            try {
                Person person = parsePersonJson(jsonPerson);
                persons.add(person);
            } catch (Exception e) {
                Log.e(TAG, "Error for parsing person json " + jsonPerson);
                Log.e(TAG, "Error message: " + e.getLocalizedMessage());
            }
        }

        Log.d(TAG, "Parsed all persons");

        return persons;
    }


    public static Person parsePersonJson (JSONObject jsonPerson) throws JSONException {
        long id = parseIdFromHref(jsonPerson
                .getJSONObject(LINKS)
                .getJSONObject(SELF)
                .getString(HREF));
        Person person = new Person(id);

        try {
            person.setName(jsonPerson.getString(PERSON_NAME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            person.setEmail(jsonPerson.getString(PERSON_PROFILE));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            person.setDescription(jsonPerson.getString(PERSON_DESCRIPTION));
        } catch (JSONException e) {
            Log.v(TAG, "Can't person event " + person.getName() + " id " + person.getId() + " description:" + e.getLocalizedMessage());
//            e.printStackTrace();
        }
        try {
            person.setImageUrl(jsonPerson.getString(PERSON_IMAGE));
        } catch (JSONException e) {
            Log.v(TAG, "Can't person event " + person.getName() + " id " + person.getId() + " image:" + e.getLocalizedMessage());
//            e.printStackTrace();
        }

        Log.d(TAG, "Parsed person: " + person);

        return person;
    }

    public static long parseIdFromHref(String href) {
        return Long.parseLong(href.split("\\/+")[3]);
    }


    /**
     * Encodes Bitmap image to Base64 string
     * @param image bitmap image
     * @param compressFormat compressFormat, for example Bitmap.CompressFormat.JPEG
     * @param quality quality from 0 to 100
     * @return base64 encoded String
     */
    public static String encodeBitmapToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static String encodeBitmapToBase64(Bitmap image) {
        return encodeBitmapToBase64(image, Bitmap.CompressFormat.JPEG, 100);
    }

    public static Bitmap decodeBitmapBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
