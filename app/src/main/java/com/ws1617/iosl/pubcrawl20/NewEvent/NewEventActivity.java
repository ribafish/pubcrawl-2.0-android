package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ws1617.iosl.pubcrawl20.R;

public class NewEventActivity extends AppCompatActivity {

    FloatingActionButton mCreateEventBtn;
    NewEventController eventController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        eventController = new NewEventController();

        if (savedInstanceState == null)
            eventController.initNewEvent();

        else {
            Event oldEvent = (Event) savedInstanceState.getSerializable(eventController.EVENT_TAG);
            eventController.restoreOldEvent(oldEvent);
        }
        initView();

    }


    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        NewEventPagerAdapter fragmentPagerAdapter = new NewEventPagerAdapter
                (getSupportFragmentManager(), getApplicationContext(),
                        eventController.getFragmentList());

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.new_event_pager);
        viewPager.setAdapter(fragmentPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.new_event_tabs);
        tabLayout.setupWithViewPager(viewPager);

        mCreateEventBtn = (FloatingActionButton) findViewById(R.id.new_event_create_new);
        mCreateEventBtn.setOnClickListener(newEventClickListener);
    }

    View.OnClickListener newEventClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            eventController.collectEventData();
        }
    };
}
