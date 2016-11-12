package com.ws1617.iosl.pubcrawl20;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.ws1617.iosl.pubcrawl20.Chat.ChatFragment;
import com.ws1617.iosl.pubcrawl20.CrawlSearch.CrawlSearchFragment;
import com.ws1617.iosl.pubcrawl20.Current.CurrentFragment;
import com.ws1617.iosl.pubcrawl20.NotificationHistory.NotificationHistoryFragment;

/**
 * Created by gaspe on 8. 11. 2016.
 */
/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private static int PAGES_NUM  = 4;
    private Context context;
    private int[] imageResId = {
            R.drawable.beer_white,
            R.drawable.ic_info_outline_24dp,
            R.drawable.ic_chat_bubble_24dp,
            R.drawable.ic_assignment
    };

    public MainFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CrawlSearchFragment();
            case 1:
                return new CurrentFragment();
            case 2:
                return new ChatFragment();
            case 3:
                return new NotificationHistoryFragment();
            default:
                return new CrawlSearchFragment();
        }
    }

    @Override
    public int getCount() {
        return PAGES_NUM;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position > imageResId.length) return "TODO";

        Drawable image = ContextCompat.getDrawable(context, imageResId[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        SpannableString sb = new SpannableString(" ");
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }
}