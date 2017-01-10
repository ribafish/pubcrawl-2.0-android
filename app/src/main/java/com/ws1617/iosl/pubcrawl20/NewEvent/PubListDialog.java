package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.Details.MiniDataModels.PubMini;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Haneen on 11/29/2016.
 */

public class PubListDialog extends DialogFragment {

    static final String TAG = "PubListDialog";

    //Listeners
    OnSelectPubDialogDismissed onSelectPubDialogDismissed;

    //Views
    Button mTimeFrom, mTimeTo, mDoneBtn;
    Spinner mPubsListView;
    TextView mPubName, mPubSize;
    SupportMapFragment mapFragment;
    View mRootView;

    //Data
    PubMini selectedPub;
    List<Pub> pubsList;
    List<String> pubsListString;
    GoogleMap mGoogleMap;


    public PubListDialog() {
    }

    public void setPubListListener(OnSelectPubDialogDismissed onSelectPubDialogDismissed) {
        this.onSelectPubDialogDismissed = onSelectPubDialogDismissed;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.view_pup_list, null);
        initView();
        initMapView();
        return mRootView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void showSelectedPub(PubMini pub) {

        // there is two lists. one is the selected list and the other is the full list
        // the itemPosition parameter that we get here is the position of the clicked item in the selected list
        // but the position that we need to pass to the dialog is the position of this item in the full list
        int position = pubsList.indexOf(pub);

        if (position >= pubsList.size()) return;
        mPubsListView.setSelection(position);
        // selectedPub =pub;
        //selectItem();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


//TODO check if this should be uncommented again
    @Override
    public void onDestroy() {
        super.onDestroy();
       /* if (null != getChildFragmentManager() && mapFragment!= null)
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .commit();*/
    }

    private void initMapView() {
         mapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.pub_dialog_map, mapFragment, TAG).commit();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
               mGoogleMap = googleMap;
                LatLng sydney = new LatLng(-34, 151);
                mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            }
        });
    }


    private void initView() {

        mPubName = (TextView) mRootView.findViewById(R.id.pub_dialog_pub_name);
        mPubSize = (TextView) mRootView.findViewById(R.id.pub_dialog_pub_size);

        mDoneBtn = (Button) mRootView.findViewById(R.id.pub_dialog_pub_done);
        mDoneBtn.setOnClickListener(mDoneBtnClickedListener);

        mTimeFrom = (Button) mRootView.findViewById(R.id.pub_dialog_visit_from_date_picker);
        mTimeFrom.setOnClickListener(mDoneBtnClickedListener);

        mTimeTo = (Button) mRootView.findViewById(R.id.pub_dialog_visit_to_date_picker);
        mTimeTo.setOnClickListener(mDoneBtnClickedListener);

        mPubsListView = (Spinner) mRootView.findViewById(R.id.pub_dialog_pubs_list);
        mPubsListView.setOnItemSelectedListener(pubListOnItemSelectedListener);

        //TODO should be fetched from the DB
        initPubList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, pubsListString);
        mPubsListView.setAdapter(adapter);




    }

    //TODO should be fetched from the local DB
    void initPubList() {

        pubsList = new ArrayList<>();
        pubsListString = new ArrayList<>();

       Pub pub1 = new Pub(1, "pub 1", new LatLng(1, 1), 10);
        pubsList.add(pub1);
        pubsListString.add(pub1.getPubName());

        Pub pub2 = new Pub(2, "pub 2", new LatLng(1, 1), 20);
        pubsList.add(pub2);
        pubsListString.add(pub2.getPubName());

        Pub pub3 = new Pub(3, "pub 3", new LatLng(1, 1), 30);
        pubsList.add(pub3);
        pubsListString.add(pub3.getPubName());

        Pub pub4 = new Pub(4, "pub 4", new LatLng(1, 1), 40);
        pubsList.add(pub4);
        pubsListString.add(pub4.getPubName());


    }

    View.OnClickListener mDoneBtnClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.pub_dialog_pub_done: {
                    dismiss();
                    if (selectedPub != null)
                        onSelectPubDialogDismissed.addPubToList(selectedPub);

                    break;
                }
                case R.id.pub_dialog_visit_from_date_picker: {
                    new TimePickerDialog(getContext(), onTimeFromSetListener, 12, 00, true).show();
                    break;
                }
                case R.id.pub_dialog_visit_to_date_picker: {
                    new TimePickerDialog(getContext(), onTimeToSetListener, 12, 00, true).show();
                    break;
                }
            }

        }
    };


    TimePickerDialog.OnTimeSetListener onTimeFromSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            mTimeFrom.setText(hours + ":" + minutes );
        }
    };
    TimePickerDialog.OnTimeSetListener onTimeToSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            mTimeTo.setText(hours + ":" + minutes );
        }
    };


    AdapterView.OnItemSelectedListener pubListOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            selectedPub = new PubMini(pubsList.get(pos));
            selectItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Toast.makeText(getContext(), "no Pub selected ", Toast.LENGTH_LONG).show();
        }
    };

    private void selectItem() {
        mPubName.setText(this.selectedPub.getName());
        mPubSize.setText(String.valueOf(this.selectedPub.getTimeSlot()));
    }

    interface OnSelectPubDialogDismissed {
        void addPubToList(PubMini newPub);
    }
}
