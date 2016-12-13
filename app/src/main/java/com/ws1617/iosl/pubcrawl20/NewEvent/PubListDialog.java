package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ws1617.iosl.pubcrawl20.DataModels.Pub;
import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Haneen on 11/29/2016.
 */

public class PubListDialog extends DialogFragment {

    static final String TAG = "PubListDialog";

    OnSelectPubDialogDismissed onSelectPubDialogDismissed;

    //Views
    Button mDateFrom, mDateTo, mDoneBtn;
    Spinner mPubsListView;


    TextView mPubName, mPubSize;

    SupportMapFragment mapFragment;
    //Data
    Pub selectedPub;
    List<Pub> pubsList;
    List<String> pubsListString;
    //GoogleMap mGoogleMap;


    public PubListDialog() {

    }

    public void setPubListListener(OnSelectPubDialogDismissed onSelectPubDialogDismissed) {
        this.onSelectPubDialogDismissed = onSelectPubDialogDismissed;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.view_pup_list, null);
        alertBuilder.setView(view);
        initView(view);

        return alertBuilder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //initMapView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initMapView();
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        if (null != getChildFragmentManager())
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .commit();
    }*/

    private void initMapView() {
        mapFragment = new SupportMapFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.pub_dialog_map, mapFragment, TAG).commit();
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                /*mGoogleMap = googleMap;
                LatLng sydney = new LatLng(-34, 151);
                mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
            }
        });
    }


    private void initView(View view) {

        mPubName = (TextView) view.findViewById(R.id.pub_dialog_pub_name);
        mPubSize = (TextView) view.findViewById(R.id.pub_dialog_pub_size);

        mDoneBtn = (Button) view.findViewById(R.id.pub_dialog_pub_done);
        mDoneBtn.setOnClickListener(mDoneBtnClickedListener);

        mDateFrom = (Button) view.findViewById(R.id.pub_dialog_visit_from_date_picker);
        mDateFrom.setOnClickListener(mDoneBtnClickedListener);

        mDateTo = (Button) view.findViewById(R.id.pub_dialog_visit_to_date_picker);
        mDateTo.setOnClickListener(mDoneBtnClickedListener);

        mPubsListView = (Spinner) view.findViewById(R.id.pub_dialog_pubs_list);
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

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            switch (view.getId()) {
                case R.id.pub_dialog_pub_done: {
                    dismiss();
                    if (selectedPub != null)
                        onSelectPubDialogDismissed.addPubToList(selectedPub);

                    break;
                }
                case R.id.pub_dialog_visit_from_date_picker: {
                    new DatePickerDialog(getContext(), onDateFromSetListener, year, month, day).show();
                    break;
                }
                case R.id.pub_dialog_visit_to_date_picker: {
                    new DatePickerDialog(getContext(), onDatToSetListener, year, month, day).show();
                    break;
                }
            }

        }
    };

    DatePickerDialog.OnDateSetListener onDateFromSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mDateFrom.setText(dayOfMonth + "." + monthOfYear + "." + year);
        }
    };

    DatePickerDialog.OnDateSetListener onDatToSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mDateTo.setText(dayOfMonth + "." + monthOfYear + "." + year);
        }
    };


    AdapterView.OnItemSelectedListener pubListOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            selectedPub = pubsList.get(pos);
            mPubName.setText(selectedPub.getPubName());
            mPubSize.setText(String.valueOf(selectedPub.getSize()));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Toast.makeText(getContext(), "no Pub selected ", Toast.LENGTH_LONG).show();
        }
    };

    interface OnSelectPubDialogDismissed {
        void addPubToList(Pub newPub);
    }
}
