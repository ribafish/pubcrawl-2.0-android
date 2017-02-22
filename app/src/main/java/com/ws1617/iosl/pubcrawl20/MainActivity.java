package com.ws1617.iosl.pubcrawl20;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.barcode.Barcode;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.resetDbTask;
import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.Details.PubDetailsActivity;
import com.ws1617.iosl.pubcrawl20.NewEvent.NewEventActivity;
import com.ws1617.iosl.pubcrawl20.ScanQR.BarcodeCaptureActivity;
import com.ws1617.iosl.pubcrawl20.ScanQR.QRScannerDialog;
import com.ws1617.iosl.pubcrawl20.Search.SearchActivity;

import java.util.List;

import static com.ws1617.iosl.pubcrawl20.NewEvent.ShareEventDialog.mBarcodeData;

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
    private ViewPager mViewPager;
    private Context context;
    private static final int RC_BARCODE_CAPTURE = 9001;
    private Handler handler;
    private Runnable updateDatabaseRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                new resetDbTask(context, resetDbTask.ALL_DB).execute();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                long delay = Long.parseLong(pref.getString("sync_frequency", "180")) * 60 * 1000;
                if (delay > 0)
                    handler.postDelayed(updateDatabaseRunnable, System.currentTimeMillis() + delay);
            }
        }
    };


    /**
     * The {@link ViewPager} that will host the section contents.
     */

    QRScannerDialog qrScannerDialog = new QRScannerDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

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

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), getApplicationContext());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mainFragmentPagerAdapter);
        mViewPager.setOffscreenPageLimit(mainFragmentPagerAdapter.getCount());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fabNewEvent = (FloatingActionButton) findViewById(R.id.main_fab_menu_create_event);
        fabNewEvent.setImageDrawable(AppCompatDrawableManager.get().getDrawable(this, R.drawable.ic_action_beer_plus_white));

        fabNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewEventActivity.class);
                context.startActivity(intent);
            }
        });

        handler = new Handler();

        setTab(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        long delay = Long.parseLong(pref.getString("sync_frequency", "180")) * 60 * 1000;
        if (delay > 0)
            handler.postDelayed(updateDatabaseRunnable, System.currentTimeMillis() + delay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateDatabaseRunnable);
    }

    @Override
    public void onBackPressed() {
        long tm = System.currentTimeMillis();
        if (tm - _mBackTime < 60000)
            finish();
        _mBackTime = tm;
        Toast.makeText(this, R.string.App_Exit, Toast.LENGTH_SHORT).show();
    }

    public void startQrScan() {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = new Intent(context, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, sharedPrefs.getBoolean("barcode_focus", false));
        intent.putExtra(BarcodeCaptureActivity.UseFlash, sharedPrefs.getBoolean("barcode_flash", false));
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                Log.d("Barcode-reader", "result was received in main activity correctly");
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    if (barcode.displayValue.isEmpty())
                        Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
                    else {
                        if (barcode.displayValue.contains(mBarcodeData)) {
                            prepareSharedEvent(barcode);
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

    private void prepareSharedEvent(final Barcode barcode) {
        try {
            List<Event> eventsList = new EventDbHelper().getAllEvents();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        Log.d("scanqr", "onResetDone");
        //extract event name from the row data
        String eventName = barcode.displayValue.substring(mBarcodeData.length(), barcode.displayValue.length());

        // get event with the same name, get its id ..
        Event event = null;
        try {
            event = new EventDbHelper().getEvent(eventName);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        //TODO start activity if id is founded
        if (event != null) {
            //TODO hide the dialog
            qrScannerDialog.initNotFoundLayout(false);
            Long id = event.getId();
            Intent intent = new Intent(context, EventDetailsActivity.class);
            // local db should be updated before starting the activity, event might be on server but not on local db
            intent.putExtra("id", id);
            startActivity(intent);
        } else {
            //TODO show it on the dialog
            qrScannerDialog.initNotFoundLayout(true);

        }
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
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
        if (mLastLocation != null)
            return mLastLocation;
        throw new NullPointerException();
    }

    public void setTab(int num) {
        mViewPager.setCurrentItem(num);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intentSettings = new Intent(context, SettingsActivity.class);
                context.startActivity(intentSettings);
                return true;

            case R.id.action_refresh:
                new resetDbTask(this, resetDbTask.ALL_DB).execute();
                return true;
            case R.id.action_search:
                Intent intentSearch = new Intent(context, SearchActivity.class);
                context.startActivity(intentSearch);
                return true;
            case  R.id.action_qrcode:
                startQrScan();
                return true;
            case R.id.action_new_event:
                Intent intent = new Intent(context, NewEventActivity.class);
                context.startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
