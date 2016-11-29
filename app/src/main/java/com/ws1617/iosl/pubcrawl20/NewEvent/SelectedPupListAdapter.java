package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.Models.Pub;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.List;

/**
 * Created by Haneen on 11/29/2016.
 */

public class SelectedPupListAdapter extends RecyclerView.Adapter<SelectedPupListAdapter.pupViewHolder> {
    List<Pub> selectedPups;


    public SelectedPupListAdapter(List<Pub> selectedPups) {
        this.selectedPups = selectedPups;
    }

    @Override
    public pupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_selected_pup_item, parent, false);

        pupViewHolder pupViewHolder = new pupViewHolder(rootView);
        return pupViewHolder;
    }

    @Override
    public void onBindViewHolder(pupViewHolder holder, int position) {
        Pub pub = selectedPups.get(position);
        holder.pubTime.setText(pub.getPubName());
        holder.pubName.setText(pub.getPubName());
    }

    @Override
    public int getItemCount() {
        return selectedPups.size();
    }

    public class pupViewHolder extends RecyclerView.ViewHolder {

        TextView pubName;
        TextView pubTime;

        public pupViewHolder(View itemView) {
            super(itemView);
            pubName = (TextView) itemView.findViewById(R.id.pub_item_title);
            pubTime = (TextView) itemView.findViewById(R.id.pub_item_time);
        }
    }
}
