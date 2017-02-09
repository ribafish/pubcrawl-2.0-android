package com.ws1617.iosl.pubcrawl20.Search;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Haneen on 09/02/2017.
 */

public class PubsListAdapter extends RecyclerView.Adapter<PubsListAdapter.PubItem> {

    static final String TAG = PubsListAdapter.class.getName();

    List<PubMini> pubsList;
    List<PubMini> pubsListCopy;

    public boolean isSearchResultAvailable() {
        return searchResultAvailable;
    }

    boolean searchResultAvailable;

    private void getPubs() {
        pubsList = new ArrayList<>();
        PubDbHelper pubDbHelper = new PubDbHelper();
        List<Pub> el = pubDbHelper.getAllPubs();
        for (Pub e : el) {
            PubMini eventMini = new PubMini(e);
            pubsList.add(eventMini);
        }
        Log.d(TAG, "eventAdapter.notifyDataSetChanged()");
        notifyDataSetChanged();
    }

    public PubsListAdapter() {
        getPubs();
        searchResultAvailable = pubsList.size() > 0 ? true:false;
        pubsListCopy = pubsList;
    }


    @Override
    public PubItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pub_list_row, parent, false);
        PubsListAdapter.PubItem pubItem = new PubsListAdapter.PubItem(view);
        return pubItem;
    }


    @Override
    public void onBindViewHolder(PubItem holder, int position) {
        holder.name.setText(pubsList.get(position).getName());
       /* holder.description.setText(pubsList.get(position).get());
        if (eventList.get(position).isSelected()) {
            holder.relativeLayout.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.relativeLayout.setBackgroundColor(Color.WHITE);
        }
        SimpleDateFormat localDateFormat = new SimpleDateFormat("E, MMM d, yyyy");
        holder.date.setText(localDateFormat.format(pubsList.get(position).getDate()));*/
    }

    @Override
    public int getItemCount() {
        return pubsList.size();
    }


    public void filter(String text, SearchActivity.SearchResultView pubSearchListener) {
        pubsList = new ArrayList<>();
        if (text.isEmpty()) {
            pubsList.addAll(pubsListCopy);
        } else {
            text = text.toLowerCase();
            for (PubMini item : pubsListCopy) {
                if (item.getName().toLowerCase().contains(text)) {
                    pubsList.add(item);
                }
            }
        }
        pubSearchListener.setSearchResultSize(pubsList.size());
        notifyDataSetChanged();
    }

    class PubItem extends RecyclerView.ViewHolder {

        TextView number;
        TextView name;
        TextView time;


        public PubItem(View itemView) {
            super(itemView);
           // number = (TextView) itemView.findViewById(R.id.pub_number);
            name = (TextView) itemView.findViewById(R.id.pub_name);
            //time = (TextView) itemView.findViewById(R.id.pub_time);

        }
    }
}
