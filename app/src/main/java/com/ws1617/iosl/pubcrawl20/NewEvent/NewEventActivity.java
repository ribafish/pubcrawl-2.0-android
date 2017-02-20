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
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.NewEventPagerAdapter;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.List;

public class NewEventActivity extends AppCompatActivity {

    FloatingActionButton mCreateEventBtn;
    NewEventPagerAdapter mFragmentPagerAdapter;
    private List<Fragment> fragmentsList;
    final static String TAG = "EVENT_TAG";

    Event oldEvent;

    public void initFragmentList() {
        fragmentsList = new ArrayList<>();
        fragmentsList.add(new NewEventGeneralFragment());
        fragmentsList.add(new NewEventRouteFragment());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        initFragmentList();
        initView();


        long id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            //Edit mode
            showOldEvent(id);
        }

    }

    private void showOldEvent(long id) {
        try {
            oldEvent = new EventDbHelper().getEvent(id);
            ((NewEventGeneralFragment) fragmentsList.get(0)).setOldEvent(oldEvent);
            ((NewEventRouteFragment) fragmentsList.get(1)).setOldEvent(oldEvent);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

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
        final Event event = new Event();
        ((NewEventGeneralFragment) fragmentsList.get(0)).collectGeneralInfo(event);
        ((NewEventRouteFragment) fragmentsList.get(1)).collectPubListInfo(event);


        new DatabaseHelper().addEventOwner(getApplicationContext(), 19, new SetOwner() {
            @Override
            public void onSuccess() {
                DatabaseHelper.resetEventsDatabase(getApplicationContext());
            }

            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(), "Error while creating the Event .. ", Toast.LENGTH_SHORT).show();
            }
        });


      /*    if (!checkSatisfyMinReq(event))
            return;
        else {
            final ShareEventDialog shareEventDialog = new ShareEventDialog();
            shareEventDialog.show(getSupportFragmentManager(), "shareEventDialog");

            DatabaseHelper.addEvent(this, event, new EventCreation() {
                @Override
                public void onSuccess() {
                    //refresh the whole DB

                    new DatabaseHelper().addEventOwner(getApplicationContext(), event.getId(), new SetOwner() {
                        @Override
                        public void onSuccess() {
                            DatabaseHelper.resetEventsDatabase(getApplicationContext());
                            shareEventDialog.initQRCodeView(event.getEventName());
                        }

                        @Override
                        public void onFail() {
                            Toast.makeText(getApplicationContext(), "Error while creating the Event .. ", Toast.LENGTH_SHORT).show();
                            shareEventDialog.dismiss();
                        }
                    });


                }

                @Override
                public void onFail() {
                    // show error
                    Toast.makeText(getApplicationContext(), "Error while creating the Event .. ", Toast.LENGTH_SHORT).show();
                    shareEventDialog.dismiss();
                }
            });
        }*/
    }

    private boolean checkSatisfyMinReq(Event event) {
        if (event.getEventName() == null || event.getEventName().trim().length() == 0) {
            Toast.makeText(this, "You cant create event without name!!!  ", Toast.LENGTH_SHORT).show();
            return false;
        } else if (event.getPubIds().size() < 2) {
            Toast.makeText(this, "You should at least provide two pubs", Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    public interface EventCreation {
        void onSuccess();

        void onFail();
    }


    public interface SetOwner {
        void onSuccess();

        void onFail();
    }

}
