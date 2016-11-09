package com.ws1617.iosl.pubcrawl20.Chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by gaspe on 8. 11. 2016.
 */

public class ChatFragment extends Fragment {
    public static final String TITLE = "Chat";
    private static final String TAG = "ChatFragment";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.chat_pager);
        mViewPager.setAdapter(new ChatFragmentPagerAdapter(getChildFragmentManager(), getContext()));

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.chat_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
}
