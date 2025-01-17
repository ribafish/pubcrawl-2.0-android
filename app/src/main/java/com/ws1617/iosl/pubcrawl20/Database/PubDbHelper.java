package com.ws1617.iosl.pubcrawl20.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;

import java.util.ArrayList;
import java.util.List;

import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PubContract.*;
import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.*;

/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class PubDbHelper  {
    private static final String TAG = "PubDbHelper";

    public PubDbHelper()    {}

    public static void onCreate(SQLiteDatabase db) {
        // create pub tables
        db.execSQL(CREATE_PUBS_TABLE);
        db.execSQL(CREATE_TOP_PERSONS_TABLE);
        db.execSQL(CREATE_PUB_EVENTS_TABLE);
        db.execSQL(CREATE_PUB_IMAGES_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUBS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOP_PERSONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUB_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUB_IMAGES);

        onCreate(db);
    }

    public static void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addPub(Pub pub) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);

        ContentValues values = new ContentValues();
        values.put(PUB_ID, pub.getId());
        values.put(PUB_NAME, pub.getPubName());
        values.put(LATITUDE, pub.getLatLng().latitude);
        values.put(LONGITUDE, pub.getLatLng().longitude);
        values.put(SIZE, pub.getSize());
        values.put(PRICES, pub.getPrices());
        values.put(RATING, pub.getRating());
        values.put(OPENING_TIMES, pub.getOpeningTimes());
        values.put(OWNER, pub.getOwnerId());

        long row_id = db.insert(TABLE_PUBS, null, values);
        if (row_id == -1) {
            updatePub(pub);
        } else {
            addTopPersons(pub);
            addPubEvents(pub);
            addPubImages(pub);
        }
    }

    public void addTopPersons(Pub pub) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);
        ContentValues values;

        for (int i = 0; i < pub.getTopsListIds().size(); i++) {
            values = new ContentValues();
            values.put(PUB_ID, pub.getId());
            values.put(PERSON_ID, pub.getTopsListIds().get(i));
            values.put(ITERATOR, i);
            db.insert(TABLE_TOP_PERSONS, null, values);
        }
    }

    public void addPubEvents(Pub pub) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);
        ContentValues values;

        for (long id : pub.getEventsListIds()) {
            values = new ContentValues();
            values.put(PUB_ID, pub.getId());
            values.put(EVENT_ID, id);
            db.insert(TABLE_PUB_EVENTS, null, values);
        }
    }

    public void addPubImages(Pub pub) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);
        ContentValues values;
        if (pub.getImages()== null || pub.getImages().size() == 0) return;

        for (int i = 0; i < pub.getImages().size(); i++) {
            values = new ContentValues();
            values.put(PUB_ID, pub.getId());
            values.put(ITERATOR, i);
            values.put(IMAGE, bitmapToBytes(pub.getImages().get(i)));
            db.insert(TABLE_PUB_IMAGES, null, values);
        }
    }

    public void updatePub(Pub pub) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);

        ContentValues values = new ContentValues();
        values.put(PUB_ID, pub.getId());
        if (pub.getPubName() != null) values.put(PUB_NAME, pub.getPubName());
        if (pub.getLatLng() != null) values.put(LATITUDE, pub.getLatLng().latitude);
        if (pub.getLatLng() != null) values.put(LONGITUDE, pub.getLatLng().longitude);
        if (pub.getSize() != null) values.put(SIZE, pub.getSize());
        if (pub.getPrices() != null) values.put(PRICES, pub.getPrices());
        if (pub.getRating() != null) values.put(RATING, pub.getRating());
        if (pub.getOpeningTimes() != null) values.put(OPENING_TIMES, pub.getOpeningTimes());
        if (pub.getOwnerId() != null) values.put(OWNER, pub.getOwnerId());

        String selection = PUB_ID + " =?";
        String[] selectionArgs = {String.valueOf(pub.getId())};

        db.update(TABLE_PUBS, values, selection, selectionArgs);

        updateLists(pub);
    }

    public void updatePubOwner(long event_id, long owner_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);
        ContentValues values = new ContentValues();
        values.put(OWNER, owner_id);

        String selection = PUB_ID + " =?";
        String[] selectionArgs = {String.valueOf(event_id)};

        db.update(TABLE_PUBS, values, selection, selectionArgs);
    }

    public void updateLists(Pub pub) {
        deleteLists(pub.getId());

        addTopPersons(pub);
        addPubEvents(pub);
        addPubImages(pub);
    }

    public void deleteLists(long pub_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);

        String selection = PUB_ID + " =?";
        String[] selectionArgs = {String.valueOf(pub_id)};

        db.delete(TABLE_TOP_PERSONS, selection, selectionArgs);
        db.delete(TABLE_PUB_EVENTS, selection, selectionArgs);
        db.delete(TABLE_PUB_IMAGES, selection, selectionArgs);
    }

    public void deletePub(long pub_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);

        String selection = PUB_ID + " =?";
        String[] selectionArgs = {String.valueOf(pub_id)};

        db.delete(TABLE_PUBS, selection, selectionArgs);

        deleteLists(pub_id);
    }

    public Pub getListlessPub (long pub_id) throws DatabaseException {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);

        Pub pub = new Pub(pub_id);

        String query = "SELECT * FROM " +
                TABLE_PUBS + " WHERE " +
                PUB_ID + " = " + pub_id;

        Cursor c = db.rawQuery(query, null);

        if (c != null && c.moveToFirst()) {

            pub.setPubName(c.getString(c.getColumnIndex(PUB_NAME)));
            pub.setLatLng(new LatLng(c.getDouble(c.getColumnIndex(LATITUDE)),
                    c.getDouble(c.getColumnIndex(LONGITUDE))));
            pub.setSize(c.getInt(c.getColumnIndex(SIZE)));
            pub.setPrices(c.getInt(c.getColumnIndex(PRICES)));
            pub.setRating(c.getDouble(c.getColumnIndex(RATING)));
            pub.setOpeningTimes(c.getString(c.getColumnIndex(OPENING_TIMES)));
            pub.setOwnerId(c.getLong(c.getColumnIndex(OWNER)));

            c.close();
        } else {
            Log.e(TAG, "Can't find pub with id " + pub_id);
            throw new DatabaseException("Can't find pub with id " + pub_id + ". Cursor is null or database empty");
        }

        return pub;
    }

    public Pub getPub(long pub_id) throws DatabaseException {
        Pub pub = getListlessPub(pub_id);

        pub.setTopsListIds(getTopPersonIds(pub_id));
        pub.setEventsListIds(getPubEventIds(pub_id));
        pub.setImages(getPubImages(pub_id));

        return pub;
    }

    public List<Pub> getAllPubs() {
        List<Pub> pubsList = new ArrayList<>();
        SQLiteDatabase db =DatabaseManager.getInstance().openDatabase();
        String query = "SELECT " + PUB_ID + " FROM " +
                TABLE_PUBS;
        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                int pub_id = c.getInt(c.getColumnIndex(PUB_ID));
                Pub pub = null;
                try {
                    pub = getListlessPub(pub_id);
                    pubsList.add(pub);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }

            } while (c.moveToNext());
            c.close();

        } else {
            Log.e(TAG, "Can't find Pubs");
            String msg = c != null ? "Can't find pubs. cursor size is " + c.getCount() : "\"Can't find pubs . Cursor is null or database empty\"";
            try {
                throw new DatabaseException(msg);
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
        c.close();
        return pubsList;
    }

    public ArrayList<Long> getTopPersonIds(long pub_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_TOP_PERSONS + " WHERE " +
                PUB_ID + " = " + pub_id +
                " ORDER BY " + ITERATOR + " ASC";

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(PERSON_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.v(TAG, "pub id " + pub_id + " getTopPersonIds: Cursor is null or database empty");
        }

        return list;
    }

    public ArrayList<Long> getPubEventIds (long pub_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);

        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_PUB_EVENTS + " WHERE " +
                PUB_ID + " = " + pub_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(EVENT_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.v(TAG, "pub id " + pub_id + " getPubEventIds: Cursor is null or database empty");
        }

        return list;
    }

    public ArrayList<Bitmap> getPubImages (long pub_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        onCreate(db);

        ArrayList<Bitmap> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_PUB_IMAGES + " WHERE " +
                PUB_ID + " = " + pub_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(bytesToBitmap(c.getBlob(c.getColumnIndex(IMAGE))));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.v(TAG, "pub id " + pub_id + " getPubImages: Cursor is null or database empty");
        }

        return list;
    }
}
