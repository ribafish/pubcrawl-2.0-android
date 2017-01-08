package com.ws1617.iosl.pubcrawl20.Details;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMini;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMiniComparator;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.SelectedPupListAdapter;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by Haneen on 15/12/2016.
 * This fragment has two parts,  a list of Pubs and the map that shows these pubs
 * inputs : pubList
 * <p>
 * <p>
 * ??
 * The fragment has two modes: View mode : used to display an already selected list of pubs
 * edit mode : used in event creation, user can select pubs
 */

public class RouteFragment extends DialogFragment {
    String TAG = "RouteFragment";
    //View
    View mRootView;

    //MapView
    private SupportMapFragment mapFragment;
    private GoogleMap map;

    //PubsListView
    RecyclerView mPubsListView;
    SelectedPupListAdapter adapter;
    //Data
    List<PubMini> pubs;

    public static RouteFragment newInstance() {
        RouteFragment routeFragment = new RouteFragment();
        Bundle bundle = new Bundle();
        routeFragment.setArguments(bundle);
        return routeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_route, container, false);

        initMapView();
        initPubsListView();
        return mRootView;


    }


    public void setListOfPubs(List<PubMini> pubs) {
        this.pubs = pubs;
    }


    // initViews
    private void initMapView() {

        setupMap();
    }

    private void initPubsListView() {
        if (pubs == null) return;
        mPubsListView = (RecyclerView) mRootView.findViewById(R.id.route_fragment_pubListView);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mPubsListView.setLayoutManager(linearLayoutManager);

        adapter = new SelectedPupListAdapter(pubs, null);
        mPubsListView.setAdapter(adapter);

    }

    // TODO: Change to get it from database when it will be ready
    private void getPubMinis(ArrayList<Long> ids, ArrayList<TimeSlot> slots) {
        ArrayList<Long> mIds = new ArrayList<>(ids);
        for (TimeSlot t : slots) {
            long id = t.getPubId();
            this.pubs.add(new PubMini("Dummy Pub " + id, t, id, new LatLng(52.5 + Math.random() * 0.1, 13.35 + Math.random() * 0.1)));
            mIds.remove(id);
        }

        Collections.sort(this.pubs, new PubMiniComparator());

        for (Long id : mIds) {
            this.pubs.add(new PubMini("Dummy Pub " + id, null, id, new LatLng(52.5 + Math.random() * 0.1, 13.35 + Math.random() * 0.1)));
        }
    }

    /*

        Map functions

     */

    private void setupMap() {
        if (mRootView == null) return;
        Log.d(TAG, "setupMap");
        FrameLayout mapContainer = (FrameLayout) mRootView.findViewById(R.id.event_details_map_map);
//        mapContainer.getLayoutParams().height = mapContainer.getLayoutParams().width;

        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .scrollGesturesEnabled(false)
                .zoomGesturesEnabled(true)
                .liteMode(true);
        mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.route_fragment_map, mapFragment).commit();
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
                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                MapDialogFragment mapDialogFragment = new MapDialogFragment();
                                mapDialogFragment.show(ft, "map_dialog_fragment");
                            }
                        });
                        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                MapDialogFragment mapDialogFragment = new MapDialogFragment();
                                mapDialogFragment.show(ft, "map_dialog_fragment");
                                return true;
                            }
                        });
                    }
                });

            }
        });

    }


    /**
     * Draws the event on the supplied map
     *
     * @param map    map to draw the event on
     * @param unzoom float amount to un-zoom the map
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

                Marker marker = map.addMarker(new MarkerOptions()
                        .position(pub.getLatLng())
                        .draggable(false)
                        .title(pub.getName())
                        .icon(getCustomMarkerIcon(i + 1))
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
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(map.getCameraPosition().target, map.getCameraPosition().zoom - unzoom));
            return markerLongHashMap;
        }
        return null;
    }


    private BitmapDescriptor getCustomMarkerIcon(int i) {
        Bitmap.Config conf = ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
        Canvas canvas = new Canvas(bmp);

        // modify canvas
        Bitmap icon = drawableToBitmap(getContext().getResources().getDrawable(R.drawable.ic_location_on_24dp), 80);
        if (icon == null) Log.e(TAG, "icon is null");
        canvas.drawBitmap(icon, 0, 0, new Paint());

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
        float x = cWidth / 2f - r.width() / 2f - r.left - 1;
        float y = cHeight / 2f + r.height() / 2f - r.bottom - 8;

        canvas.drawText(text, x, y, paint);

        return BitmapDescriptorFactory.fromBitmap(bmp);
    }

    public static Bitmap drawableToBitmap(Drawable drawable, int size) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

//        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
