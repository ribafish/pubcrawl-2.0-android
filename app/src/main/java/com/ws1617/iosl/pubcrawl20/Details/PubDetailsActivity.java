package com.ws1617.iosl.pubcrawl20.Details;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PersonDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.RequestQueueHelper;
import com.ws1617.iosl.pubcrawl20.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PubDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "PubDetailsActivity";

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
    private ArrayList<Bitmap> images = new ArrayList<>();

    private Context context;

    private Pub pub;
    private PersonMini owner;
    private String address = "Location unknown";
    private ArrayList<PersonMini> topPersonsList = new ArrayList<>();
    private ArrayList<EventMini> futureEventsList = new ArrayList<>();
    private ArrayList<EventMini> pastEventsList = new ArrayList<>();

    private PersonAdapter topPersonsAdapter;
    private ListView topPersonsListView;

    private EventAdapter futureEventsAdapter;
    private ListView futureEventsListView;

    private EventAdapter pastEventsAdapter;
    private ListView pastEventsListView;

    private BroadcastReceiver receiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_details);
        context = this;

        long id = getIntent().getLongExtra("id", -1);
        getPub(id);
        setupToolbar();

        setupTopPersons();
        setupFutureEvents();
        setupPastEvents();

        populateFields();

        initOpeningTimesExpanding();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(RequestQueueHelper.BROADCAST_INTENT)) {
                    Log.d(TAG, "Got database refreshed broadcast");
                    getPub(pub.getId());
                }
            }
        };
        IntentFilter filter = new IntentFilter(RequestQueueHelper.BROADCAST_INTENT);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void populateFields() {
        Log.d(TAG, "populateFields()");
        if (owner != null) {
            ((TextView) findViewById(R.id.pub_details_owner)).setText(owner.getName());
            findViewById(R.id.pub_details_owner).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PersonDetailsActivity.class);
                    intent.putExtra("name", owner.getName());
                    intent.putExtra("id", owner.getId());
                    startActivity(intent);
                }
            });
        }

        if (pub != null) {
            // Toolbar
            mTitle.setText(pub.getPubName());
            mSubtitle.setText(address);
            mToolbar.setTitle(pub.getPubName());

            // Info card
            ((TextView) findViewById(R.id.pub_details_id)).setText(String.valueOf(pub.getId()));
            ((TextView) findViewById(R.id.pub_details_address)).setText(address);
            ((TextView) findViewById(R.id.pub_details_latlng)).setText(
                    String.format(Locale.ENGLISH, "%.2f, %.2f",
                            pub.getLatLng().latitude,
                            pub.getLatLng().longitude));
            LinearLayout size = (LinearLayout) findViewById(R.id.pub_details_size);
            size.removeAllViews();
            if (pub.getSize() > 0) {
                for (int i = 0; i < pub.getSize(); i++) {
                    ImageView v = new ImageView(context);
                    v.setImageResource(R.drawable.ic_person);
                    v.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray));
                    v.setMaxHeight(16);
                    v.setMaxWidth(16);
                    size.addView(v);
                }
            } else {
                ImageView v = new ImageView(context);
                v.setImageResource(R.drawable.question_mark);
                v.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray));
                v.setMaxHeight(16);
                v.setMaxWidth(16);
                size.addView(v);
            }
            LinearLayout prices = (LinearLayout) findViewById(R.id.pub_details_prices);
            prices.removeAllViews();
            if (pub.getPrices() > 0) {
                for (int i = 0; i < pub.getPrices(); i++) {
                    ImageView v = new ImageView(context);
                    v.setImageResource(R.drawable.ic_euro_money);
                    v.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray));
                    v.setMaxHeight(16);
                    v.setMaxWidth(16);
                    prices.addView(v);
                }
            } else {
                ImageView v = new ImageView(context);
                v.setImageResource(R.drawable.question_mark);
                v.setColorFilter(ContextCompat.getColor(context, R.color.dark_gray));
                v.setMaxHeight(16);
                v.setMaxWidth(16);
                prices.addView(v);
            }
            ((TextView) findViewById(R.id.pub_details_rating)).setText(String.valueOf(pub.getRating()));

            ((TextView) findViewById(R.id.pub_details_address)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // "geo:<lat>,<long>?q=<lat>,<long>(Label+Name)"
                    String command = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                            pub.getLatLng().latitude,
                            pub.getLatLng().longitude,
                            pub.getLatLng().latitude,
                            pub.getLatLng().longitude,
                            pub.getPubName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(command));
                    startActivity(intent);
                }
            });
            ((TextView) findViewById(R.id.pub_details_latlng)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // "geo:<lat>,<long>?q=<lat>,<long>(Label+Name)"
                    String command = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                            pub.getLatLng().latitude,
                            pub.getLatLng().longitude,
                            pub.getLatLng().latitude,
                            pub.getLatLng().longitude,
                            pub.getPubName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(command));
                    startActivity(intent);
                }
            });


            // Opening times card
            ((TextView) findViewById(R.id.pub_details_times)).setText(pub.getOpeningTimes());

            if (pub.getImages() != null) {
                images.clear();
                images.addAll(pub.getImages());
                imageCarouselPager.notifyDataSetChanged();
            }
        }

        if (topPersonsList.size() > 0) {
            topPersonsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(topPersonsListView);
        } else Log.e(TAG, "topPersonsList empty");

        if (futureEventsList.size() > 0) {
            futureEventsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(futureEventsListView);
        } else Log.e(TAG, "futureEventsList empty");

        if (pastEventsList.size() > 0) {
            pastEventsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnItems(pastEventsListView);
        } else Log.e(TAG, "pastEventsList empty");
    }

    /*

        View control setup functions

     */

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.pub_details_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.pub_details_appbar);
        mTitle = (TextView) findViewById(R.id.pub_details_title);
        mSubtitle = (TextView) findViewById(R.id.pub_details_subtitle);
        mCollapasableTitleContainer = (LinearLayout) findViewById(R.id.pub_details_layout_title);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pub_details_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.pub_details_tabDots);


        mAppBarLayout.addOnOffsetChangedListener(this);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        try{
            String pubName = getIntent().getStringExtra("name");

            mToolbar.setTitle(pubName);
            mTitle.setText(pubName);
            mSubtitle.setText("");

        } catch (Exception e) { e.printStackTrace(); }


        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        imageCarouselPager = new ImageCarouselPager(this, images);
        viewPager.setAdapter(imageCarouselPager);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager, true);


        startAlphaAnimation(mToolbar, 0, View.INVISIBLE);

        mToolbar.inflateMenu(R.menu.pub_details_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pub_details_menu_add:
                        addFavourite(true);
                        return true;
                    case R.id.pub_details_menu_remove:
                        addFavourite(false);
                        return true;
                }
                return true;
            }
        });
        ImageView appBarAddButton = (ImageView) findViewById(R.id.pub_details_layout_add_button);
        appBarAddButton.setClickable(true);
        appBarAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mToolbar.getMenu().findItem(R.id.pub_details_menu_add).isVisible()) {
                    addFavourite(true);
                } else {
                    addFavourite(false);
                }
            }
        });
    }

    public void addFavourite(final boolean favourite) {
        //TODO: Add favourite
        final Context context = this;
        DatabaseHelper.addFavouritePub(this, pub.getId(), favourite, new DetailsCallback() {
            @Override
            public void onSuccess() {
                updateAddButtons(favourite);
            }

            @Override
            public void onFail() {
                Toast.makeText(context, "Can't connect to server", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateAddButtons(boolean favourite) {
        ImageView appBarJoinButton = (ImageView) findViewById(R.id.pub_details_layout_add_button);
        if (favourite) {
            mToolbar.getMenu().findItem(R.id.pub_details_menu_add).setVisible(false);
            mToolbar.getMenu().findItem(R.id.pub_details_menu_remove).setVisible(true);
            appBarJoinButton.setImageResource(R.drawable.ic_favorite_black_24dp);
        } else {
            mToolbar.getMenu().findItem(R.id.pub_details_menu_add).setVisible(true);
            mToolbar.getMenu().findItem(R.id.pub_details_menu_remove).setVisible(false);
            appBarJoinButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }
    }


    private void initOpeningTimesExpanding() {
        final CardView openingTimesCard = (CardView) findViewById(R.id.pub_details_times_card);
        final RelativeLayout openingTimesLayout = (RelativeLayout) findViewById(R.id.pub_details_times_layout);
        final ImageView openingTimesGradient = (ImageView) findViewById(R.id.pub_details_times_gradient);
        final ImageView openingTimesArrow = (ImageView) findViewById(R.id.pub_details_times_arrow);
        final int height200 = (int) (200 * getResources().getDisplayMetrics().density);
        final int padding16 = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        TextView openingTimes = (TextView) findViewById(R.id.pub_details_times);
        openingTimes.requestLayout();
        openingTimes.measure(0, 0);
        int textHeight = (int) (openingTimes.getMeasuredHeight() / getResources().getDisplayMetrics().density + 0.5f);
        int textWidth = (int) (openingTimes.getMeasuredWidth() / getResources().getDisplayMetrics().density + 0.5f);
        int width = (int) (metrics.widthPixels / getResources().getDisplayMetrics().density + 0.5f) - 40;
        int height = textHeight + textWidth / width * 18;
        Log.d(TAG, String.format("textHeight: %d; textWidth: %d; width: %d; height: %d", textHeight, textWidth, width, height));

        // 200dp - 2*16dp (padding) - 8dp (title margin) - 20sp (title text size) = 140dp
        if (height > 140) {
            openingTimesLayout.getLayoutParams().height = height200;
            openingTimesGradient.setVisibility(View.VISIBLE);
            openingTimesLayout.setPadding(padding16, padding16, padding16, 0);
            openingTimesArrow.setImageResource(R.drawable.ic_expand_more);
            openingTimesArrow.setVisibility(View.VISIBLE);

            openingTimesCard.setClickable(true);
            openingTimesCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Description onclick");
                    if (openingTimesGradient.getVisibility() == View.VISIBLE) {      // Expand
                        openingTimesGradient.setVisibility(View.GONE);
                        openingTimesLayout.setPadding(padding16, padding16, padding16, padding16);
                        openingTimesLayout.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                        openingTimesArrow.setImageResource(R.drawable.ic_expand_less);
                    } else {                                                        // Collapse
                        openingTimesLayout.getLayoutParams().height = height200;
                        openingTimesGradient.setVisibility(View.VISIBLE);
                        openingTimesLayout.setPadding(padding16, padding16, padding16, 0);
                        openingTimesArrow.setImageResource(R.drawable.ic_expand_more);
                    }
                }
            });
        } else {
            openingTimesGradient.setVisibility(View.GONE);
            openingTimesLayout.setPadding(padding16, padding16, padding16, padding16);
            openingTimesLayout.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            openingTimesArrow.setVisibility(View.GONE);
            openingTimesCard.setClickable(false);
        }
    }

    private void setupTopPersons() {
        if (this.topPersonsList.size() < 6) {
            topPersonsAdapter = new PersonAdapter(this, topPersonsList);
        } else {
            ArrayList<PersonMini> p = new ArrayList<>(topPersonsList.subList(0, 6));
            topPersonsAdapter = new PersonAdapter(this, p);
        }
        topPersonsListView = (ListView) findViewById(R.id.pub_details_top_participants_listView);
        topPersonsListView.setAdapter(topPersonsAdapter);
        topPersonsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, PersonDetailsActivity.class);
                intent.putExtra("name", topPersonsList.get(i).getName());
                intent.putExtra("id", topPersonsList.get(i).getId());
                startActivity(intent);
            }
        });
        if (topPersonsList.size() < 4) {
            ((Button)findViewById(R.id.pub_details_top_participants_show_all_btn)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.pub_details_top_participants_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.pub_details_top_participants_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(topPersonsListView);
        } else {
            final int height280 = (int) (280 * getResources().getDisplayMetrics().density);
            ((Button)findViewById(R.id.pub_details_top_participants_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.pub_details_top_participants_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.pub_details_top_participants_layout)).getLayoutParams().height = height280;
            findViewById(R.id.pub_details_top_participants_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand participants onPubItemClicked");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.participants));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    PersonDialogFragment personDialogFragment = new PersonDialogFragment();
                    personDialogFragment.setParticipants(topPersonsList);
                    personDialogFragment.setArguments(args);
                    personDialogFragment.show(ft, "map_dialog_fragment");
                }
            });
            // Disable scrolling
            topPersonsListView.setScrollContainer(false);
            topPersonsListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }

    private void setupFutureEvents() {
        if (this.futureEventsList.size() < 8) {
            futureEventsAdapter = new EventAdapter(this, futureEventsList);
        } else {
            ArrayList<EventMini> p = new ArrayList<>(futureEventsList.subList(0, 8));
            futureEventsAdapter = new EventAdapter(this, p);
        }
        futureEventsListView = (ListView) findViewById(R.id.pub_details_events_future_listView);
        futureEventsListView.setAdapter(futureEventsAdapter);
        futureEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("name", futureEventsList.get(i).getName());
                intent.putExtra("id", futureEventsList.get(i).getId());
                startActivity(intent);
            }
        });
        if (futureEventsList.size() < 6) {
            ((Button)findViewById(R.id.pub_details_events_future_show_all_btn)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.pub_details_events_future_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.pub_details_events_future_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(futureEventsListView);
        } else {
            final int height260 = (int) (260 * getResources().getDisplayMetrics().density);
            ((Button)findViewById(R.id.pub_details_events_future_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.pub_details_events_future_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.pub_details_events_future_layout)).getLayoutParams().height = height260;
            findViewById(R.id.pub_details_events_future_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand participants onPubItemClicked");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.future_events));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    EventDialogFragment eventDialogFragment = new EventDialogFragment();
                    eventDialogFragment.setEvents(futureEventsList);
                    eventDialogFragment.setArguments(args);
                    eventDialogFragment.show(ft, "map_dialog_fragment");
                }
            });
            // Disable scrolling
            futureEventsListView.setScrollContainer(false);
            futureEventsListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }

    private void setupPastEvents() {
        if (this.pastEventsList.size() < 8) {
            pastEventsAdapter = new EventAdapter(this, pastEventsList);
        } else {
            ArrayList<EventMini> p = new ArrayList<>(pastEventsList.subList(0, 8));
            pastEventsAdapter = new EventAdapter(this, p);
        }
        pastEventsListView = (ListView) findViewById(R.id.pub_details_events_past_listView);
        pastEventsListView.setAdapter(pastEventsAdapter);
        pastEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("name", pastEventsList.get(i).getName());
                intent.putExtra("id", pastEventsList.get(i).getId());
                startActivity(intent);
            }
        });
        if (pastEventsList.size() < 6) {
            ((Button)findViewById(R.id.pub_details_events_past_show_all_btn)).setVisibility(View.GONE);
            ((ImageView)findViewById(R.id.pub_details_events_past_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.pub_details_events_past_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(pastEventsListView);
        } else {
            final int height260 = (int) (260 * getResources().getDisplayMetrics().density);
            ((Button)findViewById(R.id.pub_details_events_past_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.pub_details_events_past_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.pub_details_events_past_layout)).getLayoutParams().height = height260;
            findViewById(R.id.pub_details_events_past_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand participants onPubItemClicked");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.past_events));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    EventDialogFragment eventDialogFragment = new EventDialogFragment();
                    eventDialogFragment.setEvents(pastEventsList);
                    eventDialogFragment.setArguments(args);
                    eventDialogFragment.show(ft, "map_dialog_fragment");
                }
            });
            // Disable scrolling
            pastEventsListView.setScrollContainer(false);
            pastEventsListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
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

    private void getPub(long id) {
        try {
            pub = new PubDbHelper().getPub(id);

            try {
                getTopPeople(pub.getTopsListIds());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                getEvents(pub.getEventsListIds());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                owner = getPersonMini(pub.getOwnerId());
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
            getAddressFromLatLng();
        } catch (DatabaseException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Can't find data for Pub with ID " + id, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "getPub: id " + id + ", pub == null");
            return;
        }
    }

    /**
     * Geocoder way didn't work, so this is a workaround
      */
    private void getAddressFromLatLng() {
        final RequestQueueHelper requestQueue = new RequestQueueHelper(context);
        String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                pub.getLatLng().latitude + ","+ pub.getLatLng().longitude + "&sensor=true";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject first = response.getJSONArray("results").getJSONObject(0);
                    address = first.getString("formatted_address");
                    mSubtitle.setText(address);
                    ((TextView) findViewById(R.id.pub_details_address)).setText(address);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getAddressFromLatLng:onErrorResponse: " + error.toString());
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);


    }

    private PersonMini getPersonMini(long id) throws DatabaseException {
        return new PersonMini(new PersonDbHelper().getPerson(id));
    }


    private EventMini getEventMini(long id) throws DatabaseException{
        return new EventMini(new EventDbHelper().getEvent(id));
    }

    private void getTopPeople(ArrayList<Long> ids) {
        for (long id : ids) {
            try {
                topPersonsList.add(getPersonMini(id));
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }

    private void getEvents(ArrayList<Long> ids) {
        Date rightNow = Calendar.getInstance().getTime();
        Log.d(TAG, "rightnow: " + rightNow.toString());

        for (long id : ids) {
            try {
                EventMini e = getEventMini(id);
                Log.d(TAG, "eventMini date: " + e.getDate().toString());
                if (e.getDate().compareTo(rightNow) >= 1) {
                    futureEventsList.add(e);
                } else {
                    pastEventsList.add(e);
                }
            } catch (DatabaseException e1) {
                e1.printStackTrace();
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
