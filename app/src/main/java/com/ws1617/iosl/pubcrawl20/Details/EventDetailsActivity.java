package com.ws1617.iosl.pubcrawl20.Details;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.HashMap;
import java.util.Locale;

import static android.graphics.Bitmap.Config.ARGB_8888;

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
    private CoordinatorLayout rootView;

    private Event event;
    private ArrayList<PubMini> pubs = new ArrayList<>();
    private ArrayList<PersonMini> participants = new ArrayList<>();
    private PersonMini owner;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_event_details);
        rootView = (CoordinatorLayout) findViewById(R.id.event_details_root);

        long id = getIntent().getLongExtra("id", -1);
        getEvent(id);
        setupToolbar();
        initOnClickListeners();
        setupMap();

        // TODO: change to the data load listener or smtn when database is ready
        populateFields();
    }

    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.event_details_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.event_details_appbar);
        mTitle = (TextView) findViewById(R.id.event_details_title);
        mSubtitle = (TextView) findViewById(R.id.event_details_subtitle);
        mCollapsableTitleContainer = (LinearLayout) findViewById(R.id.event_details_layout_title);
        ViewPager viewPager = (ViewPager) findViewById(R.id.event_details_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.event_details_tabDots);

        try{
            String eventName = getIntent().getStringExtra("name");

            mToolbar.setTitle(eventName);
            mTitle.setText(eventName);
            mSubtitle.setText("Loading...");  //TODO

        } catch (Exception e) { e.printStackTrace(); }


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
    }


    private void initOnClickListeners() {
        final CardView descriptionCard = (CardView) findViewById(R.id.event_details_description_card);
        descriptionCard.setClickable(true);
        final RelativeLayout descriptionLayout = (RelativeLayout) findViewById(R.id.event_details_description_layout);
        final ImageView descriptionGradient = (ImageView) findViewById(R.id.event_details_description_gradient);
        final ImageView descriptionArrow = (ImageView) findViewById(R.id.event_details_description_arrow);
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
                    descriptionArrow.setImageResource(R.drawable.ic_expand_less);
                } else {                                                        // Collapse
                    descriptionLayout.getLayoutParams().height = descriptionInitialHeight;
                    descriptionGradient.setVisibility(View.VISIBLE);
                    descriptionLayout.setPadding(padding16,padding16,padding16, 0);
                    descriptionArrow.setImageResource(R.drawable.ic_expand_more);
                }
            }
        });

    }

    private void populateFields() {
        if (owner != null) {
            ((TextView) findViewById(R.id.event_details_owner)).setText(owner.name);
            findViewById(R.id.event_details_owner).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PersonDetailsActivity.class);
                    intent.putExtra("name", owner.name);
                    intent.putExtra("id", owner.id);
                    startActivity(intent);
                }
            });
        }

        if (event != null) {
            mTitle.setText(event.getEventName());
            SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy 'at' HH:mm");
            mSubtitle.setText(localDateFormat.format(event.getDate()));
            ((TextView) findViewById(R.id.event_details_id)).setText(String.valueOf(event.getEventId()));
            ((TextView) findViewById(R.id.event_details_tracked)).setText(event.isTracked() ? "Tracked" : "Not tracked");
            ((TextView) findViewById(R.id.event_details_description)).setText(event.getDescription());


        }

    }

    /*

        Map functions

     */

    private void setupMap() {
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
            }
        });

    }

    /**
     *  Draws the event on the supplied map
      * @param map map to draw the event on
     *  @param unzoom float amount to un-zoom the map
     */
    public HashMap<Marker, Long> drawOnMap(GoogleMap map, float unzoom) {
        Log.d(TAG, "drawOnMap()");
        if (pubs.size() > 0 && map != null) {
            map.clear();
            HashMap<Marker, Long> markerLongHashMap = new HashMap<>();
            ArrayList<LatLng> latLngs = new ArrayList<>();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < pubs.size(); i++) {
                PubMini pub = pubs.get(i);

                Marker marker = pub.marker = map.addMarker(new MarkerOptions()
                        .position(pub.latLng)
                        .draggable(false)
                        .title(pub.name)
                        .icon(getCustomMarkerIcon(i))
                        .snippet(pub.getTimeSlotTimeString()));
                latLngs.add(pub.latLng);
                builder.include(pub.latLng);

                markerLongHashMap.put(marker, pub.id);
            }
            map.addPolyline(new PolylineOptions()
                    .addAll(latLngs)
                    .color(Color.BLUE)
                    .width(10)
                    .clickable(false));

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
            if (unzoom != 0)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(map.getCameraPosition().target, map.getCameraPosition().zoom-unzoom));
            return markerLongHashMap;
        }
        return null;
    }

    public long getPubIdFromMarker(Marker marker) throws ArrayIndexOutOfBoundsException{
        for (PubMini pub : pubs) {
            if(marker == pub.marker) return pub.id;
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


    /*

        Load functions and classes

     */

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
                "Dummy event " + id,
                new Date(116, 11, 31, 20, 0, 0),
                getResources().getString(R.string.lorem),
                true,
                dummyIds,
                12,
                dummyIds
        );

        ArrayList<TimeSlot> timeSlots= new ArrayList<>();
        timeSlots.add(new TimeSlot(
                1,
                new Date(116, 11, 31, 21, 0, 0),
                new Date(116, 11, 31, 22, 0, 0)
        ));
        timeSlots.add(new TimeSlot(
                0,
                new Date(116, 11, 31, 22, 0, 0),
                new Date(116, 11, 31, 23, 0, 0)
        ));
        timeSlots.add(new TimeSlot(
                2,
                new Date(116, 11, 31, 20, 0, 0),
                new Date(116, 11, 31, 21, 0, 0)
        ));
        this.event.setTimeSlotList(timeSlots);
        getPubMinis(this.event.getPubIds(), this.event.getTimeSlotList());
        getParticipants(this.event.getParticipantIds());
        getOwner(this.event.getOwnerId());
    }

    // TODO: Change to get it from database when it will be ready
    private void getPubMinis (ArrayList<Long> ids, ArrayList<TimeSlot> slots) {
        ArrayList<Long> mIds = new ArrayList<>(ids);
        for (TimeSlot t : slots) {
            long id = t.getPubId();
            this.pubs.add(new PubMini("Dummy Pub " + id, t, id, new LatLng(52.5 + Math.random()*0.1, 13.35 + Math.random()*0.1)));
            mIds.remove(id);
        }

        Collections.sort(this.pubs, new PubMiniComparator());

        for (Long id : mIds) {
            this.pubs.add(new PubMini("Dummy Pub " + id, null, id, new LatLng(52.5 + Math.random()*0.1, 13.35 + Math.random()*0.1)));
        }
    }

    private void getParticipants (ArrayList<Long> ids) {
        for (long id : ids) {
            this.participants.add(new PersonMini("Person " + id, id));
        }
    }

    // TODO: when databse is ready change this to get it from database
    private void getOwner (long id) {
        owner = new PersonMini("Jack Black", id);
    }

    class PubMini {
        String name;
        TimeSlot timeSlot;
        long id;
        LatLng latLng;
        Marker marker;

        public PubMini(String name, TimeSlot timeSlot, long id, LatLng latLng) {
            this.name = name;
            this.timeSlot = timeSlot;
            this.id = id;
            this.latLng = latLng;
        }

        public String getTimeSlotTimeString() {
            if (timeSlot != null) {
                SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
                String start = localDateFormat.format(timeSlot.getStartTime());
                String end = localDateFormat.format(timeSlot.getEndTime());
                return String.format("%s - %s", start, end);
            } else {
                return "Unknown time";
            }
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

    /*

            MapDialogFragment class

     */
}
