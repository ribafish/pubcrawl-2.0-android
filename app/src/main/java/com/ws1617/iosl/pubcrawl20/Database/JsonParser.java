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
    private static final String EVENT_IMAGE = "image";
    private static final String EVENT_LAT_MIN = "lat_min";
    private static final String EVENT_LAT_MAX = "lat_max";
    private static final String EVENT_LONG_MIN = "long_min";
    private static final String EVENT_LONG_MAX = "long_max";

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
    private static final String PUB_PRICES = "prices";
    private static final String PUB_RATING = "rating";
    private static final String PUB_LAT = "lat";
    private static final String PUB_LONG = "lng";
    private static final String PUB_DESCRIPTION = "description";
    private static final String PUB_SIZE = "size";
    private static final String PUB_OPENING_TIME = "closingTime";

    public static final String PUB = "pub";
    public static final String PUB_OWNER = "pubOwner";
    public static final String PUB_TOP_PERSONS = "topsList";
    public static final String PUB_EVENTS = "eventsList";
    public static final String PUB_IMAGES = "images";


    // Person
    public static final String PERSONS = "crawlers";
    private static final String PERSON_NAME = "userName";
    private static final String PERSON_EMAIL = "email";
    private static final String PERSON_DESCRIPTION = "description";
    private static final String PERSON_IMAGE = "userImage";

    public static final String PERSON = "crawler";
    public static final String PERSON_OWNED_EVENTS = "ownEvents";
    public static final String PERSON_OWNED_PUBS = "ownPubs";
    public static final String PERSON_FRIENDS = "friendsList";
    public static final String PERSON_EVENTS = "eventsList";
    public static final String PERSON_FAVOURITE_PUBS = "favourites";



    public static ArrayList<Event> parseJSONResponseEvents (JSONObject response) throws JSONException, ParseException  {
        ArrayList<Event> events = new ArrayList<>();
        JSONObject embedded = response.getJSONObject(EMBEDDED);
        JSONArray jsonEvents = embedded.getJSONArray(EVENTS);
        for (int i=0; i < jsonEvents.length(); i++) {
            JSONObject jsonEvent = jsonEvents.getJSONObject(i);
            Event e = parseJSONEvent(jsonEvent);
            Log.d(TAG, "Parsed event [" + i + "]: " + e);
            events.add(e);
        }
        Collections.sort(events, new EventComparator(true, EventComparator.NAME));
        return events;
    }

    public static Event parseJSONEvent (JSONObject jsonEvent) throws JSONException, ParseException {
        final Event event = new Event();
        DateFormat dateFormat = new SimpleDateFormat(EVENT_DATE_FORMAT, Locale.ENGLISH);
        event.setDate(dateFormat.parse(jsonEvent.getString(EVENT_DATE)));
        event.setEventName(jsonEvent.getString(EVENT_NAME));
        event.setDescription(jsonEvent.getString(EVENT_DESCRIPTION));
        event.setTracked(jsonEvent.getBoolean(EVENT_TRACKED));
        event.setImage(decodeBitmapBase64(jsonEvent.getString(EVENT_IMAGE)));
        event.setMinLatLng(new LatLng(
                jsonEvent.getDouble(EVENT_LAT_MIN),
                jsonEvent.getDouble(EVENT_LONG_MIN)));
        event.setMaxLatLng(new LatLng(
                jsonEvent.getDouble(EVENT_LAT_MAX),
                jsonEvent.getDouble(EVENT_LONG_MAX)));

        ArrayList<TimeSlot> timeSlots = new ArrayList<>();
        DateFormat timeSlotFormat = new SimpleDateFormat(EVENT_TIMESLOT_TIME_FORMAT, Locale.ENGLISH);
        JSONArray jsonTimeSlots = jsonEvent.getJSONArray(EVENT_TIMESLOTS);
        for (int i=0; i < jsonTimeSlots.length(); i++) {
            JSONObject slot = jsonTimeSlots.getJSONObject(i);
            TimeSlot ts = new TimeSlot(
                    slot.getLong(EVENT_TIMESLOT_PUB_ID),
                    timeSlotFormat.parse(slot.getString(EVENT_TIMESLOT_START)),
                    timeSlotFormat.parse(slot.getString(EVENT_TIMESLOT_END))
            );
            timeSlots.add(ts);
        }
        event.setTimeSlotList(timeSlots);

        JSONObject jsonLinks = jsonEvent.getJSONObject(LINKS);
        long eventId = parseIdFromHref(jsonLinks.getJSONObject(SELF).getString(HREF));
        event.setEventId(eventId);

        return event;
    }

    public static ArrayList<Pub> parseJsonResponsePubs (JSONObject response) throws JSONException {
        ArrayList<Pub> pubs = new ArrayList<>();
        JSONObject embedded = response.getJSONObject(EMBEDDED);
        JSONArray jsonPubs = embedded.getJSONArray(PUBS);
        for (int i=0; i < jsonPubs.length(); i++) {
            JSONObject jsonPub = jsonPubs.getJSONObject(i);
            Pub p = parsePubJson(jsonPub);
            Log.d(TAG, "Parsed pub [" + i + "]: " + p);
            pubs.add(p);
        }

        return pubs;
    }

    public static Pub parsePubJson (JSONObject jsonPub) throws JSONException {
        Pub pub = new Pub();

        pub.setId(parseIdFromHref(jsonPub.getJSONObject(LINKS).getJSONObject(SELF).getString(HREF)));
        pub.setPubName(jsonPub.getString(PUB_NAME));
        pub.setPrices(jsonPub.getInt(PUB_PRICES));
        pub.setRating(jsonPub.getInt(PUB_RATING));
        pub.setLatLng(new LatLng(
                jsonPub.getDouble(PUB_LAT),
                jsonPub.getDouble(PUB_LONG)));
        pub.setDescription(jsonPub.getString(PUB_DESCRIPTION));
        pub.setSize(jsonPub.getInt(PUB_SIZE));
        pub.setOpeningTimes(jsonPub.getString(PUB_OPENING_TIME));

        return pub;
    }

    public static ArrayList<Person> parseJsonResponsePersons (JSONObject response) throws JSONException {
        ArrayList<Person> persons = new ArrayList<>();

        JSONObject embedded = response.getJSONObject(EMBEDDED);
        JSONArray json = embedded.getJSONArray(PERSONS);
        for (int i=0; i < json.length(); i++) {
            JSONObject jsonPerson = json.getJSONObject(i);
            Person person = parsePersonJson(jsonPerson);
            Log.d(TAG, "Parsed event [" + i + "]: " + person);
            persons.add(person);
        }

        return persons;
    }


    public static Person parsePersonJson (JSONObject jsonPerson) throws JSONException {
        long id = parseIdFromHref(jsonPerson
                .getJSONObject(LINKS)
                .getJSONObject(SELF)
                .getString(HREF));
        Person person = new Person(id);

        person.setName(jsonPerson.getString(PERSON_NAME));
        person.setEmail(jsonPerson.getString(PERSON_EMAIL));
        person.setDescription(jsonPerson.getString(PERSON_DESCRIPTION));
        person.setImage(decodeBitmapBase64(jsonPerson.getString(PERSON_IMAGE)));

        return person;
    }

    private static long parseIdFromHref(String href) {
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
