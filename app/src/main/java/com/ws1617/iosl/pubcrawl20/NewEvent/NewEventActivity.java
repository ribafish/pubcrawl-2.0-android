package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ws1617.iosl.pubcrawl20.Models.Event;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.List;

public class NewEventActivity extends AppCompatActivity  {

    FloatingActionButton mCreateEventBtn;
    NewEventPagerAdapter mFragmentPagerAdapter;
    private  Event mEvent;
    private List<Fragment> fragmentsList;
     final static String EVENT_TAG = "EVENT_TAG";


    public void initFragmentList() {

        fragmentsList = new ArrayList<>();
        fragmentsList.add(new NewEventGeneralFragment());
        fragmentsList.add(new NewEventRouteFragment());
        fragmentsList.add(new NewEventShareFragment());

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        if (savedInstanceState == null)
            mEvent = new Event();
        else {
            mEvent = (Event) savedInstanceState.getSerializable(EVENT_TAG);
        }
        initFragmentList();
        initView();

    }


    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
         mFragmentPagerAdapter = new NewEventPagerAdapter
                (getSupportFragmentManager(), getApplicationContext(),
                        fragmentsList);

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.new_event_pager);
        viewPager.setAdapter(mFragmentPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.new_event_tabs);
        tabLayout.setupWithViewPager(viewPager);

        mCreateEventBtn = (FloatingActionButton) findViewById(R.id.new_event_create_new);
        mCreateEventBtn.setOnClickListener(newEventClickListener);
    }

    View.OnClickListener newEventClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           onCollectDataClicked();
        }
    };


    public void onCollectDataClicked() {
        Event mEvent = new Event();
        mEvent = ((NewEventGeneralFragment) fragmentsList.get(0)).updateGeneralInfo(mEvent);
        mEvent = ((NewEventRouteFragment) fragmentsList.get(1)).updatePubListInfo(mEvent);

    }
}
