package com.ws1617.iosl.pubcrawl20.database.Contracts;

/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class PubContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private PubContract() {
    }

    public static final String TABLE_PUBS = "pubs";
    public static final String TABLE_TOP_PERSONS = "pub_top_persons";
    public static final String TABLE_PUB_EVENTS = "pub_events";
    public static final String TABLE_PUB_IMAGES = "pub_images";

    public static final String PUB_ID = "pub_id";
    public static final String PUB_NAME = "pub_name";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SIZE = "size";
    public static final String PRICES = "prices";
    public static final String RATING = "rating";
    public static final String OPENING_TIMES = "opening_times";
    public static final String OWNER = "owner";

    public static final String ID = "_id";
    public static final String ITERATOR = "iterator";
    public static final String PERSON_ID = "person_id";
    public static final String EVENT_ID = "event_id";
    public static final String IMAGE = "image";

    public static final String CREATE_PUBS_TABLE = "CREATE TABLE " +
            TABLE_PUBS + "(" +
            PUB_ID + " INTEGER PRIMARY KEY," +
            PUB_NAME + " TEXT," +
            LATITUDE + " REAL," +
            LONGITUDE + " REAL," +
            SIZE + " INTEGER," +
            PRICES + " INTEGER," +
            RATING + " REAL," +
            OPENING_TIMES + " TEXT," +
            OWNER + " INTEGER" +
            ")";

    public static final String CREATE_TOP_PERSONS_TABLE = "CREATE TABLE " +
            TABLE_TOP_PERSONS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PUB_ID + " INTEGER," +
            PERSON_ID + " INTEGER," +
            ITERATOR + " INTEGER" +
            ")";

    public static final String CREATE_PUB_EVENTS_TABLE = "CREATE TABLE " +
            TABLE_PUB_EVENTS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PUB_ID + " INTEGER," +
            EVENT_ID + " INTEGER" +
            ")";

    public static final String CREATE_PUB_IMAGES_TABLE = "CREATE TABLE " +
            TABLE_PUB_IMAGES + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PUB_ID + " INTEGER," +
            ITERATOR + " INTEGER," +
            IMAGE + " BLOB" +
            ")";
}