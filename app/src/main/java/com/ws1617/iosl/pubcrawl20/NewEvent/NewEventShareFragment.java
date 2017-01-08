package com.ws1617.iosl.pubcrawl20.newEvent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ws1617.iosl.pubcrawl20.newEvent.adapters.ShareEventAdapter;
import com.ws1617.iosl.pubcrawl20.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewEventShareFragment extends Fragment {

    RecyclerView mShareEventView;
    ShareEventAdapter mAdapter;

    public NewEventShareFragment() {
        // Required empty public constructor
    }


    public static NewEventShareFragment newInstance() {
        Bundle bundle = new Bundle();
        NewEventShareFragment newEventShareFragment = new NewEventShareFragment();
        newEventShareFragment.setArguments(bundle);
        return newEventShareFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event_share, container, false);

       /* mShareEventView = (RecyclerView) view.findViewById(R.id.fragment_new_event_share);
        mAdapter = new ShareEventAdapter();
      //  mShareEventView.setAdapter(mAdapter);
*/
        return view;

    }


}
