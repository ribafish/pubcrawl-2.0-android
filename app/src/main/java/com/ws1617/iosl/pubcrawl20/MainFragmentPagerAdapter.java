package com.ws1617.iosl.pubcrawl20;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ws1617.iosl.pubcrawl20.Chat.ChatFragment;
import com.ws1617.iosl.pubcrawl20.Home.HomeFragment;
import com.ws1617.iosl.pubcrawl20.Profile.ProfileFragment;

/**
 * Created by gaspe on 8. 11. 2016.
 */
/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private static int PAGES_NUM  = 3;
    private Context context;

    public MainFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new ProfileFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return PAGES_NUM;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return HomeFragment.TITLE;
            case 1:
                return ChatFragment.TITLE;
            case 2:
                return ProfileFragment.TITLE;
            default:
                return "TODO";
        }
    }
}