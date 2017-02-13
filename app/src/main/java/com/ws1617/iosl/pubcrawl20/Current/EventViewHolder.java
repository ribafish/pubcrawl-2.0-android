package com.ws1617.iosl.pubcrawl20.Current;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;
import com.ws1617.iosl.pubcrawl20.Details.MapDialogFragment;
import com.ws1617.iosl.pubcrawl20.Details.PubDetailsActivity;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.SelectedPupListAdapter;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.HashMap;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.ws1617.iosl.pubcrawl20.Details.RouteFragment.drawableToBitmap;

/**
 * Created by Gasper Kojek on 13. 02. 2017.
 * Github: https://github.com/ribafish/
 */

public class EventViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback, SelectedPupListAdapter.OnPubItemClickListener {
    final private static String TAG = "Current.EventViewHolder";
    public Context mContext;
    public TextView name, eventId, starts, participants, description;
    public ImageView gradient, arrow;
    public RelativeLayout rootLayout, descriptionLayout;
    public View view;
    public GoogleMap map;
    public MapView mapView;
    public RecyclerView pubsRecyclerView;
    SelectedPupListAdapter adapter;
    public ArrayList<PubMiniModel> pubs = new ArrayList<>();
    public String eventString = "";
    final private FragmentManager fragmentManager;


    public EventViewHolder(Context context, View view, FragmentManager fm) {
        super(view);
        this.mContext = context;
        this.fragmentManager = fm;
        Log.d(TAG, "EventViewHolder" + eventString);

        this.view = view;
        name = (TextView) view.findViewById(R.id.current_name);
        eventId = (TextView) view.findViewById(R.id.current_id);
        starts = (TextView) view.findViewById(R.id.current_starts);
        participants = (TextView) view.findViewById(R.id.current_participants);
        description = (TextView) view.findViewById(R.id.current_description);
        gradient = (ImageView) view.findViewById(R.id.current_description_gradient);
        arrow = (ImageView) view.findViewById(R.id.current_description_arrow);
        rootLayout = (RelativeLayout) view.findViewById(R.id.current_description_root_layout);
        descriptionLayout = (RelativeLayout) view.findViewById(R.id.current_description_layout);

        pubsRecyclerView = (RecyclerView) view.findViewById(R.id.current_pubs_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        pubsRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = new SelectedPupListAdapter(pubs, this);
        pubsRecyclerView.setAdapter(adapter);

        Log.d(TAG, "EventViewHolder constructor pubs.size = " + pubs.size());

        mapView = (MapView) view.findViewById(R.id.current_route_mapview);
        if (mapView != null)
        {
            mapView.onCreate(null);
            mapView.getMapAsync(this);
        } else {
            Log.e(TAG, "mapView == null" + eventString);
        }
    }


    @Override
    public void onPubItemClicked(int itemPosition) {
        Intent intent = new Intent(mContext, PubDetailsActivity.class);
        intent.putExtra("name", "Test Pub");
        intent.putExtra("id", pubs.get(itemPosition).getId());
        mContext.startActivity(intent);
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()" + eventString);
        MapsInitializer.initialize(mContext);
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {    // Sometimes wouldn't load the map
            @Override
            public void onMapLoaded() {
                Log.d(TAG, "onMapLoaded()" + eventString);

                try {
                    drawOnMap(map, 0, pubs);
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                // map.setPadding(left, top, right, bottom)
                map.setPadding(20, 140, 20, 20);
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        MapDialogFragment mapDialogFragment = new MapDialogFragment();
                        mapDialogFragment.setOnMapLoaderListener(new MapDialogFragment.OnMapLoader() {
                            @Override
                            public HashMap<Marker, Long> drawOnDialogMap(GoogleMap googleMap) {
                                try {
                                    return drawOnMap(googleMap, 0.5f, pubs);
                                } catch (Exception e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                }
                                return null;
                            }
                        });
                        mapDialogFragment.show(ft, "map_dialog_fragment");
                    }
                });
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        MapDialogFragment mapDialogFragment = new MapDialogFragment();
                        mapDialogFragment.setOnMapLoaderListener(new MapDialogFragment.OnMapLoader() {
                            @Override
                            public HashMap<Marker, Long> drawOnDialogMap(GoogleMap googleMap) {
                                try {
                                    return drawOnMap(googleMap, 0.5f, pubs);
                                } catch (Exception e) {
                                    Log.e(TAG, e.getLocalizedMessage());
                                }
                                return null;
                            }
                        });
                        mapDialogFragment.show(ft, "map_dialog_fragment");
                        return true;
                    }
                });
                mapView.onResume();
            }
        });
    }

    /**
     * Draws the event on the supplied map
     *
     * @param map    map to draw the event on
     * @param unzoom float amount to un-zoom the map
     */
    public HashMap<Marker, Long> drawOnMap(GoogleMap map, float unzoom, ArrayList<PubMiniModel> pubs) throws Exception{
        Log.d(TAG, "drawOnMap()" + eventString);
        if (map == null) throw new Exception("map == null");

        if (pubs.size() == 0) {
            map.clear();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0),0));
            return null;
        }

        Log.d(TAG, eventString + " drawing pubs...");
        HashMap<Marker, Long> markerLongHashMap = new HashMap<>();
        ArrayList<LatLng> latLngs = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < pubs.size(); i++) {
            PubMiniModel pub = pubs.get(i);

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

    private BitmapDescriptor getCustomMarkerIcon(int i) {
        Bitmap.Config conf = ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
        Canvas canvas = new Canvas(bmp);

        // modify canvas
        Bitmap icon = drawableToBitmap(mContext.getResources().getDrawable(R.drawable.ic_location_on_24dp), 80);
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
}
