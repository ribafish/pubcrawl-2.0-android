package com.ws1617.iosl.pubcrawl20.Details;

import android.content.Context;
import android.icu.text.MessagePattern;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;


/**
 * Created by Gasper Kojek on 12. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class ParticipantAdapter extends BaseAdapter {
    private ArrayList<EventDetailsActivity.PersonMini> participants;
    private LayoutInflater inflater;

    public ParticipantAdapter(Context context, ArrayList<EventDetailsActivity.PersonMini> participants) {
        this.participants = participants;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View row = inflater.inflate(R.layout.participant_list_row, parent, false);

        TextView number = (TextView) row.findViewById(R.id.participant_number);
        TextView name = (TextView) row.findViewById(R.id.participant_name);
        ImageView img = (ImageView) row.findViewById(R.id.participant_img);

        EventDetailsActivity.PersonMini p = participants.get(i);

        number.setText(String.valueOf(i+1) + ".");
        name.setText(p.name);
        if (p.image != null) img.setImageBitmap(p.image);

        return row;
    }

    @Override
    public int getCount() {
        return participants.size();
    }

    @Override
    public Object getItem(int i) {
        return participants.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}