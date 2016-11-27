package com.ws1617.iosl.pubcrawl20.NewEvent;

import java.io.Serializable;

/**
 * Created by Haneen on 11/19/2016.
 */

public class Event implements Serializable {

    EventGeneralInf eventGeneralInfo;
    EventPubInfo eventPubInfo;



    class EventGeneralInf implements Serializable {
        private long id;

        private String eventName;
        private String date;
        private String description;
        private boolean tracked;


        public EventGeneralInf(long id, String eventName, String date,
                               String description, boolean tracked) {
            this.id = id;
            this.eventName = eventName;
            this.date = date;
            this.description = description;
            this.tracked = tracked;
        }
    }

    class EventPubInfo implements Serializable {
        // private List<Crawler> participantsList;
        // private List<Pub> pubsList;
        //   private Crawler eventOwner;
    }


}
