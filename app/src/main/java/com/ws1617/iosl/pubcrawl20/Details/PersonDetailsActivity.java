package com.ws1617.iosl.pubcrawl20.Details;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.EventMini;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PersonMini;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMini;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PersonDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "PersonDetailsActivity";

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.75f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mCollapasableTitleContainer;
    private TextView mTitle;
    private TextView mSubtitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private ImageCarouselPager imageCarouselPager;

    private Context context;

    private Person person;
    private ArrayList<Bitmap> images;

    private ArrayList<PersonMini> friends = new ArrayList<>();
    private PersonAdapter friendsAdapter;
    private ListView friendsListView;

    private ArrayList<EventMini> events = new ArrayList<>();
    private EventAdapter eventAdapter;
    private ListView eventListView;

    private ArrayList<PubMini> favouritePubs = new ArrayList<>();
    private PubAdapter favouritePubsAdapter;
    private ListView favouritePubsListView;

    private ArrayList<PubMini> ownedPubs = new ArrayList<>();
    private PubAdapter ownedPubsAdapter;
    private ListView ownedPubsListView;

    private ArrayList<EventMini> ownedEvents = new ArrayList<>();
    private EventAdapter ownedEventsAdapter;
    private ListView ownedEventsListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);
        context = getApplicationContext();

        long id = getIntent().getLongExtra("id", -1);

        getPerson(id);

        setupToolbar();

        populateFields();
        initDescriptionExpanding();
    }

    private void populateFields() {
        Log.d(TAG, "populateFields()");

        if (person != null) {
            // Toolbar
            mTitle.setText(person.getName());
            mSubtitle.setText(String.valueOf(person.getId()));
            mToolbar.setTitle(person.getName());

            // Description card
            ((TextView) findViewById(R.id.pub_details_times)).setText(person.getDescription());
        }

        if (friends.size() > 0) {
            friendsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(friendsListView);
        } else Log.e(TAG, "friends empty");

        if (events.size() > 0) {
            eventAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(eventListView);
        } else Log.e(TAG, "events empty");

        if (favouritePubs.size() > 0) {
            favouritePubsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(favouritePubsListView);
        } else Log.e(TAG, "favouritePubs empty");

        if (ownedPubs.size() > 0) {
            ownedPubsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(ownedPubsListView);
        } else Log.e(TAG, "ownedPubs empty");

        if (ownedEvents.size() > 0) {
            ownedEventsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(ownedEventsListView);
        } else Log.e(TAG, "ownedEvents empty");
    }

    /*

        View control setup functions

     */

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.person_details_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.person_details_appbar);
        mTitle = (TextView) findViewById(R.id.person_details_title);
        mSubtitle = (TextView) findViewById(R.id.person_details_subtitle);
        mCollapasableTitleContainer = (LinearLayout) findViewById(R.id.person_details_layout_title);
        ViewPager viewPager = (ViewPager) findViewById(R.id.person_details_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.person_details_tabDots);


        mAppBarLayout.addOnOffsetChangedListener(this);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        try{
            String personName = getIntent().getStringExtra("name");

            mToolbar.setTitle(personName);
            mTitle.setText(personName);
            mSubtitle.setText("Loading...");  //TODO

        } catch (Exception e) { e.printStackTrace(); }

        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        imageCarouselPager = new ImageCarouselPager(this, images);
        viewPager.setAdapter(imageCarouselPager);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager, true);


        startAlphaAnimation(mToolbar, 0, View.INVISIBLE);
    }

    private void initDescriptionExpanding() {
        final CardView descriptionCard = (CardView) findViewById(R.id.person_details_description_card);
        final RelativeLayout descriptionLayout = (RelativeLayout) findViewById(R.id.person_details_description_layout);
        final ImageView descriptionGradient = (ImageView) findViewById(R.id.person_details_description_gradient);
        final ImageView descriptionArrow = (ImageView) findViewById(R.id.person_details_description_arrow);
        final int height200 = (int) (200 * getResources().getDisplayMetrics().density);
        final int padding16 = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        TextView description = (TextView) findViewById(R.id.person_details_description);
        description.requestLayout();
        description.measure(0, 0);
        int textHeight = (int) (description.getMeasuredHeight() / getResources().getDisplayMetrics().density + 0.5f);
        int textWidth = (int) (description.getMeasuredWidth() / getResources().getDisplayMetrics().density + 0.5f);
        int width = (int) (metrics.widthPixels / getResources().getDisplayMetrics().density + 0.5f) - 40;
        int height = textHeight + textWidth / width * 18;
        Log.d(TAG, String.format("textHeight: %d; textWidth: %d; width: %d; height: %d", textHeight, textWidth, width, height));

        // 200dp - 2*16dp (padding) - 8dp (title margin) - 20sp (title text size) = 140dp
        if (height > 140) {
            descriptionLayout.getLayoutParams().height = height200;
            descriptionGradient.setVisibility(View.VISIBLE);
            descriptionLayout.setPadding(padding16, padding16, padding16, 0);
            descriptionArrow.setImageResource(R.drawable.ic_expand_more);
            descriptionArrow.setVisibility(View.VISIBLE);

            descriptionCard.setClickable(true);
            descriptionCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Description onclick");
                    if (descriptionGradient.getVisibility() == View.VISIBLE) {      // Expand
                        descriptionGradient.setVisibility(View.GONE);
                        descriptionLayout.setPadding(padding16, padding16, padding16, padding16);
                        descriptionLayout.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                        descriptionArrow.setImageResource(R.drawable.ic_expand_less);
                    } else {                                                        // Collapse
                        descriptionLayout.getLayoutParams().height = height200;
                        descriptionGradient.setVisibility(View.VISIBLE);
                        descriptionLayout.setPadding(padding16, padding16, padding16, 0);
                        descriptionArrow.setImageResource(R.drawable.ic_expand_more);
                    }
                }
            });
        } else {
            descriptionGradient.setVisibility(View.GONE);
            descriptionLayout.setPadding(padding16, padding16, padding16, padding16);
            descriptionLayout.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            descriptionArrow.setVisibility(View.GONE);
            descriptionCard.setClickable(false);
        }
    }

    /**
     * Sets ListView height to show all items
     * @param listView ListView to change height
     * @return the raw height set to the listView
     */
    public static int setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return totalItemsHeight + totalDividersHeight;

        } else {
            return 0;
        }

    }

    /*

        Data load functions
        TODO: add them all

     */

    private void getPerson(long id) {
        // TODO
    }

    private PersonMini getPersonMini(long id) {
        return new PersonMini("Person " + id, id);
    }

    private EventMini getEventMini(long id) {
        Date date = Calendar.getInstance().getTime();
        int newdate = (int) (Math.random() * 30);
        date.setDate(newdate);

        return new EventMini("Dummy event " + id, id, date);

    }



    /*

        Expandable / Collapsable toolbar animation functions

     */

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
                startAlphaAnimation(mCollapasableTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mCollapasableTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
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
}
