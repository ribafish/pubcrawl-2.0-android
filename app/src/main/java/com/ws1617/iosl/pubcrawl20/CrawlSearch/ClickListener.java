package com.ws1617.iosl.pubcrawl20.CrawlSearch;

import android.view.View;

/**
 * Created by gaspe on 30. 11. 2016.
 */

public interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}