package com.ws1617.iosl.pubcrawl20.Details;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlotComparator;
import com.ws1617.iosl.pubcrawl20.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Gasper Kojek on 2. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class EventDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "EventDetailsActivity";

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.75f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mCollapsableTitleContainer;
    private TextView mTitle;
    private TextView mSubtitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private ImageCarouselPager imageCarouselPager;

    private Event event;
    private ArrayList<PubMini> pubs = new ArrayList<>();
    private ArrayList<PersonMini> participants = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        mToolbar = (Toolbar) findViewById(R.id.event_details_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.event_details_appbar);
        mTitle = (TextView) findViewById(R.id.event_details_title);
        mSubtitle = (TextView) findViewById(R.id.event_details_subtitle);
        mCollapsableTitleContainer = (LinearLayout) findViewById(R.id.event_details_layout_title);
        ViewPager viewPager = (ViewPager) findViewById(R.id.event_details_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.event_details_tabDots);


        mAppBarLayout.addOnOffsetChangedListener(this);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        long id = getIntent().getLongExtra("id", -1);

        try{
            String eventName = getIntent().getStringExtra("name");

            mToolbar.setTitle(eventName);
            mTitle.setText(eventName);
            mSubtitle.setText("Loading...");  //TODO

        } catch (Exception e) { e.printStackTrace(); }

        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        imageCarouselPager = new ImageCarouselPager(this, images);
        viewPager.setAdapter(imageCarouselPager);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager, true);

        getEvent(id);
        initOnClickListeners();

        startAlphaAnimation(mToolbar, 0, View.INVISIBLE);
    }

    private void initOnClickListeners() {
        final CardView descriptionCard = (CardView) findViewById(R.id.event_details_description_card);
        descriptionCard.setClickable(true);
        final RelativeLayout descriptionLayout = (RelativeLayout) findViewById(R.id.event_details_description_layout);
        final ImageView descriptionGradient = (ImageView) findViewById(R.id.event_details_description_gradient);
        final int descriptionInitialHeight = descriptionLayout.getLayoutParams().height;
        final int padding16 = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);

        descriptionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Description onclick");
                if (descriptionGradient.getVisibility() == View.VISIBLE) {      // Expand
                    descriptionGradient.setVisibility(View.GONE);
                    descriptionLayout.setPadding(padding16, padding16, padding16, padding16);
                    descriptionLayout.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else {                                                        // Collapse
                    descriptionLayout.getLayoutParams().height = descriptionInitialHeight;
                    descriptionGradient.setVisibility(View.VISIBLE);
                    descriptionLayout.setPadding(padding16,padding16,padding16, 0);
                }
            }
        });
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mToolbar, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mToolbar, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mCollapsableTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mCollapsableTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    // TODO: Change to get it from database when it will be ready
    private void getEvent(long id) {
        ArrayList<Long> dummyIds = new ArrayList<>();
        dummyIds.add((long)0);
        dummyIds.add((long)1);
        dummyIds.add((long)2);
        dummyIds.add((long)3);
        dummyIds.add((long)4);
        dummyIds.add((long)5);
        dummyIds.add((long)6);


        this.event = new Event(
                "Abstract event " + id,
                new Date(2016, 12, 31),
                getResources().getString(R.string.lorem),
                true,
                dummyIds,
                12,
                dummyIds
        );

        ArrayList<TimeSlot> timeSlots= new ArrayList<>();
        timeSlots.add(new TimeSlot(
                1,
                new Date(2016, 12, 31, 21, 0, 0),
                new Date(2016, 12, 31, 22, 0, 0)
        ));
        timeSlots.add(new TimeSlot(
                0,
                new Date(2016, 12, 31, 22, 0, 0),
                new Date(2016, 12, 31, 23, 0, 0)
        ));
        timeSlots.add(new TimeSlot(
                2,
                new Date(2016, 12, 31, 20, 0, 0),
                new Date(2016, 12, 31, 21, 0, 0)
        ));
        this.event.setTimeSlotList(timeSlots);
        getPubMinis(this.event.getPubIds(), this.event.getTimeSlotList());
        getParticipants(this.event.getParticipantIds());
    }

    // TODO: Change to get it from database when it will be ready
    private void getPubMinis (ArrayList<Long> ids, ArrayList<TimeSlot> slots) {
        ArrayList<Long> mIds = new ArrayList<>(ids);
        for (TimeSlot t : slots) {
            long id = t.getPubId();
            this.pubs.add(new PubMini("Pub " + id, t, id));
            mIds.remove(id);
        }

        Collections.sort(this.pubs, new PubMiniComparator());

        for (Long id : mIds) {
            this.pubs.add(new PubMini("Pub " + id, null, id));
        }
    }

    private void getParticipants (ArrayList<Long> ids) {
        for (long id : ids) {
            this.participants.add(new PersonMini("Person " + id, id));
        }
    }

    class PubMini {
        String name;
        TimeSlot timeSlot;
        long id;

        public PubMini(String name, TimeSlot timeSlot, long id) {
            this.name = name;
            this.timeSlot = timeSlot;
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            return this.id == ((PubMini)obj).id;
        }
    }

    class PubMiniComparator implements Comparator<PubMini> {
        @Override
        public int compare(PubMini pubMini, PubMini t1) {
            return new TimeSlotComparator(true, TimeSlotComparator.START)
                    .compare(pubMini.timeSlot, t1.timeSlot);
        }
    }

    class PersonMini {
        String name;
        long id;

        public PersonMini(String name, long id) {
            this.name = name;
            this.id = id;
        }
    }


}
