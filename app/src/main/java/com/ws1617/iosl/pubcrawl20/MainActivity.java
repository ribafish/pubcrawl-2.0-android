package com.ws1617.iosl.pubcrawl20;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.barcode.Barcode;
import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PubDetailsActivity;
import com.ws1617.iosl.pubcrawl20.NewEvent.NewEventActivity;
import com.ws1617.iosl.pubcrawl20.ScanQR.BarcodeCaptureActivity;
import com.ws1617.iosl.pubcrawl20.Search.SearchActivity;

/**
 * Created by Gasper Kojek on 9. 11. 2016.
 * Github: https://github.com/ribafish/
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private MainFragmentPagerAdapter mainFragmentPagerAdapter;
    private GoogleApiClient mGoogleApiClient;
    private static Location mLastLocation;
    private long _mBackTime = 0L;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Context context;
    private static final int RC_BARCODE_CAPTURE = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        /**
         * Check for Location permissions
         */
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);


       if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
               && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Create an instance of GoogleAPIClient.
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                  .addConnectionCallbacks(this)
                  .addOnConnectionFailedListener(this)
                  .addApi(LocationServices.API)
                  .build();
            }
       }


        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), getApplicationContext());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mainFragmentPagerAdapter);
        mViewPager.setOffscreenPageLimit(mainFragmentPagerAdapter.getCount());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        /**Set images for Floating Buttons in Fragment to support Floating Button Vector Drawables for pre Lollipop Devices
         * see https://github.com/Clans/FloatingActionButton/issues/273 **/
        final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        FloatingActionButton fabSettings = (FloatingActionButton) findViewById(R.id.main_fab_menu_settings);
        fabSettings.setImageDrawable(AppCompatDrawableManager.get().getDrawable(this, R.drawable.ic_settings));
        FloatingActionButton fabNewEvent = (FloatingActionButton) findViewById(R.id.main_fab_menu_create_event);
        fabNewEvent.setImageDrawable(AppCompatDrawableManager.get().getDrawable(this, R.drawable.ic_action_beer_plus_white));
        FloatingActionButton fabScanQR = (FloatingActionButton) findViewById(R.id.main_fab_menu_qr_scan);
        fabScanQR.setImageDrawable(AppCompatDrawableManager.get().getDrawable(this, R.drawable.qrcode));
        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.main_fab_menu_search);
        fabSearch.setImageDrawable(AppCompatDrawableManager.get().getDrawable(this, R.drawable.ic_search_24dp));

        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SettingsActivity.class);
                context.startActivity(intent);
                fabMenu.close(false);
            }
        });

        fabNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewEventActivity.class);
                context.startActivity(intent);
                fabMenu.close(false);
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchActivity.class);
                context.startActivity(intent);
                fabMenu.close(false);
            }
        });

        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        fabScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, sharedPrefs.getBoolean("barcode_focus", false));
                intent.putExtra(BarcodeCaptureActivity.UseFlash, sharedPrefs.getBoolean("barcode_flash", false));
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                fabMenu.close(true);
            }
        });

    }

    @Override
    public void onBackPressed() {
      long tm = System.currentTimeMillis();
      if (tm - _mBackTime < 60000)
        finish();
      _mBackTime = tm;
      Toast.makeText(this, R.string.App_Exit, Toast.LENGTH_SHORT).show();
      return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    if (barcode.displayValue.isEmpty())
                        Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
                    else {
                        if (barcode.displayValue.contains("/event/")) {
                            Intent intent = new Intent(context, EventDetailsActivity.class);
                            intent.putExtra("name", "Test Event");
                            intent.putExtra("id", (long) 14);
                            startActivity(intent);
                        }
                        if (barcode.displayValue.contains("/pub/")) {
                            Intent intent = new Intent(context, PubDetailsActivity.class);
                            intent.putExtra("name", "Test Pub");
                            intent.putExtra("id", (long) 9);
                            startActivity(intent);
                        }
                    }
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "complete fail", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        if(mGoogleApiClient != null)mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null)  mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static Location getLocation() throws NullPointerException {
        if(mLastLocation != null)
            return mLastLocation;
        throw new NullPointerException();
    }

    public void setTab(int num) {
        mViewPager.setCurrentItem(num);
    }
}
