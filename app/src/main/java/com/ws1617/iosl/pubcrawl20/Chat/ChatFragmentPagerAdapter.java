package com.ws1617.iosl.pubcrawl20.Chat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.ws1617.iosl.pubcrawl20.R;

/**
 * Created by gaspe on 9. 11. 2016.
 */

public class ChatFragmentPagerAdapter extends FragmentPagerAdapter {
    private static int PAGES_NUM  = 2;
    private Context context;
    private int[] imageResId = {
            R.drawable.ic_chat_24dp,
            R.drawable.ic_contacts_24dp
    };

    public ChatFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ChatHistoryFragment();
            case 1:
                return new ContactsFragment();
            default:
                return new ChatHistoryFragment();
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
