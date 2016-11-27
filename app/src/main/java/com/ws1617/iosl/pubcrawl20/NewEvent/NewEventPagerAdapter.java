package com.ws1617.iosl.pubcrawl20.NewEvent;

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

import java.util.List;

/**
 * Created by gaspe on 9. 11. 2016.
 */

public class NewEventPagerAdapter extends FragmentPagerAdapter {

    List<Fragment> fragmentsList;

    private Context context;
    private int[] imageResId = {
            R.drawable.ic_assignment,
            R.drawable.ic_pin_drop,
            R.drawable.ic_share
    };

    public NewEventPagerAdapter(FragmentManager fm, Context context, List<Fragment> fragmentsList) {
        super(fm);
        this.context = context;
        this.fragmentsList = fragmentsList;
    }

    @Override
    public Fragment getItem(int position) {
        if (position <= fragmentsList.size()) {
            switch (position) {
                case 0:
                    return fragmentsList.get(0);
                case 1:
                    return fragmentsList.get(1);
                case 2:
                    return fragmentsList.get(2);
                default:
                    return fragmentsList.get(0);
            }
        } else
            return fragmentsList.get(0);

    }

    @Override
    public int getCount() {
        return fragmentsList.size();
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
