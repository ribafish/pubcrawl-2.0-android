package com.ws1617.iosl.pubcrawl20.Database.Contracts;

/**
 * Created by Gasper Kojek on 20. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class PersonContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private PersonContract() {}

    public static final String TABLE_PERSONS = "persons";
    public static final String TABLE_PERSON_EVENTS = "persons_events_list";
    public static final String TABLE_PERSON_FRIENDS = "persons_friends_list";
    public static final String TABLE_PERSON_FAVOURITE_PUBS = "persons_pubs_list";
    public static final String TABLE_PERSON_OWNED_EVENTS = "persons_owned_events_list";
    public static final String TABLE_PERSON_OWNED_PUBS = "persons_owned_pubs_list";

    public static final String PERSON_ID = "person_id";
    public static final String DESCRIPTION = "description";
    public static final String EMAIL = "email";
    public static final String IMAGE = "image";
    public static final String USERNAME = "username";

    public static final String EVENT_ID = "event_id";
    public static final String PUB_ID = "pub_id";
    public static final String FRIEND_ID = "friend_id";

    public static final String ID = "_id";

    public static final String CREATE_PERSONS_TABLE = "CREATE TABLE " +
            TABLE_PERSONS + "(" +
                    PERSON_ID + " INTEGER PRIMARY KEY," +
                    DESCRIPTION + " TEXT," +
                    EMAIL + " TEXT," +
                    IMAGE + " TEXT," +
                    USERNAME + " TEXT" +
            ")";

    public static final String CREATE_PERSON_EVENTS_TABLE = "CREATE TABLE " +
            TABLE_PERSON_EVENTS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PERSON_ID + " INTEGER," +
            EVENT_ID + " INTEGER" +
            ")";

    public static final String CREATE_PERSON_FRIENDS_TABLE = "CREATE TABLE " +
            TABLE_PERSON_FRIENDS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PERSON_ID + " INTEGER," +
            FRIEND_ID + " INTEGER" +
            ")";

    public static final String CREATE_PERSON_PUBS_FAVOURITE_TABLE = "CREATE TABLE " +
            TABLE_PERSON_FAVOURITE_PUBS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PERSON_ID + " INTEGER," +
            PUB_ID + " INTEGER," +
            ")";

    public static final String CREATE_PERSON_OWNED_EVENTS_TABLE = "CREATE TABLE " +
            TABLE_PERSON_OWNED_EVENTS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PERSON_ID + " INTEGER," +
            EVENT_ID + " INTEGER" +
            ")";

    public static final String CREATE_PERSON_OWNED_PUBS_TABLE = "CREATE TABLE " +
            TABLE_PERSON_OWNED_PUBS + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PERSON_ID + " INTEGER," +
            PUB_ID + " INTEGER," +
            ")";
}
