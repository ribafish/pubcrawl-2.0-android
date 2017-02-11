package com.ws1617.iosl.pubcrawl20.Current;

import android.content.Context;
import android.support.v4.app.FragmentManager;
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

import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;
import com.ws1617.iosl.pubcrawl20.Details.RouteFragment;
import com.ws1617.iosl.pubcrawl20.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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


    public class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView name, starts, participants, description;
        public ImageView gradient, arrow;
        public RelativeLayout rootLayout, descriptionLayout;
        public FrameLayout routeHolder;
        public View view;

        public EventViewHolder(View view) {
            super(view);
            this.view = view;
            name = (TextView) view.findViewById(R.id.current_name);
            starts = (TextView) view.findViewById(R.id.current_starts);
            participants = (TextView) view.findViewById(R.id.current_participants);
            description = (TextView) view.findViewById(R.id.current_description);
            gradient = (ImageView) view.findViewById(R.id.current_description_gradient);
            arrow = (ImageView) view.findViewById(R.id.current_description_arrow);
            rootLayout = (RelativeLayout) view.findViewById(R.id.current_description_root_layout);
            descriptionLayout = (RelativeLayout) view.findViewById(R.id.current_description_layout);
            routeHolder = (FrameLayout) view.findViewById(R.id.current_route_holder);
            routeHolder.setId(R.id.current_route_holder +  (int)(Math.random() * 9999));
        }
    }


    public EventAdapter(Context mContext, List<EventMini> eventMiniList, DisplayMetrics metrics, FragmentManager fragmentManager) {
        this.mContext = mContext;
        this.eventMiniList = eventMiniList;
        this.metrics = metrics;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);

        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        EventMini event = eventMiniList.get(position);
        holder.name.setText(event.getName());
        SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy 'at' HH:mm");
        holder.starts.setText(localDateFormat.format(event.getDate()));
        holder.participants.setText(event.getParticipants());
        holder.description.setText(event.getDescription());

        initDescriptionExpanding(mContext, holder);
        initRouteFragment(holder, event.getPubs());
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

    private void initRouteFragment(final EventViewHolder holder, ArrayList<PubMiniModel> pubs) {

        RouteFragment routeFragment = RouteFragment.newInstance(RouteFragment.DIALOG_STATUS.VIEW_MODE);
        fragmentManager.beginTransaction().add(holder.routeHolder.getId(), routeFragment, "Route").commit();
        routeFragment.setListOfPubs(pubs);
    }


    @Override
    public int getItemCount() {
        return eventMiniList.size();
    }
}
