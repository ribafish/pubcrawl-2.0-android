package com.ws1617.iosl.pubcrawl20.dataModels;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Gasper Kojek on 3. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class Event {
    private long Id;
    private String eventName;
    private Date date;
    private String description;
    private boolean tracked;
    private long ownerId;
    private Bitmap image;
    private LatLng minLatLng, maxLatLng;

    private ArrayList<TimeSlot> timeSlotList = new ArrayList<>();
    private ArrayList<Long> participantIds = new ArrayList<>();
    private ArrayList<Long> pubIds = new ArrayList<>();
    private Polyline polyline;

    public Event() {
    }

    public Event(long Id) {
        this.Id = Id;
    }

    public Event(String eventName, Date date, String description, boolean tracked) {
        this.eventName = eventName;
        this.date = date;
        this.description = description;
        this.tracked = tracked;
    }

    public Event(String eventName, Date date, String description, boolean tracked,
                 @NonNull ArrayList<Long> pubIds, long ownerId,
                 @NonNull ArrayList<Long> participantIds) {
        this.eventName = eventName;
        this.date = date;
        this.description = description;
        this.tracked = tracked;
        this.pubIds = pubIds;
        this.ownerId = ownerId;
        this.participantIds = participantIds;
    }

    public Event addPub (long pubId) {
        this.pubIds.add(pubId);
        return this;
    }

    public Event addPubs (List<Long> pubs) {
        this.pubIds.addAll(pubs);
        return this;
    }

    public Event removePub (long pubId) {
        this.pubIds.remove(pubId);
        return this;
    }

    public Event removePubs (List<Long> pubs) {
        this.pubIds.removeAll(pubs);
        return this;
    }


    public Event addParticipant(long participantId) {
        participantIds.add(participantId);
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

    public ArrayList<Long> getPubIds() {
        return pubIds;
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

    public void setPubIds(ArrayList<Long> pubIds) {
        this.pubIds = pubIds;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public ArrayList<Long> getParticipantIds() {
        return participantIds;
    }


    public void setParticipantIds(ArrayList<Long> participantIds) {
        this.participantIds = participantIds;


    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        this.Id = id;
    }

    @Override
    public String toString() {
        return "Event{" +
                "Id=" + Id +
                ", eventName='" + eventName + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", tracked=" + tracked +
                ", pubIds=" + pubIds +
                ", ownerId=" + ownerId +
                ", participantIds=" + participantIds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return Id == event.Id;

    }

    public ArrayList<TimeSlot> getTimeSlotList() {
        return timeSlotList;
    }

    public void setTimeSlotList(ArrayList<TimeSlot> timeSlotList) {
        this.timeSlotList = timeSlotList;
    }

    /*
        Not to be used for database, but we will probably use it every time we display the event
     */

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public LatLng getMinLatLng() {
        return minLatLng;
    }

    public void setMinLatLng(LatLng minLatLng) {
        this.minLatLng = minLatLng;
    }

    public LatLng getMaxLatLng() {
        return maxLatLng;
    }

    public void setMaxLatLng(LatLng maxLatLng) {
        this.maxLatLng = maxLatLng;
    }
}
