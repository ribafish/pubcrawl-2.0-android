package com.ws1617.iosl.pubcrawl20.NotificationHistory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by gaspe on 11. 11. 2016.
 */

public class NotificationHistoryFragment extends Fragment {
    private View rootView;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notification_history, container, false);
        return rootView;
    }

}
