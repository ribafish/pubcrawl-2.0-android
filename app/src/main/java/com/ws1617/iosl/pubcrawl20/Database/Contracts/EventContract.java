package com.ws1617.iosl.pubcrawl20.Database.Contracts;

/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class EventContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private EventContract() {
    }

    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_EVENT_TIMESLOTS = "event_timeslots";
    public static final String TABLE_EVENT_PARTICIPANTS = "event_participants";
    public static final String TABLE_EVENT_PUBS = "event_pubs";

    public static final String EVENT_ID = "event_id";
    public static final String EVENT_NAME = "event_name";
    public static final String DATE = "date";
    public static final String DESCRIPTION = "description";
    public static final String TRACKED = "tracked";
    public static final String OWNER = "owner";
    public static final String IMAGE = "image";

    public static final String PUB_ID = "pub_id";
    public static final String PARTICIPANT_ID = "participant_id";
    public static final String START_TIME = "start";
    public static final String END_TIME = "end";

    public static final String ID = "_id";

    public static final String CREATE_EVENTS_TABLE = "CREATE TABLE " +
            TABLE_EVENTS + "(" +
            EVENT_ID + " INTEGER PRIMARY KEY," +
            EVENT_NAME + " TEXT," +
            DATE + " INTEGER," +            // epoch time in ms
            DESCRIPTION + " TEXT," +
            TRACKED + " INTEGER," +
            OWNER + " INTEGER," +
            IMAGE + " BLOB" +
            ")";

    public static final String CREATE_TIMESLOTS_TABLE = "CREATE TABLE " +
            TABLE_EVENT_TIMESLOTS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
            EVENT_ID + " INTEGER," +
            PUB_ID + " INTEGER," + 
            START_TIME + " INTEGER," +      // epoch time in ms
            END_TIME + " INTEGER" +         // epoch time in ms
            ")";

    public static final String CREATE_PARTICIPANTS_TABLE = "CREATE TABLE " +
            TABLE_EVENT_PARTICIPANTS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            EVENT_ID + " INTEGER," +
            PARTICIPANT_ID + " INTEGER" +
            ")";

    public static final String CREATE_PUBS_TABLE = "CREATE TABLE " +
            TABLE_EVENT_PUBS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            EVENT_ID + " INTEGER," +
            PUB_ID + " INTEGER" +
            ")";
}
