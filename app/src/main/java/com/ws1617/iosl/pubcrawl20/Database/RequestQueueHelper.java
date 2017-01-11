package com.ws1617.iosl.pubcrawl20.Database;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gasper Kojek on 4. 01. 2017.
 * Github: https://github.com/ribafish/
 */

public class RequestQueueHelper {
    private static final String TAG = "RequestQueueHelper";
    private RequestQueue requestQueue;
    private AtomicInteger requestNumber;
    public static final String BROADCAST_INTENT = "com.ws1617.iosl.pubcrawl20.DatabaseRefreshedBroadcast";
    private Context context;

    public RequestQueueHelper(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        requestNumber = new AtomicInteger(0);
        this.context = context;
    }

    public RequestQueue getQueue() {
        return requestQueue;
    }

    public void add(Request request) {
        requestQueue.add(request);
        requestNumber.incrementAndGet();
    }

    public void gotResponse() {
        if (requestNumber.decrementAndGet() == 0) {
            Log.d(TAG, "Finished all active volley requests. Broadcasting database download finished");
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(BROADCAST_INTENT);
            context.sendBroadcast(broadcastIntent);
        }
    }

}
