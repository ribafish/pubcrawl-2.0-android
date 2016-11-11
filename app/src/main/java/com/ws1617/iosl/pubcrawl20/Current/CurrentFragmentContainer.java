package com.ws1617.iosl.pubcrawl20.Current;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by gaspe on 9. 11. 2016.
 */

public class CurrentFragmentContainer extends Fragment {
    public static final String TITLE = "Current";
    private static final String TAG = "CurrentFragmentContainer";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_current, container, false);
        return rootView;
    }
}