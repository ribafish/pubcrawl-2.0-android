package com.ws1617.iosl.pubcrawl20.NewEvent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PersonMini;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMini;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMiniComparator;
import com.ws1617.iosl.pubcrawl20.Details.RouteFragment;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.SelectedPupListAdapter;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewEventRouteFragment extends Fragment implements SelectedPupListAdapter.OnPubItemClickListener {

    static final String TAG = "NewEventRouteFragment";
    //Views
    View rootView;
    Button mAddPubBtn;
    RecyclerView mSelectedPupListView;
    SelectedPupListAdapter adapter;
    PubListDialog mPubItemDialog;

    //Data
    List<PubMini> mSelectedPupsList;

    public NewEventRouteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_new_event_route, container, false);

        initRouteFragment();

        //initView();
        return rootView;
    }


    private Event event;
    private ArrayList<PubMini> pubs = new ArrayList<>();
    private ArrayList<PersonMini> participants = new ArrayList<>();
    private PersonMini owner;

    void initRouteFragment() {
        // this list should come from the DB and from outside the fragment
        // the fragment it self get the data from outsource

        //View mode
        initVewMode();
        //Edit mode
        //TODO

        RouteFragment routeFragment = RouteFragment.newInstance();
        routeFragment.setListOfPubs(pubs);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.test_place_holder, routeFragment, "Route").commit();

    }


    void initVewMode() {
        long id = getActivity().getIntent().getLongExtra("id", -1);

        getEvent(id);
    }


    // TODO: Change to get it from database when it will be ready
    private void getEvent(long id) {
        ArrayList<Long> dummyIds = new ArrayList<>();
        dummyIds.add((long) 0);
        dummyIds.add((long) 1);
        dummyIds.add((long) 2);
        dummyIds.add((long) 3);
        dummyIds.add((long) 4);
        dummyIds.add((long) 5);
        dummyIds.add((long) 6);


        this.event = new Event(
                "Dummy event " + id,
                new Date(116, 11, 31, 20, 0, 0),
                getResources().getString(R.string.lorem),
                true,
                dummyIds,
                12,
                dummyIds
        );

        this.event.setEventId(id);

        ArrayList<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(new TimeSlot(
                1,
                new Date(116, 11, 31, 21, 0, 0),
                new Date(116, 11, 31, 22, 0, 0)
        ));
        timeSlots.add(new TimeSlot(
                0,
                new Date(116, 11, 31, 22, 0, 0),
                new Date(116, 11, 31, 23, 0, 0)
        ));
        timeSlots.add(new TimeSlot(
                2,
                new Date(116, 11, 31, 20, 0, 0),
                new Date(116, 11, 31, 21, 0, 0)
        ));
        this.event.setTimeSlotList(timeSlots);
        getPubMinis(this.event.getPubIds(), this.event.getTimeSlotList());
        getParticipants(this.event.getParticipantIds());
        getOwner(this.event.getOwnerId());
    }

    // TODO: Change to get it from database when it will be ready
    private void getPubMinis(ArrayList<Long> ids, ArrayList<TimeSlot> slots) {
        ArrayList<Long> mIds = new ArrayList<>(ids);
        for (TimeSlot t : slots) {
            long id = t.getPubId();
            this.pubs.add(new PubMini("Dummy Pub " + id, t, id, new LatLng(52.5 + Math.random() * 0.1, 13.35 + Math.random() * 0.1)));
            mIds.remove(id);
        }

        Collections.sort(this.pubs, new PubMiniComparator());

        for (Long id : mIds) {
            this.pubs.add(new PubMini("Dummy Pub " + id, null, id, new LatLng(52.5 + Math.random() * 0.1, 13.35 + Math.random() * 0.1)));
        }
    }

    private void getParticipants(ArrayList<Long> ids) {
        for (long id : ids) {
            this.participants.add(new PersonMini("Person " + id, id));
        }
        for (int i = ids.size(); i < 20; i++) {
            this.participants.add(new PersonMini("Person " + i, i));
        }
    }

    // TODO: when databse is ready change this to get it from database
    private void getOwner(long id) {
        owner = new PersonMini("Jack Black", id);
    }


    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible && pubs.size() == 0) {
            Toast toast = Toast.makeText(getContext(), "Click on + to add Pubs", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

   /* private void initView() {
        // init pups list
        mSelectedPupsList = new ArrayList<>();
        // mSelectedPupsList.add( new Pub(1,"Date",new LatLng(1,1),1));

        mAddPubBtn = (Button) rootView.findViewById(R.id.event_new_add_pub);
        mAddPubBtn.setOnClickListener(addPubClickListener);
        //selected list
        mSelectedPupListView = (RecyclerView) rootView.findViewById(R.id.event_new_selected_pub_list);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mSelectedPupListView.setLayoutManager(linearLayoutManager);

        adapter = new SelectedPupListAdapter(mSelectedPupsList, this);
        mSelectedPupListView.setAdapter(adapter);

    }*/


    View.OnClickListener addPubClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mPubItemDialog = new PubListDialog();
            mPubItemDialog.setPubListListener(onSelectPubDialogDismissed);
            mPubItemDialog.show(getChildFragmentManager(), TAG + "pub");
        }
    };


    PubListDialog.OnSelectPubDialogDismissed onSelectPubDialogDismissed = new PubListDialog.OnSelectPubDialogDismissed() {
        @Override
        public void addPubToList(PubMini newPub) {
            mSelectedPupsList.add(newPub);
            adapter.notifyItemChanged(mSelectedPupsList.size());
        }
    };

    @Override
    public void onPubItemClicked(int itemPosition) {
        if (mPubItemDialog != null) {
            mPubItemDialog.setPubListListener(onSelectPubDialogDismissed);
            mPubItemDialog.showSelectedPub(mSelectedPupsList.get(itemPosition));
            mPubItemDialog.show(getChildFragmentManager(), TAG + "pub");
        }
    }
}
