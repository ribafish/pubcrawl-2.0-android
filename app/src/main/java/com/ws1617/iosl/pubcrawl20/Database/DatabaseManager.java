package com.ws1617.iosl.pubcrawl20.Database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Peter on 17.01.2017.
 */

public class DatabaseManager {

  private Integer mOpenCounter = 0;

  private static DatabaseManager instance;
  private static SQLiteOpenHelper mDatabaseHelper;
  private SQLiteDatabase mDatabase;
  private boolean open = false;

  private DatabaseManager()   {}

  public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
    if (instance == null) {
      instance = new DatabaseManager();
      mDatabaseHelper = helper;
    }
  }

  public static synchronized DatabaseManager getInstance() {
    if (instance == null) {
      throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
        " is not initialized, call initializeInstance(..) method first.");
    }

    return instance;
  }

  public synchronized SQLiteDatabase openDatabase() {
    if(!open) {
      // Opening new database
      mDatabase = mDatabaseHelper.getWritableDatabase();
    }
    open = true;
    return mDatabase;
  }
}
