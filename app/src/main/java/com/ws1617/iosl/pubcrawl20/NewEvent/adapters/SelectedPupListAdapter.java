package com.ws1617.iosl.pubcrawl20.NewEvent.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.List;

/**
 * Created by Haneen on 11/29/2016.
 */

public class SelectedPupListAdapter extends RecyclerView.Adapter<SelectedPupListAdapter.pupViewHolder> {
    List<PubMiniModel> selectedPups;
    OnPubItemClickListener mOnPubItemClickListener;


    public SelectedPupListAdapter(List<PubMiniModel> selectedPups, OnPubItemClickListener listener) {
        this.selectedPups = selectedPups;
        mOnPubItemClickListener = listener;
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
        PubMiniModel pub = selectedPups.get(position);
        holder.setPubData(pub);

    }

    @Override
    public int getItemCount() {
        return selectedPups.size();
    }

    public class pupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView pubName;
        TextView startTime,endTime;

        public pupViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            pubName = (TextView) itemView.findViewById(R.id.selected_pub_item_title);
            startTime = (TextView) itemView.findViewById(R.id.txt_time_from);
            endTime = (TextView) itemView.findViewById(R.id.txt_time_to);
        }

        public void setPubData(PubMiniModel pub) {
            pubName.setText(pub.getName());
            startTime.setText(pub.getTimeSlot().getStartTimeAsString());
            endTime.setText(pub.getTimeSlot().getEndTimeAsString());
        }

        @Override
        public void onClick(View view) {
            mOnPubItemClickListener.onPubItemClicked(getAdapterPosition());
        }
    }


    public interface OnPubItemClickListener {
        void onPubItemClicked(int itemPosition);
    }
}