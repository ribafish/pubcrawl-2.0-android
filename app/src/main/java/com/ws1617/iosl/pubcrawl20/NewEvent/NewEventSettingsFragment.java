package com.ws1617.iosl.pubcrawl20.NewEvent;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.ws1617.iosl.pubcrawl20.R;

import java.util.Calendar;

public class NewEventSettingsFragment extends Fragment {
    private static final String TAG = "NewEventSettingsFragment";
    private View rootView;
    private Button dataPickerButton;

    Event.EventGeneralInf mEventPublicInfo;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_new_event_settings, container, false);
        mEventPublicInfo = new Event().eventGeneralInfo;
        initView();
        return rootView;
    }


    public void initView() {
        dataPickerButton = (Button) rootView.findViewById(R.id.event_new_date_picker);
        dataPickerButton.setOnClickListener(dataPickerEventListener);

    }

    View.OnClickListener dataPickerEventListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(getContext(), onDateSetListener, year, month, day).show();
        }
    };

    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            dataPickerButton.setText(dayOfMonth + "." + monthOfYear + "." + year);
        }
    };



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mEventPublicInfo = CollectGeneralInfo();
        outState.putSerializable(TAG, mEventPublicInfo);
    }

    public Event.EventGeneralInf CollectGeneralInfo() {
        Event.EventGeneralInf info = new Event().new EventGeneralInf(
                1, "eventame", "1.1.2016", "this is a description", false
        );

        return info;
    }


}
