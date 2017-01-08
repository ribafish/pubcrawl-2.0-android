package com.ws1617.iosl.pubcrawl20.displayEvents.MiniDataModels;

import android.graphics.Color;

import com.google.android.gms.maps.model.Polyline;
import com.ws1617.iosl.pubcrawl20.dataModels.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Gasper Kojek on 13. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class EventMini {
    private long eventId;
    private String name;
    private Date date;
    private String description;
    private ArrayList<PubMini> pubs;
    private Polyline polyline;
    private boolean selected = false;
    private ArrayList<Long> participantIds = new ArrayList<>();

    public EventMini() {
    }

    public EventMini(Event event) {
        this.eventId = event.getId();
        this.name = event.getEventName();
        this.date = event.getDate();
        this.description = event.getDescription();
        this.pubs = new ArrayList<>();
        this.polyline = event.getPolyline();
        this.selected = false;
        this.participantIds = event.getParticipantIds();
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

    public ArrayList<PubMini> getPubs() {
        return pubs;
    }

    public void setPubs(ArrayList<PubMini> pubs) {
        this.pubs = pubs;
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

    public ArrayList<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(ArrayList<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public EventMini addParticipant(long participantId) {
        participantIds.add(participantId);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventMini event = (EventMini) o;

        return this.eventId == event.getEventId();

    }
}
