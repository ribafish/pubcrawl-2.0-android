package com.ws1617.iosl.pubcrawl20.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Peter on 24.01.2017.
 */

public class SQLDatabase extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "PubCrawl20.db";

  public SQLDatabase(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    PersonDbHelper.onCreate(sqLiteDatabase);
    EventDbHelper.onCreate(sqLiteDatabase);
    PubDbHelper.onCreate(sqLiteDatabase);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    PersonDbHelper.onUpgrade(sqLiteDatabase, i, i1);
    EventDbHelper.onUpgrade(sqLiteDatabase, i, i1);
    PubDbHelper.onUpgrade(sqLiteDatabase, i, i1);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)  {
    PersonDbHelper.onUpgrade(db, oldVersion, newVersion);
    EventDbHelper.onUpgrade(db, oldVersion, newVersion);
    PubDbHelper.onUpgrade(db, oldVersion, newVersion);
  }
}
