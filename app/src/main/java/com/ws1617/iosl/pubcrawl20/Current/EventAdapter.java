package com.ws1617.iosl.pubcrawl20.Current;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.ws1617.iosl.pubcrawl20.Details.RouteFragment;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.SelectedPupListAdapter;
import com.ws1617.iosl.pubcrawl20.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.ws1617.iosl.pubcrawl20.Details.RouteFragment.drawableToBitmap;

/**
 * Created by Gasper Kojek on 11. 02. 2017.
 * Github: https://github.com/ribafish/
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder>{
    final private static String TAG = "Current.EventAdapter";
    final private Context mContext;
    private List<EventMini> eventMiniList;
    final private DisplayMetrics metrics;
    final private FragmentManager fragmentManager;
    private ArrayList<MapView> mapViews = new ArrayList<>();


    public class EventViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        public TextView name, eventId, starts, participants, description;
        public ImageView gradient, arrow;
        public RelativeLayout rootLayout, descriptionLayout;
        public View view;
        public GoogleMap map;
        public MapView mapView;
        public RecyclerView pubsRecyclerView;
        SelectedPupListAdapter adapter;
        public ArrayList<PubMiniModel> pubs = new ArrayList<>();

        public EventViewHolder(View view) {
            super(view);
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
            adapter = new SelectedPupListAdapter(pubs, new SelectedPupListAdapter.OnPubItemClickListener() {
                @Override
                public void onPubItemClicked(int itemPosition) {
                    Intent intent = new Intent(mContext, PubDetailsActivity.class);
                    intent.putExtra("name", "Test Pub");
                    intent.putExtra("id", pubs.get(itemPosition).getId());
                    mContext.startActivity(intent);
                }
            });
            pubsRecyclerView.setAdapter(adapter);

            Log.d(TAG, "EventViewHolder constructor pubs.size = " + pubs.size());

            mapView = (MapView) view.findViewById(R.id.current_route_mapview);
            if (mapView != null)
            {
                mapView.onCreate(null);
                mapView.onResume();
                mapView.getMapAsync(this);
            } else {
                Log.e(TAG, "mapView == null");
            }
        }

        @Override
        public void onMapReady(final GoogleMap googleMap) {
            Log.d(TAG, "onMapReady()");
            MapsInitializer.initialize(mContext);
            map = googleMap;
            try {
                drawOnMap(googleMap, 0, pubs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {    // Sometimes wouldn't load the map
                @Override
                public void onMapLoaded() {
                    Log.d(TAG, "onMapLoaded()");

                    // googleMap.setPadding(left, top, right, bottom)
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
                                        e.printStackTrace();
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
                                        e.printStackTrace();
                                    }
                                    return null;
                                }
                            });
                            mapDialogFragment.show(ft, "map_dialog_fragment");
                            return true;
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
        public HashMap<Marker, Long> drawOnMap(GoogleMap map, float unzoom, ArrayList<PubMiniModel> pubs) throws Exception{
            Log.d(TAG, "drawOnMap()");
            if (pubs.size() == 0) throw new Exception("pubs.size() == 0");
            if (map == null) throw new Exception("map == null");

            map.clear();
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

        private void refreshMap() {
            mapView.getMapAsync(this);
            adapter.notifyDataSetChanged();
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


    public EventAdapter(Context mContext, List<EventMini> eventMiniList,
                        DisplayMetrics metrics, FragmentManager fragmentManager) {
        this.mContext = mContext;
        this.eventMiniList = eventMiniList;
        this.metrics = metrics;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);

        EventViewHolder ev = new EventViewHolder(itemView);
        mapViews.add(ev.mapView);
        return ev;
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        EventMini event = eventMiniList.get(position);
        holder.name.setText(event.getName());
        SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy 'at' HH:mm");
        holder.eventId.setText(String.valueOf(event.getEventId()));
        holder.starts.setText(localDateFormat.format(event.getDate()));
        holder.participants.setText(String.valueOf(event.getParticipants()));
        holder.description.setText(event.getDescription());

        initDescriptionExpanding(mContext, holder);
        initRoute(holder, event);
    }



    private void initDescriptionExpanding(final Context context, final EventViewHolder holder) {
        final RelativeLayout rootLayout = holder.rootLayout;
        final RelativeLayout descriptionLayout = holder.descriptionLayout;
        final ImageView descriptionGradient = holder.gradient;
        final ImageView descriptionArrow = holder.arrow;
        final int height200 = (int) (125 * context.getResources().getDisplayMetrics().density);
        final int padding16 = (int) (16 * context.getResources().getDisplayMetrics().density + 0.5f);

        TextView description = holder.description;
        description.requestLayout();
        description.measure(0, 0);
        int textHeight = (int) (description.getMeasuredHeight() / context.getResources().getDisplayMetrics().density + 0.5f);
        int textWidth = (int) (description.getMeasuredWidth() / context.getResources().getDisplayMetrics().density + 0.5f);
        int width = (int) (metrics.widthPixels / context.getResources().getDisplayMetrics().density + 0.5f) - 40;
        int height = textHeight + textWidth / width * 18;
        Log.d(TAG, String.format("textHeight: %d; textWidth: %d; width: %d; height: %d", textHeight, textWidth, width, height));

        // 125 - 2*16dp (padding) - 8dp (title margin) - 20sp (title text size) = 140dp
        if (height > 65) {
            descriptionLayout.getLayoutParams().height = height200;
            descriptionGradient.setVisibility(View.VISIBLE);
            descriptionLayout.setPadding(padding16, padding16, padding16, 0);
            descriptionArrow.setImageResource(R.drawable.ic_expand_more);
            descriptionArrow.setVisibility(View.VISIBLE);

            rootLayout.setClickable(true);
            rootLayout.setOnClickListener(new View.OnClickListener() {
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
            rootLayout.setClickable(false);
        }
    }

    private void initRoute(final EventViewHolder holder, EventMini event) {
        holder.pubs.clear();
        holder.pubs.addAll(event.getPubs());
        holder.adapter.notifyDataSetChanged();
        try {
            holder.drawOnMap(holder.map, 0, event.getPubs());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        holder.refreshMap();
    }

    //Recycling GoogleMap for list item
    @Override
    public void onViewRecycled(EventViewHolder holder)
    {
        Log.d(TAG, "onViewRecycled");
        // Cleanup MapView here?
        if (holder.map != null)
        {
            Log.d(TAG, "holder.map != null");
            holder.map.clear();
            holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }

    public ArrayList<MapView> getMapViews() {
        return mapViews;
    }


    @Override
    public int getItemCount() {
        return eventMiniList.size();
    }
}
