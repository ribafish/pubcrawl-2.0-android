package com.ws1617.iosl.pubcrawl20;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.ws1617.iosl.pubcrawl20.Database.DatabaseManager;
import com.ws1617.iosl.pubcrawl20.Database.SQLDatabase;

/**
 * Created by Icke-Hier on 18.01.2017.
 */

public class App extends Application {

  private static Context appContext;

  @Override
  public void onCreate() {
    super.onCreate();
    appContext = this;
    SQLiteOpenHelper dbHelper = new SQLDatabase(this);
    DatabaseManager.initializeInstance(dbHelper);
  }

  public static Context getAppContext() {
    return appContext;
  }
}
