package com.ws1617.iosl.pubcrawl20.Details;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ws1617.iosl.pubcrawl20.R;

import java.util.ArrayList;

public class PersonDetailsActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "PersonDetailsActivity";

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.75f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mCollapasableTitleContainer;
    private TextView mTitle;
    private TextView mSubtitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private ImageCarouselPager imageCarouselPager;

    private long personId;
    private String personName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        mToolbar = (Toolbar) findViewById(R.id.person_details_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.person_details_appbar);
        mTitle = (TextView) findViewById(R.id.person_details_title);
        mSubtitle = (TextView) findViewById(R.id.person_details_subtitle);
        mCollapasableTitleContainer = (LinearLayout) findViewById(R.id.person_details_layout_title);
        ViewPager viewPager = (ViewPager) findViewById(R.id.person_details_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.person_details_tabDots);


        mAppBarLayout.addOnOffsetChangedListener(this);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        try{
            personId = getIntent().getLongExtra("id", -1);
            personName = getIntent().getStringExtra("name");

            mToolbar.setTitle(personName);
            mTitle.setText(personName);
            mSubtitle.setText("Loading...");  //TODO

        } catch (Exception e) { e.printStackTrace(); }

        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        images.add(BitmapFactory.decodeResource(getResources(), R.mipmap.bestpub));
        imageCarouselPager = new ImageCarouselPager(this, images);
        viewPager.setAdapter(imageCarouselPager);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager, true);


        startAlphaAnimation(mToolbar, 0, View.INVISIBLE);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mToolbar, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mToolbar, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mCollapasableTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mCollapasableTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
}
