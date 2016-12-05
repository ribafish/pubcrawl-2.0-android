package com.ws1617.iosl.pubcrawl20.CrawlSearch.Models;

import android.graphics.Color;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gaspe on 18. 11. 2016.
 */

public class Event {
    private long eventId;
    private String eventName;
    private Date date;
    private String description;
    private boolean tracked;
    private ArrayList<Pub> pubs;
    private Person owner;
    private ArrayList<Person> participants;
    private Polyline polyline;
    private boolean selected;

    public Event() {
        this.selected = false;
    }

    public Event(String eventName, Date date, String description, boolean tracked) {
        this.eventName = eventName;
        this.date = date;
        this.description = description;
        this.tracked = tracked;
        this.selected = false;
    }

    public Event(String eventName, Date date, String description, boolean tracked, ArrayList<Pub> pubs, Person owner, ArrayList<Person> participants) {
        this.eventName = eventName;
        this.date = date;
        this.description = description;
        this.tracked = tracked;
        this.pubs = pubs;
        this.owner = owner;
        this.participants = participants;
        this.selected = false;
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


    public Event addParticipant(Person participant) {
        participants.add(participant);
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

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public ArrayList<Person> getParticipants() {
        return participants;
    }


    public void setParticipants(ArrayList<Person> participants) {
        this.participants = participants;


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
                ", participants=" + participants +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return eventId == event.eventId;

    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {


        if (polyline == null) {
            this.selected = selected;
            return;
        }

        if (selected && !this.selected) {
            this.selected = true;
            this.polyline.setColor(Color.BLUE);
        } else {
            this.selected = false;
            this.polyline.setColor(Color.GRAY);
        }
    }
}
