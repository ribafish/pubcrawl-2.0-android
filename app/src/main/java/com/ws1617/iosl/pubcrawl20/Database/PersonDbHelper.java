package com.ws1617.iosl.pubcrawl20.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import com.ws1617.iosl.pubcrawl20.DataModels.Person;

import java.util.ArrayList;
import java.util.List;

import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.CREATE_PERSONS_TABLE;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.CREATE_PERSON_EVENTS_TABLE;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.CREATE_PERSON_FRIENDS_TABLE;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.CREATE_PERSON_OWNED_EVENTS_TABLE;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.CREATE_PERSON_OWNED_PUBS_TABLE;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.CREATE_PERSON_PUBS_FAVOURITE_TABLE;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.DESCRIPTION;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.EMAIL;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.EVENT_ID;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.FRIEND_ID;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.IMAGE;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.PERSON_ID;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.PUB_ID;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.TABLE_PERSONS;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.TABLE_PERSON_EVENTS;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.TABLE_PERSON_FAVOURITE_PUBS;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.TABLE_PERSON_FRIENDS;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.TABLE_PERSON_OWNED_EVENTS;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.TABLE_PERSON_OWNED_PUBS;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.USERNAME;
import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.bitmapToBytes;
import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.bytesToBitmap;

/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class PersonDbHelper {
    private static final String TAG = "PersonDbHelper";

    public static void onCreate(SQLiteDatabase db) {
        // create person tables
        db.execSQL(CREATE_PERSONS_TABLE);
        db.execSQL(CREATE_PERSON_EVENTS_TABLE);
        db.execSQL(CREATE_PERSON_FRIENDS_TABLE);
        db.execSQL(CREATE_PERSON_PUBS_FAVOURITE_TABLE);
        db.execSQL(CREATE_PERSON_OWNED_EVENTS_TABLE);
        db.execSQL(CREATE_PERSON_OWNED_PUBS_TABLE);

    }

    public static void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // drop older person tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON_FAVOURITE_PUBS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON_OWNED_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON_OWNED_PUBS);

        onCreate(db);
    }

    public static void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addPerson(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        long row_id;

        // add to person table
        ContentValues values = person.getContentValues();

        row_id = db.insert(TABLE_PERSONS, null, values);
        if (row_id == -1) { // Error - already exists -> update the person
            updatePerson(person);
        } else {
            addEventIds(person);
            addFriendIds(person);
            addFavouritePubIds(person);
            addOwnedEventIds(person);
            addOwnedPubIds(person);
        }
    }

    public void addEventIds(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values;
        for (long event_id : person.getEventIds()) {
            values = new ContentValues();
            values.put(PERSON_ID, person.getId());
            values.put(EVENT_ID, event_id);
            db.insert(TABLE_PERSON_EVENTS, null, values);
        }
    }

    public void addFriendIds(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values;
        for (long event_id : person.getFriendIds()) {
            values = new ContentValues();
            values.put(PERSON_ID, person.getId());
            values.put(FRIEND_ID, event_id);
            db.insert(TABLE_PERSON_FRIENDS, null, values);
        }
    }

    public void addFavouritePubIds(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values;
        for (long event_id : person.getFavouritePubIds()) {
            values = new ContentValues();
            values.put(PERSON_ID, person.getId());
            values.put(PUB_ID, event_id);
            db.insert(TABLE_PERSON_FAVOURITE_PUBS, null, values);
        }
    }

    public void addOwnedEventIds(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values;
        for (long event_id : person.getOwnedEventIds()) {
            values = new ContentValues();
            values.put(PERSON_ID, person.getId());
            values.put(EVENT_ID, event_id);
            db.insert(TABLE_PERSON_OWNED_EVENTS, null, values);
        }
    }

    public void addOwnedPubIds(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values;
        for (long pub_id : person.getOwnedPubIds()) {
            values = new ContentValues();
            values.put(PERSON_ID, person.getId());
            values.put(PUB_ID, pub_id);
            db.insert(TABLE_PERSON_OWNED_PUBS, null, values);
        }
    }

    public void updatePerson(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(PERSON_ID, person.getId());
        values.put(DESCRIPTION, person.getDescription());
        values.put(EMAIL, person.getMail());
        values.put(IMAGE, bitmapToBytes(person.getImage()));
        values.put(USERNAME, person.getName());

        String selection = PERSON_ID + " =?";
        String[] selectionArgs = {String.valueOf(person.getId())};

        db.update(TABLE_PERSONS, values, selection, selectionArgs);
        updateLists(person);
    }

    public void updateLists(Person person) {
        deleteLists(person);

        addEventIds(person);
        addFriendIds(person);
        addFavouritePubIds(person);
        addOwnedEventIds(person);
        addOwnedPubIds(person);
    }

    public void deleteLists(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        String selection = PERSON_ID + " =?";
        String[] selectionArgs = {String.valueOf(person.getId())};

        db.delete(TABLE_PERSON_EVENTS, selection, selectionArgs);
        db.delete(TABLE_PERSON_FRIENDS, selection, selectionArgs);
        db.delete(TABLE_PERSON_FAVOURITE_PUBS, selection, selectionArgs);
        db.delete(TABLE_PERSON_OWNED_EVENTS, selection, selectionArgs);
        db.delete(TABLE_PERSON_OWNED_PUBS, selection, selectionArgs);
    }

    public void deletePerson(Person person) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        String selection = PERSON_ID + " =?";
        String[] selectionArgs = {String.valueOf(person.getId())};

        db.delete(TABLE_PERSONS, selection, selectionArgs);
        deleteLists(person);
    }

    public Person getPerson(long person_id) throws DatabaseException {
        Person person = getListlessPerson(person_id);

        person.setEventIds(getEventIds(person_id));
        person.setFriendIds(getFriendIds(person_id));
        person.setFavouritePubIds(getFavouritePubIds(person_id));
        person.setOwnedEventIds(getOwnedEventIds(person_id));
        person.setOwnedPubIds(getOwnedPubIds(person_id));
        return person;
    }

    public Person getListlessPerson(long person_id) throws DatabaseException {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Person person = new Person(person_id);

        String query = "SELECT * FROM " +
                TABLE_PERSONS + " WHERE " +
                PERSON_ID + " = " + person_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {

            person.setDescription(c.getString(c.getColumnIndex(DESCRIPTION)));
            person.setEmail(c.getString(c.getColumnIndex(EMAIL)));
            Bitmap image = bytesToBitmap(c.getBlob(c.getColumnIndex(IMAGE)));
            if (image != null) person.setImage(image);
            person.setName(c.getString(c.getColumnIndex(USERNAME)));

            c.close();
        } else {
            Log.e(TAG, "Can't find person with id " + person_id);
            throw new DatabaseException("Can't find person with id " + person_id + ". Cursor is null or database empty");
        }
        return person;
    }

    public ArrayList<Long> getEventIds(long person_id) throws DatabaseException {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_PERSON_EVENTS + " WHERE " +
                PERSON_ID + " = " + person_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(EVENT_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.v(TAG, "person id " + person_id + " getEventIds: Cursor is null or database empty");
        }

        return list;
    }

    public ArrayList<Long> getFriendIds(long person_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_PERSON_FRIENDS + " WHERE " +
                PERSON_ID + " = " + person_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(FRIEND_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.v(TAG, "person id " + person_id + " getFriendIds: Cursor is null or database empty");
        }

        return list;
    }

    public ArrayList<Long> getFavouritePubIds(long person_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_PERSON_FAVOURITE_PUBS + " WHERE " +
                PERSON_ID + " = " + person_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(PUB_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.v(TAG, "person id " + person_id + " getFavouritePubIds: Cursor is null or database empty");
        }

        return list;
    }

    public ArrayList<Long> getOwnedEventIds(long person_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_PERSON_OWNED_EVENTS + " WHERE " +
                PERSON_ID + " = " + person_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(EVENT_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.v(TAG, "person id " + person_id + " getOwnedEventIds: Cursor is null or database empty");
        }

        return list;
    }

    public ArrayList<Long> getOwnedPubIds(long person_id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Long> list = new ArrayList<>();

        String query = "SELECT * FROM " +
                TABLE_PERSON_OWNED_PUBS + " WHERE " +
                PERSON_ID + " = " + person_id;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                list.add(c.getLong(c.getColumnIndex(PUB_ID)));
            } while (c.moveToNext());
            c.close();
        } else {
            Log.v(TAG, "person id " + person_id + " getOwnedPubIds: Cursor is null or database empty");
        }

        return list;
    }

    public List<Person> getAllPersons() {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ArrayList<Person> list = new ArrayList<>();

        String query = "SELECT " + PERSON_ID + " FROM " +
                TABLE_PERSONS;

        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            do {
                int person_id = c.getInt(c.getColumnIndex(PERSON_ID));
                Person person = null;
                try {
                    person = getPerson(person_id);
                    list.add(person);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }


            } while (c.moveToNext());
            c.close();
        } else {
            Log.w(TAG, "getFavouritePubIds: Cursor is null or database empty");
        }


        return list;
    }
}
