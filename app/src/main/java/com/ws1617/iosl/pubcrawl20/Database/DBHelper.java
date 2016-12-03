package com.ws1617.iosl.pubcrawl20.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ws1617.iosl.pubcrawl20.Database.Models.User;
import com.ws1617.iosl.pubcrawl20.Database.Repo.UserRepo;

/**
 * Created by Icke-Hier on 03.12.2016.
 */

public class DBHelper extends SQLiteOpenHelper implements DatabaseDefI {

  public DBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(UserRepo.createTable());
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int old, int current) {
  }
}
