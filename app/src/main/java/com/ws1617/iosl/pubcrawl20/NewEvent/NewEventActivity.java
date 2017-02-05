package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.NewEventPagerAdapter;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.List;

public class NewEventActivity extends AppCompatActivity  {

    FloatingActionButton mCreateEventBtn;
    NewEventPagerAdapter mFragmentPagerAdapter;
    private Event mEvent;
    private List<Fragment> fragmentsList;
     final static String EVENT_TAG = "EVENT_TAG";


    public void initFragmentList() {

        fragmentsList = new ArrayList<>();
        fragmentsList.add(new NewEventGeneralFragment());
        fragmentsList.add(new NewEventRouteFragment());
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

    InsertNewEventListener insertNewEventListener;

    public void onCollectDataClicked() {
        Event event = new Event();
        mEvent = ((NewEventGeneralFragment) fragmentsList.get(0)).updateGeneralInfo(event);
        mEvent = ((NewEventRouteFragment) fragmentsList.get(1)).updatePubListInfo(event);


        final Event mEvent = event;
        DatabaseHelper.addEvent(this,mEvent, new EventCreation(){
            @Override
            public void onSuccess() {
                // Add to local DB or refresh it
                EventDbHelper eventDbHelper = new EventDbHelper();
                eventDbHelper.addEvent(mEvent);

                //onSuccess adding to local DB
                ShareEventDialog shareEventDialog = new ShareEventDialog();
                shareEventDialog.show(getFragmentManager(),"shareEventDialog");
            }

            @Override
            public void onFail() {
                // show error
                Toast.makeText(getApplicationContext(), "Error while creating the Event .. ", Toast.LENGTH_SHORT).show();
            }
        });



    }

    public interface EventCreation{
        public void onSuccess();
        public void onFail();
    }


    interface InsertNewEventListener{
        void onSuccessfulInsert();
        void onFaildInsert();
    }
}
