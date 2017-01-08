package com.ws1617.iosl.pubcrawl20.chat;

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

public class ChatFragment extends Fragment {
    public static final String TITLE = "Chat";
    private static final String TAG = "ChatFragment";
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
}
