package com.ws1617.iosl.pubcrawl20.CrawlSearch;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.R;

import java.util.List;

/**
 * Created by gaspe on 30. 11. 2016.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> eventList;

    public class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView name, description;
        public RelativeLayout relativeLayout;

        public EventViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.event_name);
            this.description = (TextView) itemView.findViewById(R.id.event_description);
            this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.event_layout);
        }
    }

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_list_row, parent, false);

        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
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
