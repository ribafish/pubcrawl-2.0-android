package com.ws1617.iosl.pubcrawl20.NewEvent.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMini;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.List;

/**
 * Created by Haneen on 11/29/2016.
 */

public class SelectedPupListAdapter extends RecyclerView.Adapter<SelectedPupListAdapter.pupViewHolder> {
    List<PubMini> selectedPups;
    OnPubItemClickListener mOnPubItemClickListener;


    public SelectedPupListAdapter(List<PubMini> selectedPups, OnPubItemClickListener listener) {
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
        PubMini pub = selectedPups.get(position);
        holder.setPubData(pub);

    }

    @Override
    public int getItemCount() {
        return selectedPups.size();
    }

    public class pupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView pubName;
        TextView pubTime;

        public pupViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            pubName = (TextView) itemView.findViewById(R.id.selected_pub_item_title);
            pubTime = (TextView) itemView.findViewById(R.id.selected_pub_item_time);
        }

        public void setPubData(PubMini pub) {
            pubName.setText(pub.getName());
            pubTime.setText(pub.getTimeSlotTimeString());
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