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
import com.google.android.gms.maps.GoogleMapOptions;
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

public class EventAdapter extends RecyclerView.Adapter<EventViewHolder>{
    final private static String TAG = "Current.EventAdapter";
    final private Context mContext;
    private List<EventMini> eventMiniList;
    final private DisplayMetrics metrics;
    final private FragmentManager fragmentManager;
    private ArrayList<MapView> mapViews = new ArrayList<>();


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

        EventViewHolder ev = new EventViewHolder(parent.getContext(), itemView, fragmentManager);
        mapViews.add(ev.mapView);
        return ev;
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        EventMini event = eventMiniList.get(position);
        holder.itemView.setTag(event);
        holder.eventString = String.format(" Event %s id %d", event.getName(), event.getEventId());
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
        holder.mapView.onResume();
        holder.adapter.notifyDataSetChanged();
        try {
            holder.drawOnMap(holder.map, 0, event.getPubs());
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    //Recycling GoogleMap for list item
    @Override
    public void onViewRecycled(EventViewHolder holder)
    {
        Log.d(TAG, "onViewRecycled" + holder.eventString);
//        if (holder.map != null)
//        {
//            Log.d(TAG, "onViewRecycled: holder.map exists");
//            holder.map.clear();
//            holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
//        }
    }

    public ArrayList<MapView> getMapViews() {
        return mapViews;
    }

    @Override
    public void onViewAttachedToWindow(EventViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(TAG, "onViewAttachedToWindow" + holder.eventString);
//        if (holder.mapView == null) {
//            holder.mapView = (MapView) holder.view.findViewById(R.id.current_route_mapview);
//        }
//        holder.mapView.onCreate(null);
//        MapsInitializer.initialize(mContext);
//        holder.mapView.onStart();
        holder.mapView.onResume();
        try {
            holder.drawOnMap(holder.map, 0, holder.pubs);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
//        holder.mapView.forceLayout();
//        holder.mapView.getMapAsync(holder);
    }

    @Override
    public void onViewDetachedFromWindow(EventViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.d(TAG, "onViewDetachedFromWindow" + holder.eventString);
    }

    @Override
    public int getItemCount() {
        return eventMiniList.size();
    }
}
