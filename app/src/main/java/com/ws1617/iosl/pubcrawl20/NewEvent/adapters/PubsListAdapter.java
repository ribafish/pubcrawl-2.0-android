package com.ws1617.iosl.pubcrawl20.newEvent.adapters;

        import android.content.Context;
        import android.widget.ArrayAdapter;

        import com.google.android.gms.maps.model.LatLng;
        import com.ws1617.iosl.pubcrawl20.dataModels.Pub;

        import java.util.ArrayList;
        import java.util.List;

/**
 * Created by Haneen on 12/5/2016.
 */

public class PubsListAdapter extends ArrayAdapter{



    List<Pub> pubsList;

    public PubsListAdapter(Context context, int resource) {
        super(context, resource);
    }

    void initPubList(){
        //TODO should be fetched from the local DB
        pubsList = new ArrayList<>();
        pubsList.add(new Pub(1,"pub 1",new LatLng(1,1),10));
        pubsList.add(new Pub(2,"pub 2",new LatLng(1,1),20));
        pubsList.add(new Pub(3,"pub 3",new LatLng(1,1),30));
        pubsList.add(new Pub(4,"pub 4",new LatLng(1,1),40));
        pubsList.add(new Pub(5,"pub 5",new LatLng(1,1),50));
        pubsList.add(new Pub(6,"pub 6",new LatLng(1,1),60));
    }
}
