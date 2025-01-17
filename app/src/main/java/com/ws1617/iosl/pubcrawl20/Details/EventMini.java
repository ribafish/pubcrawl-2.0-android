package com.ws1617.iosl.pubcrawl20.Details;

import com.ws1617.iosl.pubcrawl20.DataModels.Event;

import java.util.Date;

/**
 * Created by Gasper Kojek on 12. 12. 2016.
 * Github: https://github.com/ribafish/
 */

class EventMini {
    private String name;
    private long id;
    private Date date;

    public EventMini(String name, long id, Date date) {
        this.name = name;
        this.id = id;
        this.date = date;
    }

    public EventMini(Event event) {
        this.name = event.getEventName();
        this.id = event.getId();
        this.date = event.getDate();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
