package com.ws1617.iosl.pubcrawl20.Event;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ws1617.iosl.pubcrawl20.R;

public class EventDetailsActivity extends AppCompatActivity {
    private long eventId;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        try{
            eventId = getIntent().getLongExtra("eventId", -1);
            eventName = getIntent().getStringExtra("name");
        } catch (Exception e) { e.printStackTrace(); }


    }
}
