package com.ws1617.iosl.pubcrawl20.Models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gaspe on 18. 11. 2016.
 */

public class Event {
    // general info
    private long eventId;
    private String eventName;
    private Date date;
    private String description;
    private boolean tracked;


    private ArrayList<Pub> pubs;

    private Crawler owner;

    private ArrayList<Crawler> participantsList;

    public Event() {
    }

    public Event(String eventName, Date date, String description, boolean tracked) {
        this.eventName = eventName;
        this.date = date;
        this.description = description;
        this.tracked = tracked;
    }

    public Event(String eventName, Date date, String description, boolean tracked, ArrayList<Pub> pubs, Crawler owner, ArrayList<Crawler> participantsList) {
        this.eventName = eventName;
        this.date = date;
        this.description = description;
        this.tracked = tracked;
        this.pubs = pubs;
        this.owner = owner;
        this.participantsList = participantsList;
    }

    public Event addPub (Pub pub) {
        this.pubs.add(pub);
        return this;
    }

    public Event addPubs (List<Pub> pubs) {
        this.pubs.addAll(pubs);
        return this;
    }

    public Event removePub (Pub pub) {
        this.pubs.remove(pub);
        return this;
    }

    public Event removePubs (List<Pub> pubs) {
        this.pubs.removeAll(pubs);
        return this;
    }


    public Event addParticipant(Crawler participant) {
        participantsList.add(participant);
        return this;
    }

    public String getEventName() {
        return eventName;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTracked() {
        return tracked;
    }

    public ArrayList<Pub> getPubs() {
        return pubs;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }

    public void setPubs(ArrayList<Pub> pubs) {
        this.pubs = pubs;
    }

    public Crawler getOwner() {
        return owner;
    }

    public void setOwner(Crawler owner) {
        this.owner = owner;
    }

    public ArrayList<Crawler> getParticipantsList() {
        return participantsList;
    }


    public void setParticipantsList(ArrayList<Crawler> participantsList) {
        this.participantsList = participantsList;


    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", eventName='" + eventName + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", tracked=" + tracked +
                ", pubs=" + pubs +
                ", owner=" + owner +
                ", participantsList=" + participantsList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return eventId == event.eventId;

    }

}
