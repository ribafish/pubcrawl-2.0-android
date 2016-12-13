package com.ws1617.iosl.pubcrawl20.Details;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMini;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;


/**
 * Created by Gasper Kojek on 11. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class PubAdapter extends BaseAdapter {
    private ArrayList<PubMini> pubs;
    private LayoutInflater inflater;

    public PubAdapter(Context context, ArrayList<PubMini> pubs) {
        this.pubs = pubs;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View row = inflater.inflate(R.layout.pub_list_row, parent, false);

        TextView number = (TextView) row.findViewById(R.id.pub_number);
        TextView name = (TextView) row.findViewById(R.id.pub_name);
        TextView time = (TextView) row.findViewById(R.id.pub_time);

        PubMini pub = pubs.get(i);

        number.setText(String.valueOf(i+1) + ".");
        name.setText(pub.getName());
        time.setText(pub.getTimeSlotTimeString());

        return row;
    }

    @Override
    public int getCount() {
        return pubs.size();
    }

    @Override
    public Object getItem(int i) {
        return pubs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}
