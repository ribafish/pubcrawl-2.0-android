package com.ws1617.iosl.pubcrawl20.Database;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.resetEventsDatabase;
import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.resetPersonsDatabase;
import static com.ws1617.iosl.pubcrawl20.Database.DatabaseHelper.resetPubsDatabase;

/**
 * Created by Gasper Kojek on 20. 02. 2017.
 * Github: https://github.com/ribafish/
 */

public class resetDbTask extends AsyncTask<Void, Void, Void> {
    private Context context;
    private ProgressDialog pd;
    private int which = 0;
    public static final int EVENTS_DB = 0x01;
    public static final int PUBS_DB = 0x02;
    public static final int PERSONS_DB = 0x04;
    public static final int ALL_DB = 0x07;

    public resetDbTask(Context context, int which) {
        super();
        this.context = context;
        this.which = which;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            pd = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
            pd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if ((which&PUBS_DB) == PUBS_DB) resetPubsDatabase(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if ((which&PERSONS_DB) == PERSONS_DB) resetPersonsDatabase(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if ((which&EVENTS_DB) == EVENTS_DB) resetEventsDatabase(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            pd.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
