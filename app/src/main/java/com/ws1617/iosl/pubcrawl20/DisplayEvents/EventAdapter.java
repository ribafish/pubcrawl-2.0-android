package com.ws1617.iosl.pubcrawl20.DisplayEvents;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.DisplayEvents.Models.Event;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.List;

/**
 * Created by gaspe on 30. 11. 2016.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> eventList;
    private final static String TAG = "EventAdapter";

    public class EventViewHolder extends RecyclerView.ViewHolder  {
        public TextView name, description;
        public RelativeLayout relativeLayout;
        public Button btn_details;

        public EventViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.event_name);
            this.description = (TextView) itemView.findViewById(R.id.event_description);
            this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.event_layout);
            this.btn_details = (Button) itemView.findViewById(R.id.event_details_btn);
        }

    }

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_events, parent, false);

        final EventViewHolder holder = new EventViewHolder(view);

        holder.btn_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = eventList.get(holder.getAdapterPosition());

                Intent intent = new Intent(view.getContext(), EventDetailsActivity.class);

                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("name", event.getEventName());

                Log.d(TAG, "onClick event details: " + event);

                view.getContext().startActivity(intent);
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = eventList.get(holder.getAdapterPosition());

                for (Event e : eventList) {
                    if (e.getEventId() == event.getEventId()) {
                        e.setSelected(true);

                    } else {
                        e.setSelected(false);
                    }
                }
                notifyDataSetChanged();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, final int position) {
        holder.name.setText(eventList.get(position).getEventName());
        holder.description.setText(eventList.get(position).getDescription());
        if (eventList.get(position).isSelected()) {
            holder.relativeLayout.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.relativeLayout.setBackgroundColor(Color.WHITE);
        }


    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
