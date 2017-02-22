package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.MainActivity;
import com.ws1617.iosl.pubcrawl20.Database.resetDbTask;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.NewEventPagerAdapter;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.List;

public class NewEventActivity extends AppCompatActivity {

    FloatingActionButton mCreateEventBtn;
    NewEventPagerAdapter mFragmentPagerAdapter;
    private List<Fragment> fragmentsList;
    private Context context;
    final static String EVENT_TAG = "EVENT_TAG";

    Event oldEvent;
    VIEW_MODE currentMode = VIEW_MODE.ADD;
    ;

    enum VIEW_MODE {EDIT, ADD}

    public void initFragmentList() {
        fragmentsList = new ArrayList<>();
        fragmentsList.add(new NewEventGeneralFragment());
        fragmentsList.add(new NewEventRouteFragment());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_new_event);

        initFragmentList();


        long id = getIntent().getLongExtra("id", -1);
        if (id != -1) {
            //Edit mode
            showOldEvent(id);
        }
        initView();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentMode == VIEW_MODE.EDIT) {
            getMenuInflater().inflate(R.menu.event_ops_menu, menu);
            return true;
        } else
            return super.onCreateOptionsMenu(menu);
    }


    private void showOldEvent(long id) {
        try {
            oldEvent = new EventDbHelper().getEvent(id);
            currentMode = VIEW_MODE.EDIT;
            ((NewEventGeneralFragment) fragmentsList.get(0)).setOldEvent(oldEvent);
            ((NewEventRouteFragment) fragmentsList.get(1)).setOldEvent(oldEvent);
        } catch (DatabaseException e) {
            currentMode = VIEW_MODE.ADD;
            e.printStackTrace();
        }

    }


    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.done_event: {
                        Event event = onCollectDataClicked();
                        if (event != null) editEvent(event);
                        break;
                    }
                    case R.id.delete_event:{
                        deleteEvent();
                        break;
                    }
                }
                return true;
            }
        });


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
        if (currentMode == VIEW_MODE.EDIT) {
            mCreateEventBtn.setVisibility(View.GONE);
        } else {
            mCreateEventBtn.setVisibility(View.VISIBLE);
            mCreateEventBtn.setOnClickListener(newEventClickListener);
        }
    }

    View.OnClickListener newEventClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Event event = onCollectDataClicked();
            if (event != null)
                addEvent(event);
        }
    };


    public Event onCollectDataClicked() {
        final Event event = new Event();
        ((NewEventGeneralFragment) fragmentsList.get(0)).collectGeneralInfo(event);
        ((NewEventRouteFragment) fragmentsList.get(1)).collectPubListInfo(event);
        if (checkSatisfyMinReq(event))
            return event;
        else return null;
    }


    private void editEvent(final Event event) {
        final ShareEventDialog shareEventDialog = new ShareEventDialog();
        shareEventDialog.show(getSupportFragmentManager(), "shareEventDialog");

        event.setId(oldEvent.getId());
        DatabaseHelper.updateEvent(this, event, new EventCreation() {
            @Override
            public void onSuccess() {
                //refresh the whole DB
                new resetDbTask(getApplicationContext(), resetDbTask.ALL_DB).execute();
                shareEventDialog.initQRCodeView(event.getEventName());
            }

            @Override
            public void onFail() {
                // show error
                Toast.makeText(getApplicationContext(), "Error while creating the Event .. ", Toast.LENGTH_SHORT).show();
                shareEventDialog.dismiss();
            }
        });
    }


    private void addEvent(final Event event) {
        final ShareEventDialog shareEventDialog = new ShareEventDialog();
        shareEventDialog.show(getSupportFragmentManager(), "shareEventDialog");

        DatabaseHelper.addEvent(this, event, new EventCreation() {
            @Override
            public void onSuccess() {
                //refresh the whole DB
                new resetDbTask(getApplicationContext(), resetDbTask.ALL_DB).execute();
                shareEventDialog.initQRCodeView(event.getEventName());
            }

            @Override
            public void onFail() {
                // show error
                Toast.makeText(getApplicationContext(), "Error while creating the Event .. ", Toast.LENGTH_SHORT).show();
                shareEventDialog.dismiss();
            }
        });


    }

    private void deleteEvent() {
        DatabaseHelper.deleteEvent(this, oldEvent.getId(), new EventCreation() {
            @Override
            public void onSuccess() {
                //refresh the whole DB
                new resetDbTask(getApplicationContext(), resetDbTask.ALL_DB).execute();
                closeActivity();
            }

            @Override
            public void onFail() {
                // show error
                Toast.makeText(getApplicationContext(), "Error while Updating the Event .. ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void closeActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
