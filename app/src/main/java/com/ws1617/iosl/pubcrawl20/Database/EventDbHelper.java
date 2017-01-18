package com.ws1617.iosl.pubcrawl20.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;

import java.util.ArrayList;
import java.util.Date;

import static com.ws1617.iosl.pubcrawl20.Database.Contracts.EventContract.*;
import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.*;

/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class EventDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "EventDbHelper";

    public EventDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create event tables
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_TIMESLOTS_TABLE);
        db.execSQL(CREATE_PARTICIPANTS_TABLE);
        db.execSQL(CREATE_PUBS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_TIMESLOTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_PARTICIPANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_PUBS);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addEvent(Event event) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        // Add to event table
        ContentValues values = new ContentValues();
        values.put(EVENT_ID, event.getId());
        values.put(EVENT_NAME, event.getEventName());
        values.put(DATE, event.getDate().getTime());    // epoch time in ms
        values.put(DESCRIPTION, event.getDescription());
        values.put(TRACKED, event.isTracked() ? 1 : 0);
        values.put(OWNER, event.getOwnerId());
        values.put(IMAGE, bitmapToBytes(event.getImage()));
        try {
            values.put(LAT_MIN, event.getMinLatLng().latitude);
            values.put(LONG_MIN, event.getMinLatLng().longitude);
            values.put(LAT_MAX, event.getMaxLatLng().latitude);
            values.put(LONG_MAX, event.getMaxLatLng().longitude);
        }
        catch (Exception e) { e.printStackTrace(); }


        long row_id = db.insert(TABLE_EVENTS, null, values);
        if (row_id == -1) {
            updateEvent(event);
        } else {
            addTimeslots(event);
            addParticipants(event);
            addPubs(event);
        }
    }

    public void addTimeslots(Event event) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values;

        for (TimeSlot ts : event.getTimeSlotList()) {
            values = new ContentValues();
            values.put(EVENT_ID, event.getId());
            values.put(PUB_ID, ts.getPubId());
            values.put(START_TIME, ts.getStartTime().getTime());
            values.put(END_TIME, ts.getEndTime().getTime());
            db.insert(TABLE_EVENT_TIMESLOTS, null, values);
        }
    }

    public void addParticipants(Event event) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values;

        for (long id : event.getParticipantIds()) {
            values = new ContentValues();
            values.put(EVENT_ID, event.getId());
            values.put(PARTICIPANT_ID, id);
            db.insert(TABLE_EVENT_PARTICIPANTS, null, values);
        }
    }

    public void addPubs(Event event) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values;

        for (long id : event.getPubIds()) {
            values = new ContentValues();
            values.put(EVENT_ID, event.getId());
            values.put(PUB_ID, id);
            db.insert(TABLE_EVENT_PUBS, null, values);
        }
    }


    public void updateEvent(Event event) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENT_ID, event.getId());
        values.put(EVENT_NAME, event.getEventName());
        values.put(DATE, event.getDate().getTime());    // epoch time in ms
        values.put(DESCRIPTION, event.getDescription());
        values.put(TRACKED, event.isTracked() ? 1 : 0);
        values.put(OWNER, event.getOwnerId());
        values.put(IMAGE, bitmapToBytes(event.getImage()));
        values.put(LAT_MIN, event.getMinLatLng().latitude);
        values.put(LONG_MIN, event.getMinLatLng().longitude);
        values.put(LAT_MAX, event.getMaxLatLng().latitude);
        values.put(LONG_MAX, event.getMaxLatLng().longitude);

        String selection = EVENT_ID + " =?";
        String[] selectionArgs = {String.valueOf(event.getId())};

        db.update(TABLE_EVENTS, values, selection, selectionArgs);
        updateLists(event);
    }

    public void updateEventOwner(long event_id, long owner_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(OWNER, owner_id);

        String selection = EVENT_ID + " =?";
        String[] selectionArgs = {String.valueOf(event_id)};

        db.update(TABLE_EVENTS, values, selection, selectionArgs);
    }

    public void updateLists(Event event) {
        deleteLists(event.getId());

        addTimeslots(event);
        addParticipants(event);
        addPubs(event);
    }

    public void deleteLists(long event_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        String selection = EVENT_ID + " =?";
        String[] selectionArgs = {String.valueOf(event_id)};

        db.delete(TABLE_EVENT_TIMESLOTS, selection, selectionArgs);
        db.delete(TABLE_EVENT_PARTICIPANTS, selection, selectionArgs);
        db.delete(TABLE_EVENT_PUBS, selection, selectionArgs);
    }

    public void deleteEvent(long event_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        String selection = EVENT_ID + " =?";
        String[] selectionArgs = {String.valueOf(event_id)};

        db.delete(TABLE_EVENTS, selection, selectionArgs);
        deleteLists(event_id);
    }

    private Event getListlessEventFromCursor(Cursor c) {
        Event event = new Event();

        event.setId(c.getLong(c.getColumnIndex(EVENT_ID)));
        event.setEventName(c.getString(c.getColumnIndex(EVENT_NAME)));
        event.setDate(new Date(c.getLong(c.getColumnIndex(DATE))));
        event.setDescription(c.getString(c.getColumnIndex(DESCRIPTION)));
        event.setTracked(1 == c.getInt(c.getColumnIndex(TRACKED)));
        event.setOwnerId(c.getLong(c.getColumnIndex(OWNER)));
        Bitmap image = bytesToBitmap(c.getBlob(c.getColumnIndex(IMAGE)));
        event.setImage(image);
        event.setMinLatLng(new LatLng(c.getDouble(c.getColumnIndex(LAT_MIN)),
                c.getDouble(c.getColumnIndex(LONG_MIN))));
        event.setMaxLatLng(new LatLng(c.getDouble(c.getColumnIndex(LAT_MAX)),
                c.getDouble(c.getColumnIndex(LONG_MAX))));

        return event;
    }

    public Event getListlessEvent(long event_id) throws DatabaseException {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Event event;

        String query = "SELECT * FROM " +
                TABLE_EVENTS +  " WHERE " +
                EVENT_ID + " = " + event_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {

            event = getListlessEventFromCursor(c);

            c.close();
        } else {
            Log.e(TAG, "Can't find event with id " + event_id );
            throw new DatabaseException("Can't find event with id " + event_id + ". Cursor is null or database empty");
        }

        return event;
    }

    public Event getEvent (long event_id) throws DatabaseException {
        Event event = getListlessEvent(event_id);

        event.setTimeSlotList(getTimeSlots(event_id));
        event.setParticipantIds(getParticipantIds(event_id));
        event.setPubIds(getPubIds(event_id));

        return event;
    }

    public ArrayList<TimeSlot> getTimeSlots(long event_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<TimeSlot> timeSlots = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_EVENT_TIMESLOTS +  " WHERE " +
                EVENT_ID + " = " + event_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                TimeSlot slot = new TimeSlot(
                        c.getLong(c.getColumnIndex(PUB_ID)),
                        new Date(c.getLong(c.getColumnIndex(START_TIME))),
                        new Date(c.getLong(c.getColumnIndex(END_TIME)))
                );
                timeSlots.add(slot);
            } while (c.moveToNext());
            c.close();
        } else {
            Log.w(TAG, "getTimeSlots: Cursor is null or database empty");
        }

        return timeSlots;
    }

    public ArrayList<Long> getParticipantIds(long event_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_EVENT_PARTICIPANTS +  " WHERE " +
                EVENT_ID + " = " + event_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(PARTICIPANT_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.w(TAG, "getParticipantIds: Cursor is null or database empty");
        }

        return list;
    }

    public ArrayList<Long> getPubIds(long event_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_EVENT_PUBS +  " WHERE " +
                EVENT_ID + " = " + event_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(PUB_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.w(TAG, "getPubIds: Cursor is null or database empty");
        }

        return list;
    }

    public ArrayList<Event> getAllEvents() throws DatabaseException {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Event> events = new ArrayList<>();

        String query = "SELECT * FROM " + TABLE_EVENTS;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                Event event = getListlessEventFromCursor(c);
                event.setPubIds(getPubIds(event.getId()));
                event.setParticipantIds(getParticipantIds(event.getId()));
                event.setTimeSlotList(getTimeSlots(event.getId()));
                events.add(event);
            } while (c.moveToNext());
            c.close();
        } else {
            throw new DatabaseException("Database not initialized, cursor is null");
        }

        Log.d(TAG, "getAllEvents: Found " + events.size() + " events");

        return events;
    }

    public ArrayList<Event> getEventsBoundryBox(long event_id, LatLng minLatLng, LatLng maxLatLng) {
        ArrayList<Event> events = new ArrayList<>();

        //TODO

        return null;
    }

    public ArrayList<Event> getEventsBoundryBoxTimeFrame(long event_id, LatLng minLatLng, LatLng maxLatLng, Date min, Date max) {
        ArrayList<Event> events = new ArrayList<>();

        //TODO

        return null;
    }

}
