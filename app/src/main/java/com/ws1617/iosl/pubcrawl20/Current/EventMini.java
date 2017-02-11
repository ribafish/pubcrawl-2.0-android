package com.ws1617.iosl.pubcrawl20.Current;

import com.ws1617.iosl.pubcrawl20.DataModels.Event;
import com.ws1617.iosl.pubcrawl20.DataModels.PubMiniModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Gasper Kojek on 11. 02. 2017.
 * Github: https://github.com/ribafish/
 */

class EventMini {
    private long eventId;
    private String name;
    private Date date;
    private String description;
    private ArrayList<PubMiniModel> pubs;
    private int participants;

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public EventMini() {
    }

    public EventMini(Event event) {
        this.eventId = event.getId();
        this.name = event.getEventName();
        this.date = event.getDate();
        this.description = event.getDescription();
        this.pubs = new ArrayList<>();
        this.participants = event.getParticipantIds().size();
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventMini addPub (PubMini pub) {
        this.pubs.add(pub);
        return this;
    }

    public EventMini addPubs (List<PubMini> pubs) {
        this.pubs.addAll(pubs);
        return this;
    }

    public EventMini removePub (PubMini pub) {
        this.pubs.remove(pub);
        return this;
    }

    public EventMini removePubs (List<PubMini> pubs) {
        this.pubs.removeAll(pubs);
        return this;
    }

    public ArrayList<PubMiniModel> getPubs() {
        return pubs;
    }

    public void setPubs(ArrayList<PubMiniModel> pubs) {
        this.pubs = pubs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventMini event = (EventMini) o;

        return this.eventId == event.getEventId();
    }
}