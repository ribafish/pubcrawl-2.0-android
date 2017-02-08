package com.ws1617.iosl.pubcrawl20;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import com.ws1617.iosl.pubcrawl20.Database.DatabaseManager;
import com.ws1617.iosl.pubcrawl20.Database.SQLDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Icke-Hier on 18.01.2017.
 */

public class App extends Application {

  private static final String TAG = "App";
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
