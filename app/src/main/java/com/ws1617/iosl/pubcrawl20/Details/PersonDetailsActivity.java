package com.ws1617.iosl.pubcrawl20.Details;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.DataModels.Person;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PersonDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;

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

        setupFriends();
        setupEvents();
        setupFavouritePubs();
        setupOwnedEvents();
        setupOwnedPubs();

        populateFields();
        initDescriptionExpanding();
    }

    private void populateFields() {
        Log.d(TAG, "populateFields()");

        if (person != null) {
            // Toolbar
            mTitle.setText(person.getName());
            mSubtitle.setText("Id: " + String.valueOf(person.getId()));
            mToolbar.setTitle(person.getName());

            // Description card
            ((TextView) findViewById(R.id.person_details_description)).setText(person.getDescription());
        }

        if (friends.size() > 0) {
            ((CardView) findViewById(R.id.person_details_friends_card)).setVisibility(View.VISIBLE);
            friendsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(friendsListView);
        } else {
            Log.e(TAG, "friends empty");
            ((CardView) findViewById(R.id.person_details_friends_card)).setVisibility(View.GONE);
        }

        if (events.size() > 0) {
            ((CardView) findViewById(R.id.person_details_events_card)).setVisibility(View.VISIBLE);
            eventAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(eventListView);
        } else {
            Log.e(TAG, "events empty");
            ((CardView) findViewById(R.id.person_details_events_card)).setVisibility(View.GONE);
        }

        if (favouritePubs.size() > 0) {
            ((CardView) findViewById(R.id.person_details_favourite_pubs_card)).setVisibility(View.VISIBLE);
            favouritePubsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(favouritePubsListView);
        } else {
            Log.e(TAG, "favouritePubs empty");
            ((CardView) findViewById(R.id.person_details_favourite_pubs_card)).setVisibility(View.GONE);
        }

        if (ownedPubs.size() > 0) {
            ((CardView) findViewById(R.id.person_details_owned_pubs_card)).setVisibility(View.VISIBLE);
            ownedPubsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(ownedPubsListView);
        } else {
            Log.e(TAG, "ownedPubs empty");
            ((CardView) findViewById(R.id.person_details_owned_pubs_card)).setVisibility(View.GONE);
        }

        if (ownedEvents.size() > 0) {
            ((CardView) findViewById(R.id.person_details_owned_events_card)).setVisibility(View.VISIBLE);
            ownedEventsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(ownedEventsListView);
        } else {
            Log.e(TAG, "ownedEvents empty");
            ((CardView) findViewById(R.id.person_details_owned_events_card)).setVisibility(View.GONE);
        }
    }

    private void setupFriends() {
        if (this.friends.size() < 6) {
            friendsAdapter = new PersonAdapter(this, friends);
        } else {
            ArrayList<PersonMini> p = new ArrayList<>(friends.subList(0, 6));
            friendsAdapter = new PersonAdapter(this, p);
        }
        friendsListView = (ListView) findViewById(R.id.person_details_friends_listView);
        friendsListView.setAdapter(friendsAdapter);
        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, PersonDetailsActivity.class);
                intent.putExtra("name", friends.get(i).getName());
                intent.putExtra("id", friends.get(i).getId());
                startActivity(intent);
            }
        });
        if (friends.size() < 4) {
            ((Button)findViewById(R.id.person_details_friends_show_all_btn)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.person_details_friends_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.person_details_friends_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(friendsListView);
        } else {
            final int height280 = (int) (280 * getResources().getDisplayMetrics().density);
            ((Button)findViewById(R.id.person_details_friends_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.person_details_friends_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.person_details_friends_layout)).getLayoutParams().height = height280;
            findViewById(R.id.person_details_friends_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand friends onClick");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.friends));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    PersonDialogFragment personDialogFragment = new PersonDialogFragment();
                    personDialogFragment.setParticipants(friends);
                    personDialogFragment.setArguments(args);
                    personDialogFragment.show(ft, "person_details_friends_fragment");
                }
            });
            // Disable scrolling
            friendsListView.setScrollContainer(false);
            friendsListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }

    private void setupEvents() {
        if (this.events.size() < 6) {
            eventAdapter = new EventAdapter(this, events);
        } else {
            ArrayList<EventMini> p = new ArrayList<>(events.subList(0, 6));
            eventAdapter = new EventAdapter(this, p);
        }
        eventListView = (ListView) findViewById(R.id.person_details_events_listView);
        eventListView.setAdapter(eventAdapter);
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("name", events.get(i).getName());
                intent.putExtra("id", events.get(i).getId());
                startActivity(intent);
            }
        });
        if (events.size() < 4) {
            ((Button)findViewById(R.id.person_details_events_show_all_btn)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.person_details_events_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.person_details_events_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(eventListView);
        } else {
            final int height280 = (int) (280 * getResources().getDisplayMetrics().density);
            ((Button)findViewById(R.id.person_details_events_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.person_details_events_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.person_details_events_layout)).getLayoutParams().height = height280;
            findViewById(R.id.person_details_events_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand events onClick");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.joined_events));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    EventDialogFragment eventDialogFragment = new EventDialogFragment();
                    eventDialogFragment.setEvents(events);
                    eventDialogFragment.setArguments(args);
                    eventDialogFragment.show(ft, "person_details_events_fragment");
                }
            });
            // Disable scrolling
            eventListView.setScrollContainer(false);
            eventListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }

    private void setupFavouritePubs() {
        if (this.favouritePubs.size() < 6) {
            favouritePubsAdapter = new PubAdapter(this, favouritePubs);
        } else {
            ArrayList<PubMini> p = new ArrayList<>(favouritePubs.subList(0, 6));
            favouritePubsAdapter = new PubAdapter(this, p);
        }
        favouritePubsListView = (ListView) findViewById(R.id.person_details_favourite_pubs_listView);
        favouritePubsListView.setAdapter(favouritePubsAdapter);
        favouritePubsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, PubDetailsActivity.class);
                intent.putExtra("name", favouritePubs.get(i).getName());
                intent.putExtra("id", favouritePubs.get(i).getId());
                startActivity(intent);
            }
        });
        if (favouritePubs.size() < 4) {
            ((Button)findViewById(R.id.person_details_favourite_pubs_show_all_btn)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.person_details_favourite_pubs_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.person_details_favourite_pubs_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(favouritePubsListView);
        } else {
            final int height280 = (int) (280 * getResources().getDisplayMetrics().density);
            ((Button)findViewById(R.id.person_details_favourite_pubs_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.person_details_favourite_pubs_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.person_details_favourite_pubs_layout)).getLayoutParams().height = height280;
            findViewById(R.id.person_details_favourite_pubs_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand favourite pubs onClick");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.favourite_pubs));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    PubDialogFragment pubDialogFragment = new PubDialogFragment();
                    pubDialogFragment.setPubs(favouritePubs);
                    pubDialogFragment.setArguments(args);
                    pubDialogFragment.show(ft, "person_details_favourite_pubs_fragment");
                }
            });
            // Disable scrolling
            favouritePubsListView.setScrollContainer(false);
            favouritePubsListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }

    private void setupOwnedPubs() {
        if (this.ownedPubs.size() < 6) {
            ownedPubsAdapter = new PubAdapter(this, ownedPubs);
        } else {
            ArrayList<PubMini> p = new ArrayList<>(ownedPubs.subList(0, 6));
            ownedPubsAdapter = new PubAdapter(this, p);
        }
        ownedPubsListView = (ListView) findViewById(R.id.person_details_owned_pubs_listView);
        ownedPubsListView.setAdapter(ownedPubsAdapter);
        ownedPubsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, PubDetailsActivity.class);
                intent.putExtra("name", ownedPubs.get(i).getName());
                intent.putExtra("id", ownedPubs.get(i).getId());
                startActivity(intent);
            }
        });
        if (ownedPubs.size() < 4) {
            ((Button)findViewById(R.id.person_details_owned_pubs_show_all_btn)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.person_details_owned_pubs_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.person_details_owned_pubs_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(ownedPubsListView);
        } else {
            final int height280 = (int) (280 * getResources().getDisplayMetrics().density);
            ((Button)findViewById(R.id.person_details_owned_pubs_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.person_details_owned_pubs_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.person_details_owned_pubs_layout)).getLayoutParams().height = height280;
            findViewById(R.id.person_details_owned_pubs_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand owned pubs onClick");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.owned_pubs));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    PubDialogFragment pubDialogFragment = new PubDialogFragment();
                    pubDialogFragment.setPubs(ownedPubs);
                    pubDialogFragment.setArguments(args);
                    pubDialogFragment.show(ft, "person_details_owned_pubs_fragment");
                }
            });
            // Disable scrolling
            ownedPubsListView.setScrollContainer(false);
            ownedPubsListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }

    private void setupOwnedEvents() {
        if (this.ownedEvents.size() < 6) {
            ownedEventsAdapter = new EventAdapter(this, ownedEvents);
        } else {
            ArrayList<EventMini> p = new ArrayList<>(ownedEvents.subList(0, 6));
            ownedEventsAdapter = new EventAdapter(this, p);
        }
        ownedEventsListView = (ListView) findViewById(R.id.person_details_owned_events_listView);
        ownedEventsListView.setAdapter(ownedEventsAdapter);
        ownedEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("name", ownedEvents.get(i).getName());
                intent.putExtra("id", ownedEvents.get(i).getId());
                startActivity(intent);
            }
        });
        if (ownedEvents.size() < 4) {
            ((Button)findViewById(R.id.person_details_owned_events_show_all_btn)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.person_details_owned_events_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.person_details_owned_events_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(ownedEventsListView);
        } else {
            final int height280 = (int) (280 * getResources().getDisplayMetrics().density);
            ((Button)findViewById(R.id.person_details_owned_events_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.person_details_owned_events_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.person_details_owned_events_layout)).getLayoutParams().height = height280;
            findViewById(R.id.person_details_owned_events_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand participants onClick");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.participants));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    EventDialogFragment eventDialogFragment = new EventDialogFragment();
                    eventDialogFragment.setEvents(ownedEvents);
                    eventDialogFragment.setArguments(args);
                    eventDialogFragment.show(ft, "map_dialog_fragment");
                }
            });
            // Disable scrolling
            ownedEventsListView.setScrollContainer(false);
            ownedEventsListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
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

     */

    private void getPerson(long id) {
        try {
            person = new PersonDbHelper(this).getPerson(id);
        } catch (DatabaseException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Can't find data for Person with ID " + id, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "getPerson: id " + id + ", person == null");
            return;
        }

        getFriends();
        getEvents();
        getFavouritePubs();
        getOwnedPubs();
        getOwnedEvents();
    }

    private PersonMini getPersonMini(long id) throws DatabaseException {
        return new PersonMini(new PersonDbHelper(this).getPerson(id));
    }


    private EventMini getEventMini(long id) throws DatabaseException {
        return new EventMini(new EventDbHelper(this).getEvent(id));
    }

    private PubMini getPubMini(long id) throws DatabaseException {
        return new PubMini(new PubDbHelper(this).getPub(id), null);
    }

    private void getFriends() {
        for (Long i : person.getFriendIds()) {
            try {
                friends.add(getPersonMini(i));
            } catch (DatabaseException de) {
                de.printStackTrace();
            }
        }
    }

    private void getEvents() {
        for (long i : person.getEventIds()) {
            try {
                events.add(getEventMini(i));
            } catch (DatabaseException de) {
                de.printStackTrace();
            }
        }
    }

    private void getFavouritePubs() {
        for (long i : person.getFavouritePubIds()) {
            try {
                favouritePubs.add(getPubMini(i));
            } catch (DatabaseException de) {
                de.printStackTrace();
            }
        }
    }

    private void getOwnedPubs() {
        for (long i : person.getOwnedPubIds()) {
            try {
                ownedPubs.add(getPubMini(i));
            } catch (DatabaseException de) {
                de.printStackTrace();
            }
        }
    }

    private void getOwnedEvents() {
        for (long i : person.getOwnedEventIds()) {
            try {
                ownedEvents.add(getEventMini(i));
            } catch (DatabaseException de) {
                de.printStackTrace();
            }
        }
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
