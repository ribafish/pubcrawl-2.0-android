package com.ws1617.iosl.pubcrawl20.Details;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.EventDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PersonDbHelper;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by Gasper Kojek on 2. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class EventDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "EventDetailsActivity";

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.75f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mCollapsableTitleContainer;
    private TextView mTitle;
    private TextView mSubtitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private ImageCarouselPager imageCarouselPager;
    private CoordinatorLayout rootView;

    private Event event;
    private ArrayList<PubMini> pubs = new ArrayList<>();
    private ArrayList<PersonMini> participants = new ArrayList<>();
    private PersonMini owner;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Context context = this;

    private PubAdapter pubAdapter;
    private ListView pubListView;

    private PersonAdapter personAdapter;
    private ListView participantListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_event_details);
        rootView = (CoordinatorLayout) findViewById(R.id.event_details_root);


        initRouteFragment();

        initDescriptionExpanding();

        setupToolbar();
        //  setupMap();
        //setupPubsListView();


        // TODO: change to the data load listener or smtn when database is ready
        populateFields();
        initDescriptionExpanding();
        setupParticipants();
    }


    void initRouteFragment() {
        // this list should come from the DB and from outside the fragment
        // the fragment it self get the data from outsource

        //View mode
        initVewMode();
        //Edit mode
        //TODO

        RouteFragment routeFragment = RouteFragment.newInstance(RouteFragment.DIALOG_STATUS.VIEW_MODE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.test_place_holder, routeFragment, "Route").commit();
       // routeFragment.setListOfPubs(pubs);

    }

    void initVewMode() {
        long id = getIntent().getLongExtra("id", -1);

        getEvent(id);
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.event_details_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.event_details_appbar);
        mTitle = (TextView) findViewById(R.id.event_details_title);
        mSubtitle = (TextView) findViewById(R.id.event_details_subtitle);
        mCollapsableTitleContainer = (LinearLayout) findViewById(R.id.event_details_layout_title);
        ViewPager viewPager = (ViewPager) findViewById(R.id.event_details_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.event_details_tabDots);

        try {
            String eventName = getIntent().getStringExtra("name");

            mToolbar.setTitle(eventName);
            mTitle.setText(eventName);
            mSubtitle.setText("Loading...");  //TODO

        } catch (Exception e) {
            e.printStackTrace();
        }


        mAppBarLayout.addOnOffsetChangedListener(this);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        imageCarouselPager = new ImageCarouselPager(this, images);
        viewPager.setAdapter(imageCarouselPager);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager, true);

        startAlphaAnimation(mToolbar, 0, View.INVISIBLE);

        ImageView appBarJoinButton = (ImageView) findViewById(R.id.event_details_layout_add_button);
        appBarJoinButton.setClickable(true);
        appBarJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mToolbar.getMenu().findItem(R.id.event_details_menu_add).isVisible()) {
                    joinEvent(true);
                } else {
                    joinEvent(false);
                }
            }
        });
        mToolbar.inflateMenu(R.menu.event_details_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.event_details_menu_add:
                        joinEvent(true);
                        return true;
                    case R.id.event_details_menu_remove:
                        joinEvent(false);
                        return true;
                }

                return true;

            }
        });
    }

    public void joinEvent(boolean join) {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        if (join){
            dialog.setTitle(getString(R.string.alert_join_event_title));
            dialog.setMessage(getString(R.string.alert_join_event_message));
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                    getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.join),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // TODO: join event
                            updateJoinButtons(true);
                            dialogInterface.dismiss();
                        }
                    });
            dialog.show();
        } else {
            dialog.setTitle(getString(R.string.alert_leave_event_title));
            dialog.setMessage(getString(R.string.alert_leave_event_message));
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                    getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.leave),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // TODO: join event
                            updateJoinButtons(false);
                            dialogInterface.dismiss();
                        }
                    });
            dialog.show();
        }
    }

    public void updateJoinButtons(boolean joined) {
        ImageView appBarJoinButton = (ImageView) findViewById(R.id.event_details_layout_add_button);
        if (joined) {
            mToolbar.getMenu().findItem(R.id.event_details_menu_add).setVisible(false);
            mToolbar.getMenu().findItem(R.id.event_details_menu_remove).setVisible(true);
            appBarJoinButton.setImageResource(R.drawable.ic_event_joined_black_24dp);
        } else {
            mToolbar.getMenu().findItem(R.id.event_details_menu_add).setVisible(true);
            mToolbar.getMenu().findItem(R.id.event_details_menu_remove).setVisible(false);
            appBarJoinButton.setImageResource(R.drawable.ic_event_add_black_24dp);
        }
    }


    private void initDescriptionExpanding() {
        final CardView descriptionCard = (CardView) findViewById(R.id.event_details_description_card);
        final RelativeLayout descriptionLayout = (RelativeLayout) findViewById(R.id.event_details_description_layout);
        final ImageView descriptionGradient = (ImageView) findViewById(R.id.event_details_description_gradient);
        final ImageView descriptionArrow = (ImageView) findViewById(R.id.event_details_description_arrow);
        final int height200 = (int) (200 * getResources().getDisplayMetrics().density);
        final int padding16 = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        TextView description = (TextView) findViewById(R.id.event_details_description);
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

    private void setupParticipants() {
        if (this.participants.size() < 6) {
            personAdapter = new PersonAdapter(this, participants);
        } else {
            ArrayList<PersonMini> p = new ArrayList<>(participants.subList(0, 6));
            personAdapter = new PersonAdapter(this, p);
        }
        participantListView = (ListView) findViewById(R.id.event_details_participants_listView);
        participantListView.setAdapter(personAdapter);
        participantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, PersonDetailsActivity.class);
                intent.putExtra("name", participants.get(i).getName());
                intent.putExtra("id", participants.get(i).getId());
                startActivity(intent);
            }
        });
        if (participants.size() < 4) {
            ((Button) findViewById(R.id.event_details_participants_show_all_btn)).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.event_details_participants_gradient)).setVisibility(View.GONE);
            ((RelativeLayout) findViewById(R.id.event_details_participants_layout)).getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            setListViewHeightBasedOnItems(participantListView);
        } else {
            final int height280 = (int) (280 * getResources().getDisplayMetrics().density);
            ((Button) findViewById(R.id.event_details_participants_show_all_btn)).setVisibility(View.VISIBLE);
            ((ImageView) findViewById(R.id.event_details_participants_gradient)).setVisibility(View.VISIBLE);
            ((RelativeLayout) findViewById(R.id.event_details_participants_layout)).getLayoutParams().height = height280;
            findViewById(R.id.event_details_participants_show_all_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "expand participants onPubItemClicked");
                    Bundle args = new Bundle();
                    args.putString("title", getString(R.string.participants));
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    PersonDialogFragment personDialogFragment = new PersonDialogFragment();
                    personDialogFragment.setParticipants(participants);
                    personDialogFragment.setArguments(args);
                    personDialogFragment.show(ft, "map_dialog_fragment");
                }
            });
            // Disable scrolling
            participantListView.setScrollContainer(false);
            participantListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return (motionEvent.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }


    private void populateFields() {
        if (owner != null) {
            ((TextView) findViewById(R.id.event_details_owner)).setText(owner.getName());
            findViewById(R.id.event_details_owner).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PersonDetailsActivity.class);
                    intent.putExtra("name", owner.getName());
                    intent.putExtra("id", owner.getId());
                    startActivity(intent);
                }
            });
        }

        if (event != null) {
            mTitle.setText(event.getEventName());
            mToolbar.setTitle(event.getEventName());
            SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy 'at' HH:mm");
            mSubtitle.setText(localDateFormat.format(event.getDate()));
            ((TextView) findViewById(R.id.event_details_starts)).setText(localDateFormat.format(event.getDate()));
            ((TextView) findViewById(R.id.event_details_id)).setText(String.valueOf(event.getId()));
            ((TextView) findViewById(R.id.event_details_tracked)).setText(event.isTracked() ? "Tracked" : "Not tracked");
            ((TextView) findViewById(R.id.event_details_description)).setText(event.getDescription());
        }

        if (pubs.size() > 0) {
            //pubAdapter.notifyDataSetChanged();
            //setListViewHeightBasedOnItems(pubListView);
        }

    }

 /*   private void setupPubsListView() {
        pubAdapter = new PubAdapter(this, mSelectedPupsList);
        pubListView = (ListView) findViewById(R.id.event_details_pubListView);
        pubListView.setAdapter(pubAdapter);
        pubListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, PubDetailsActivity.class);
                intent.putExtra("name", mSelectedPupsList.get(i).getName());
                intent.putExtra("id", mSelectedPupsList.get(i).getId());
                startActivity(intent);
            }
        });
    }*/

    /**
     * Sets ListView height to show all items
     *
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

        Map functions

     */

  /*  private void setupMap() {
        Log.d(TAG, "setupMap");
        FrameLayout mapContainer = (FrameLayout) findViewById(R.id.event_details_map_map);
//        mapContainer.getLayoutParams().height = mapContainer.getLayoutParams().width;

        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .scrollGesturesEnabled(false)
                .zoomGesturesEnabled(true)
                .liteMode(true);
        mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.event_details_map_map, mapFragment).commit();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady()");
                map = googleMap;
                map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {    // Sometimes wouldn't load the map
                    @Override
                    public void onMapLoaded() {
                        // googleMap.setPadding(left, top, right, bottom)
                        map.setPadding(20, 140, 20, 20);
                        drawOnMap(map, 0);
                        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                MapDialogFragment mapDialogFragment = new MapDialogFragment();
                                mapDialogFragment.show(ft, "map_dialog_fragment");
                            }
                        });
                        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                MapDialogFragment mapDialogFragment = new MapDialogFragment();
                                mapDialogFragment.show(ft, "map_dialog_fragment");
                                return true;
                            }
                        });
                    }
                });

            }
        });

    }*/


   /* public HashMap<Marker, Long> drawOnMap(GoogleMap map, float unzoom) {
        Log.d(TAG, "drawOnMap()");
<<<<<<< HEAD
        if (mSelectedPupsList.size() > 0 && map != null) {
=======
        if (this.event == null) {
            Log.e(TAG, "drawOnMap: event == null");
            return null;
        }
        if (pubs.size() > 0 && map != null) {
>>>>>>> master
            map.clear();
            HashMap<Marker, Long> markerLongHashMap = new HashMap<>();
            ArrayList<LatLng> latLngs = new ArrayList<>();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < mSelectedPupsList.size(); i++) {
                PubMini pub = mSelectedPupsList.get(i);

                Marker marker = map.addMarker(new MarkerOptions()
                        .position(pub.getLatLng())
                        .draggable(false)
                        .title(pub.getName())
                        .icon(getCustomMarkerIcon(i+1))
                        .snippet(pub.getTimeSlotTimeString()));
                pub.setMarker(marker);
                latLngs.add(pub.getLatLng());
                builder.include(pub.getLatLng());

                markerLongHashMap.put(marker, pub.getId());
            }
            map.addPolyline(new PolylineOptions()
                    .addAll(latLngs)
                    .color(Color.BLUE)
                    .width(10)
                    .clickable(false));

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(map.getCameraPosition().target, map.getCameraPosition().zoom-unzoom));
            return markerLongHashMap;
        }
        return null;
    }

    public long getPubIdFromMarker(Marker marker) throws ArrayIndexOutOfBoundsException{
        for (PubMini pub : mSelectedPupsList) {
            if(marker == pub.getMarker()) return pub.getId();
        }
        throw new ArrayIndexOutOfBoundsException("Can't find pub");
    }

    private BitmapDescriptor getCustomMarkerIcon(int i) {
        Bitmap.Config conf = ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
        Canvas canvas = new Canvas(bmp);

// modify canvas
        Bitmap icon = drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_location_on_24dp), 80);
        if (icon == null) Log.e(TAG, "icon is null");
        canvas.drawBitmap(icon, 0,0, new Paint());

        // paint defines the text color, stroke width and size
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(36);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);

        String text = String.valueOf(i);
        Rect r = new Rect();
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x =  cWidth / 2f - r.width() / 2f - r.left - 1;
        float y = cHeight / 2f + r.height() / 2f - r.bottom - 8;

        canvas.drawText(text, x, y, paint);

        return BitmapDescriptorFactory.fromBitmap(bmp);
    }

    public static Bitmap drawableToBitmap (Drawable drawable, int size) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

//        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
*/
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

            if (!mIsTheTitleVisible) {
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
            if (mIsTheTitleContainerVisible) {
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

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }


    /*

        Load functions and classes

     */

    private void getEvent(long id) {

        try {
            this.event = new EventDbHelper().getEvent(id);

            getPubMinis(this.event.getPubIds(), this.event.getTimeSlotList());
            getParticipants(this.event.getParticipantIds());
            getOwner(this.event.getOwnerId());
        } catch (DatabaseException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Can't find data for Event with ID " + id, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "getEvent: id " + id + ", event == null");
        }
    }

    private void getPubMinis (ArrayList<Long> ids, ArrayList<TimeSlot> slots) {
        ArrayList<Long> mIds = new ArrayList<>(ids);
        for (TimeSlot t : slots) {
            try {
                long id = t.getPubId();
                this.pubs.add(new PubMini(new PubDbHelper().getPub(id), t));
                mIds.remove(id);
            } catch (DatabaseException de) {
                de.printStackTrace();
            }
        }

        Collections.sort(this.pubs, new PubMiniComparator());

        for (Long id : mIds) {

            try {
                this.pubs.add(new PubMini(new PubDbHelper().getPub(id), null));
            } catch (DatabaseException de) {
                de.printStackTrace();
            }
        }
    }

    private void getParticipants(ArrayList<Long> ids) {
        for (long id : ids) {
            try {
                this.participants.add(new PersonMini(new PersonDbHelper().getPerson(id)));
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }


    private void getOwner (long id) {
        try {
            owner = new PersonMini(new PersonDbHelper().getPerson(id));
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

}
