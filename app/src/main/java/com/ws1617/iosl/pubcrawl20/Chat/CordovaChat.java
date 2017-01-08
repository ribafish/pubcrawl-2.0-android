package com.ws1617.iosl.pubcrawl20.chat;

import android.os.Bundle;

import org.apache.cordova.CordovaActivity;

/**
 * Created by Icke-Hier on 07.01.2017.
 */

public class CordovaChat extends CordovaActivity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // enable Cordova apps to be started in the background
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
      moveTaskToBack(true);
    }

    // Set by <content src="index.html" /> in config.xml
    loadUrl(launchUrl);
  }
}
