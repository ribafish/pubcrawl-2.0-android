package com.ws1617.iosl.pubcrawl20.NewEvent;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ws1617.iosl.pubcrawl20.Models.Pub;
import com.ws1617.iosl.pubcrawl20.NewEvent.adapters.PubsListAdapter;
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


    //Data
    List<Pub> pubsList;
    List<String> pubsListString;


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

        Pub pub1 = new Pub("pub 1", new LatLng(1, 1), 10);
        pubsList.add(pub1);
        pubsListString.add(pub1.getPubName());

        Pub pub2 = new Pub("pub 2", new LatLng(1, 1), 20);
        pubsList.add(pub2);
        pubsListString.add(pub2.getPubName());

        Pub pub3 = new Pub("pub 3", new LatLng(1, 1), 30);
        pubsList.add(pub3);
        pubsListString.add(pub3.getPubName());

        Pub pub4 = new Pub("pub 4", new LatLng(1, 1), 40);
        pubsList.add(pub4);
        pubsListString.add(pub4.getPubName());


    }

    View.OnClickListener mDoneBtnClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.pub_dialog_pub_done: {
                    Pub pub = new Pub("no", new LatLng(1, 1), 1);
                    onSelectPubDialogDismissed.addPubToList(pub);
                    dismiss();
                    break;
                }
                case R.id.pub_dialog_visit_from_date_picker: {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    new DatePickerDialog(getContext(), onDateFromSetListener, year, month, day).show();
                    break;
                }
                case R.id.pub_dialog_visit_to_date_picker: {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
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
            Pub selectedPub = pubsList.get(pos);
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
