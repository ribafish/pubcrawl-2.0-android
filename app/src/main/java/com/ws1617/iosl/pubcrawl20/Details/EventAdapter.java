package com.ws1617.iosl.pubcrawl20.Details;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.EventMini;
import com.ws1617.iosl.pubcrawl20.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Gasper Kojek on 12. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class EventAdapter extends BaseAdapter {
    private ArrayList<EventMini> events;
    private LayoutInflater inflater;

    public EventAdapter(Context context, ArrayList<EventMini> events) {
        this.events = events;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View row = inflater.inflate(R.layout.popup_list_row_events, parent, false);

        TextView number = (TextView) row.findViewById(R.id.event_number);
        TextView name = (TextView) row.findViewById(R.id.event_name);
        TextView date = (TextView) row.findViewById(R.id.event_date);

        EventMini eventMini = events.get(i);

        number.setText(String.valueOf(i+1) + ".");
        name.setText(eventMini.getName());

        SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy");
        date.setText(localDateFormat.format(eventMini.getDate()));

        return row;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int i) {
        return events.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}
