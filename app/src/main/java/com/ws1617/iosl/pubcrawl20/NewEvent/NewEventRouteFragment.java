package com.ws1617.iosl.pubcrawl20.NewEvent;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;
import com.ws1617.iosl.pubcrawl20.DataModels.TimeSlot;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseException;
import com.ws1617.iosl.pubcrawl20.Database.PubDbHelper;
import com.ws1617.iosl.pubcrawl20.Details.RouteFragment;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewEventRouteFragment extends Fragment {

    static final String TAG = "NewEventRouteFragment";
    //Views
    View rootView;
    Button mAddPubBtn;
    SelectPubDialog mPubItemDialog;

    //Data
    List<PubMiniModel> mSelectedPupsList = new ArrayList<>();
    ArrayList<Long> mSelectedPubsID;
    ArrayList<TimeSlot> mTimeSlotList;
    UpdatePubList updatePubListInterface;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_new_event_route, container, false);

        initRouteFragment();

        mAddPubBtn = (Button) rootView.findViewById(R.id.event_new_add_pub);
        mAddPubBtn.setOnClickListener(addPubClickListener);

        //initView();
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (oldEvent != null) {
            List<Long> pubsIds = oldEvent.getPubIds();
            List<TimeSlot> timeSlots = oldEvent.getTimeSlotList();

            for (int i = 0 ; i < pubsIds.size() ;i++) {
                Pub newPub = null;
                try {
                    newPub = new PubDbHelper().getPub(pubsIds.get(i));
                    PubMiniModel miniPup = new PubMiniModel(newPub,timeSlots.get(i));
                    mSelectedPupsList.add(miniPup);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
         }
    }

    Event oldEvent;

    public void setOldEvent(Event oldEvent) {
        this.oldEvent = oldEvent;
    }

    void initRouteFragment() {

        RouteFragment routeFragment = RouteFragment.newInstance(RouteFragment.DIALOG_STATUS.EDIT_MODE);
        try {
            updatePubListInterface = routeFragment;
        } catch (ClassCastException ex) {

            throw new ClassCastException(
                    routeFragment.toString() + " must implement OnPlayerSelectionSetListener");
        }

        routeFragment.setListOfPubs(mSelectedPupsList);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.event_new_selected_pub_list, routeFragment, "Route").commit();

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible && mSelectedPupsList.size() == 0) {
            Toast toast = Toast.makeText(getContext(), "Click on + to add Pubs", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            if(oldEvent != null)
                updatePubListInterface.onNewValues();

        }
    }


    View.OnClickListener addPubClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mPubItemDialog = new SelectPubDialog();
            mPubItemDialog.setPubListListener(onSelectPubDialogDismissed);
            mPubItemDialog.show(getChildFragmentManager(), TAG + "pub");
        }
    };

    SelectPubDialog.OnSelectPubDialogDismissed onSelectPubDialogDismissed = new SelectPubDialog.OnSelectPubDialogDismissed() {
        @Override
        public void addPubToList(PubMiniModel newPub) {
            mSelectedPupsList.add(newPub);
            updatePubListInterface.onNewValues();

        }
    };

    public Event collectPubListInfo(Event mEvent) {
        mSelectedPubsID = new ArrayList<>();
        mTimeSlotList = new ArrayList<>();
        for (PubMiniModel selectedPub : mSelectedPupsList) {
            mSelectedPubsID.add(selectedPub.getId());
            mTimeSlotList.add(selectedPub.getTimeSlot());
        }
        mEvent.setPubIds(mSelectedPubsID);
        mEvent.setTimeSlotList(mTimeSlotList);
        setLangLong(mEvent);
        return mEvent;
    }


    private void setLangLong(Event mEvent) {
        if (mSelectedPupsList.size() == 0) return;
        LatLng maxLatLng = mSelectedPupsList.get(0).getLatLng();
        LatLng minLatLng = mSelectedPupsList.get(0).getLatLng();

        for (PubMiniModel selectedPub : mSelectedPupsList) {
            LatLng currentLatLng = selectedPub.getLatLng();
            Log.d(TAG, " currentLatLng " + currentLatLng);
            if (currentLatLng.latitude > maxLatLng.latitude && currentLatLng.longitude > maxLatLng.longitude)
                maxLatLng = currentLatLng;
            else if (currentLatLng.latitude < maxLatLng.latitude && currentLatLng.longitude < maxLatLng.longitude)
                minLatLng = currentLatLng;
        }

        mEvent.setMaxLatLng(maxLatLng);
        mEvent.setMinLatLng(minLatLng);

        Log.d(TAG, " maxLatLng " + maxLatLng + " minLatLng " + minLatLng);
    }


    public interface UpdatePubList {
        void onNewValues();
    }
}
