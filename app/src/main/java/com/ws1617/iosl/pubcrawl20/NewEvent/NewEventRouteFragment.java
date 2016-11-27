package com.ws1617.iosl.pubcrawl20.NewEvent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ws1617.iosl.pubcrawl20.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewEventRouteFragment extends Fragment {


    public NewEventRouteFragment() {
        // Required empty public constructor


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_event_route, container, false);
    }


    public Event.EventPubInfo CollectPublInfo(){
        Event.EventPubInfo
    }

}
