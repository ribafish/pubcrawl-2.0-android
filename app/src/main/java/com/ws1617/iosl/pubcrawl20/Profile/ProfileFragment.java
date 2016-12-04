package com.ws1617.iosl.pubcrawl20.Profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by Gasper Kojek on 8. 11. 2016.
 * Github: https://github.com/ribafish/
 */

public class ProfileFragment extends Fragment {
    public static final String TITLE = "Profile";
    private static final String TAG = "ProfileFragment";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

}
