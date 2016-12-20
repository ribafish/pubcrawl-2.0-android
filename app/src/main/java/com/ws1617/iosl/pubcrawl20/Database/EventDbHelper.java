package com.ws1617.iosl.pubcrawl20.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

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
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_EVENT_TIMESLOTS);
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_EVENT_PARTICIPANTS);
        db.execSQL("DROP IF TABLE EXISTS " + TABLE_EVENT_PUBS);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Add to event table
        ContentValues values = new ContentValues();
        values.put(EVENT_ID, event.getEventId());
        values.put(EVENT_NAME, event.getEventName());
        values.put(DATE, event.getDate().getTime());    // epoch time in ms
        values.put(DESCRIPTION, event.getDescription());
        values.put(TRACKED, event.isTracked() ? 1 : 0);
        values.put(OWNER, event.getOwnerId());
        values.put(IMAGE, bitmapToBytes(event.getImage()));

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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values;

        for (TimeSlot ts : event.getTimeSlotList()) {
            values = new ContentValues();
            values.put(EVENT_ID, event.getEventId());
            values.put(PUB_ID, ts.getPubId());
            values.put(START_TIME, ts.getStartTime().getTime());
            values.put(END_TIME, ts.getEndTime().getTime());
            db.insert(TABLE_EVENT_TIMESLOTS, null, values);
        }
    }

    public void addParticipants(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values;

        for (long id : event.getParticipantIds()) {
            values = new ContentValues();
            values.put(EVENT_ID, event.getEventId());
            values.put(PARTICIPANT_ID, id);
            db.insert(TABLE_EVENT_PARTICIPANTS, null, values);
        }
    }

    public void addPubs(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values;

        for (long id : event.getPubIds()) {
            values = new ContentValues();
            values.put(EVENT_ID, event.getEventId());
            values.put(PUB_ID, id);
            db.insert(TABLE_EVENT_PUBS, null, values);
        }
    }


    public void updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENT_ID, event.getEventId());
        values.put(EVENT_NAME, event.getEventName());
        values.put(DATE, event.getDate().getTime());    // epoch time in ms
        values.put(DESCRIPTION, event.getDescription());
        values.put(TRACKED, event.isTracked() ? 1 : 0);
        values.put(OWNER, event.getOwnerId());
        values.put(IMAGE, bitmapToBytes(event.getImage()));

        String selection = EVENT_ID + " =?";
        String[] selectionArgs = {String.valueOf(event.getEventId())};

        db.update(TABLE_EVENTS, values, selection, selectionArgs);

        updateLists(event);
    }

    public void updateLists(Event event) {
        deleteLists(event.getEventId());

        addTimeslots(event);
        addParticipants(event);
        addPubs(event);
    }

    public void deleteLists(long event_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = EVENT_ID + " =?";
        String[] selectionArgs = {String.valueOf(event_id)};

        db.delete(TABLE_EVENT_TIMESLOTS, selection, selectionArgs);
        db.delete(TABLE_EVENT_PARTICIPANTS, selection, selectionArgs);
        db.delete(TABLE_EVENT_PUBS, selection, selectionArgs);
    }

    public void deleteEvent(long event_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = EVENT_ID + " =?";
        String[] selectionArgs = {String.valueOf(event_id)};

        db.delete(TABLE_EVENTS, selection, selectionArgs);

        deleteLists(event_id);
    }

    public Event getListlessEvent(long event_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Event event = new Event();

        String query = "SELECT * FROM " +
                TABLE_EVENTS +  " WHERE " +
                EVENT_ID + " =? " + event_id;

        Cursor c = db.rawQuery(query, null);

        if (c != null) {
            c.moveToFirst();

            event.setEventId(event_id);
            event.setEventName(c.getString(c.getColumnIndex(EVENT_NAME)));
            event.setDate(new Date(c.getLong(c.getColumnIndex(DATE))));
            event.setDescription(c.getString(c.getColumnIndex(DESCRIPTION)));
            event.setTracked(1 == c.getInt(c.getColumnIndex(TRACKED)));
            event.setOwnerId(c.getLong(c.getColumnIndex(OWNER)));
            Bitmap image = bytesToBitmap(c.getBlob(6));
            event.setImage(image);

            c.close();
        } else {
            Log.e(TAG, "Can't find event with id " + event_id);
            return null;
        }

        return event;
    }

    public Event getEvent (long event_id) {
        Event event = getListlessEvent(event_id);

        event.setTimeSlotList(getTimeSlots(event_id));
        event.setParticipantIds(getParticipantIds(event_id));
        event.setPubIds(getPubIds(event_id));

        return event;
    }

    public ArrayList<TimeSlot> getTimeSlots(long event_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TimeSlot> timeSlots = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_EVENT_TIMESLOTS +  " WHERE " +
                EVENT_ID + " =? " + event_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            c.moveToFirst();
            do {
                TimeSlot slot = new TimeSlot(
                        c.getLong(c.getColumnIndex(PUB_ID)),
                        new Date(c.getLong(c.getColumnIndex(START_TIME))),
                        new Date(c.getLong(c.getColumnIndex(END_TIME)))
                );
                timeSlots.add(slot);
            } while (c.moveToNext());
            c.close();
        }

        return timeSlots;
    }

    public ArrayList<Long> getParticipantIds(long event_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_EVENT_PARTICIPANTS +  " WHERE " +
                EVENT_ID + " =? " + event_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            c.moveToFirst();
            do {
                list.add(c.getLong(c.getColumnIndex(PARTICIPANT_ID)));
            } while (c.moveToNext());
            c.close();
        }

        return list;
    }

    public ArrayList<Long> getPubIds(long event_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_EVENT_PUBS +  " WHERE " +
                EVENT_ID + " =? " + event_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            c.moveToFirst();
            do {
                list.add(c.getLong(c.getColumnIndex(PUB_ID)));
            } while (c.moveToNext());
            c.close();
        }

        return list;
    }
}
