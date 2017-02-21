package com.ws1617.iosl.pubcrawl20.DisplayEvents;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper;
import com.ws1617.iosl.pubcrawl20.Database.resetDbTask;
import com.ws1617.iosl.pubcrawl20.Details.DetailsCallback;
import com.ws1617.iosl.pubcrawl20.Details.EventDetailsActivity;
import com.ws1617.iosl.pubcrawl20.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaspe on 30. 11. 2016.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<EventMini> eventList;
    private final static String TAG = "EventAdapter";
    private GoogleMap map;
    private final Context context;
    private long userId = -1;


    public class EventViewHolder extends RecyclerView.ViewHolder  {
        public TextView name, description, date;
        public RelativeLayout relativeLayout;
        public Button btn_details;

        public EventViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.event_name);
            this.description = (TextView) itemView.findViewById(R.id.event_description);
            this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.event_layout);
            this.btn_details = (Button) itemView.findViewById(R.id.event_details_btn);
            this.date = (TextView) itemView.findViewById(R.id.event_date);
        }

    }

    public EventAdapter(List<EventMini> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_user), Context.MODE_PRIVATE);
        userId = sharedPref.getLong(context.getString(R.string.user_id), -1);
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_row, parent, false);

        final EventViewHolder holder = new EventViewHolder(view);

        holder.btn_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventMini event = eventList.get(holder.getAdapterPosition());

                Intent intent = new Intent(view.getContext(), EventDetailsActivity.class);

                intent.putExtra("id", event.getEventId());
                intent.putExtra("name", event.getName());

                Log.d(TAG, "onPubItemClicked event details: " + event);

                view.getContext().startActivity(intent);
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EventMini event = eventList.get(holder.getAdapterPosition());

                for (EventMini e : eventList) {
                    if (e.getEventId() == event.getEventId()) {
                        e.setSelected(true, context);
                    } else {
                        e.setSelected(false, context);
                    }
                }
                notifyDataSetChanged();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng pos : event.getPolyline().getPoints()) {
                    builder.include(pos);
                }
                LatLngBounds bounds = builder.build();
                int padding = 40; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                try {
                    map.animateCamera(cu);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                final EventMini event = eventList.get(holder.getAdapterPosition());
                String[] options = {view.getContext().getString(R.string.join),
                        view.getContext().getString(R.string.details)};
                if (event.getParticipantIds().contains(userId)) options[0] = view.getContext().getString(R.string.leave);   // if we're already joined show leave option
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(event.getName())
                        .setItems(options, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // join
                                        try {
                                            DatabaseHelper.joinEvent(context, event.getEventId(), !event.getParticipantIds().contains(userId), new DetailsCallback() {
                                                @Override
                                                public void onSuccess() {
                                                    new resetDbTask(context, resetDbTask.EVENTS_DB + resetDbTask.PERSONS_DB).execute();
                                                    String s = "Joined event";
                                                    if (event.getParticipantIds().contains(userId)) s = "Left event";
                                                    Toast.makeText(context,
                                                            s, Toast.LENGTH_LONG)
                                                            .show();
                                                }

                                                @Override
                                                public void onFail() {
                                                    Toast.makeText(context, "Can't connect to server", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        dialog.dismiss();
                                        break;
                                    case 1:
                                        Intent intent = new Intent(view.getContext(), EventDetailsActivity.class);

                                        intent.putExtra("id", event.getEventId());
                                        intent.putExtra("name", event.getName());

                                        Log.d(TAG, "onPubItemClicked event details: " + event);
                                        dialog.dismiss();
                                        view.getContext().startActivity(intent);
                                        break;
                                    default:
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        });
                builder.create().show();

                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, final int position) {
        holder.name.setText(eventList.get(position).getName());
        holder.description.setText(eventList.get(position).getDescription());
        if (eventList.get(position).isSelected()) {
            holder.relativeLayout.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.relativeLayout.setBackgroundColor(Color.WHITE);
        }
        SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy");
        holder.date.setText(localDateFormat.format(eventList.get(position).getDate()));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
