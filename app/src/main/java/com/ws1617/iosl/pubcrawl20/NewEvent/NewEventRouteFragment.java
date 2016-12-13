package com.ws1617.iosl.pubcrawl20.NewEvent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.SelectedPupListAdapter;
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
    RecyclerView mSelectedPupListView;
    SelectedPupListAdapter adapter;

    //Data
    List<Pub> mSelectedPupsList;

    public NewEventRouteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_new_event_route, container, false);
        initView();
        return rootView;
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

    private void initView() {
        // init pups list
        mSelectedPupsList = new ArrayList<>();
        // mSelectedPupsList.add( new Pub(1,"Date",new LatLng(1,1),1));

        mAddPubBtn = (Button) rootView.findViewById(R.id.event_new_add_pub);
        mAddPubBtn.setOnClickListener(addPubClickListener);
        //selected list
        mSelectedPupListView = (RecyclerView) rootView.findViewById(R.id.event_new_selected_pub_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mSelectedPupListView.setLayoutManager(linearLayoutManager);

        adapter = new SelectedPupListAdapter(mSelectedPupsList);
        mSelectedPupListView.setAdapter(adapter);

    }


    View.OnClickListener addPubClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PubListDialog dialog = new PubListDialog();
            dialog.setPubListListener(onSelectPubDialogDismissed);
            dialog.show(getChildFragmentManager(), TAG + "pub");
        }
    };


    PubListDialog.OnSelectPubDialogDismissed onSelectPubDialogDismissed = new PubListDialog.OnSelectPubDialogDismissed() {
        @Override
        public void addPubToList(Pub newPub) {
            mSelectedPupsList.add(newPub);
            adapter.notifyItemChanged(mSelectedPupsList.size());
        }
    };
}
