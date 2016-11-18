package com.ws1617.iosl.pubcrawl20;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;


import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.ws1617.iosl.pubcrawl20.NewEvent.NewEventActivity;
import com.ws1617.iosl.pubcrawl20.ScanQR.ScanQrActivity;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private MainFragmentPagerAdapter mainFragmentPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

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

        final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        FloatingActionButton fabSettings = (FloatingActionButton) findViewById(R.id.main_fab_menu_settings);
        FloatingActionButton fabNewEvent = (FloatingActionButton) findViewById(R.id.main_fab_menu_create_event);
        FloatingActionButton fabScanQR = (FloatingActionButton) findViewById(R.id.main_fab_menu_qr_scan);
        FloatingActionButton fabSearch = (FloatingActionButton) findViewById(R.id.main_fab_menu_search);

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

        fabScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ScanQrActivity.class);
                context.startActivity(intent);
                fabMenu.close(true);
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "TODO", Snackbar.LENGTH_SHORT).show();
                fabMenu.close(true);
            }
        });

    }

}
