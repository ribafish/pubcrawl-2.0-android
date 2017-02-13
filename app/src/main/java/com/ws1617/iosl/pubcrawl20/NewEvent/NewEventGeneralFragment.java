package com.ws1617.iosl.pubcrawl20.NewEvent;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.R;
import com.ws1617.iosl.pubcrawl20.Utilites.DateTimeTools;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewEventGeneralFragment extends Fragment {
    private static final String TAG = "NewEventGeneralFragment";
    private View rootView;
    private Button datePickerButton;
    EditText mEventTitleTxt, mEventDescription;
    SwitchCompat mTrackedSwitch;
    Event mEventPublicInfo;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_new_event_generals, container, false);
        mEventPublicInfo = new Event();
        initView();
        return rootView;
    }


    public void initView() {
        mEventTitleTxt = (EditText) rootView.findViewById(R.id.event_new_title);
        mEventDescription = (EditText) rootView.findViewById(R.id.event_new_description);
        mTrackedSwitch = (SwitchCompat) rootView.findViewById(R.id.event_new_tracked);
        datePickerButton = (Button) rootView.findViewById(R.id.event_new_date_picker);
        datePickerButton.setOnClickListener(datePickerEventListener);
        datePickerButton.setText(DateTimeTools.getCurrentDate());

    }

    View.OnClickListener datePickerEventListener = new View.OnClickListener() {
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
            int month = monthOfYear + 1;
            datePickerButton.setText(dayOfMonth + "." + month + "." + year);
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public Event updateGeneralInfo(Event event) {

        String eventTitle = mEventTitleTxt.getText().toString().trim();
        String eventDesc = mEventDescription.getText().toString();
        Date date = new Date();

        String EVENT_DATE_FORMAT = "dd.MM.yyyy";
        DateFormat dateFormat = new SimpleDateFormat(EVENT_DATE_FORMAT, Locale.ENGLISH);

        try {
            date = dateFormat.parse(datePickerButton.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        boolean tracked = mTrackedSwitch.isChecked() ? true : false;

        event.setDate(date);
        event.setEventName(eventTitle);
        event.setDescription(eventDesc);
        event.setTracked(tracked);
        return event;
    }
}
