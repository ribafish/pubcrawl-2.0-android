package com.ws1617.iosl.pubcrawl20.NewEvent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;
import com.ws1617.iosl.pubcrawl20.Details.RouteFragment;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewEventRouteFragment extends Fragment  {

    static final String TAG = "NewEventRouteFragment";
    //Views
    View rootView;
    Button mAddPubBtn;
    SelectPubDialog mPubItemDialog;

    //Data
    List<PubMiniModel> mSelectedPupsList = new ArrayList<>();

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
            updatePubListInterface.onNewPub(newPub);
            //TODO init the map as well
        }
    };



    public interface UpdatePubList {
        void onNewPub(PubMiniModel pub);
    }
}
