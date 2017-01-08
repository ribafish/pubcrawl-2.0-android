package com.ws1617.iosl.pubcrawl20.dataModels;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Gasper Kojek on 3. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class Pub {
    private long id;
    private String pubName;
    private LatLng latLng;
    private int size;
    private int prices;
    private double rating;
    private String description;
    private String openingTimes;
    private ArrayList<Long> topsListIds = new ArrayList<>();
    private ArrayList<Long> eventsListIds = new ArrayList<>();
    private ArrayList<Bitmap> images = new ArrayList<>();
    private Long ownerId;

    public Pub() {
    }

    public Pub(long id) {
        this.id = id;
    }

    public Pub(long id, String pubName, LatLng latLng, int size) {
        this.id = id;
        this.pubName = pubName;
        this.latLng = latLng;
        this.size = size;
    }

    public Pub(long id, String pubName, LatLng latLng, int size, int prices, double rating,
               String openingTimes,
               @NonNull ArrayList<Long> topsListIds,
               @NonNull ArrayList<Long> eventsListIds,
               Long ownerId) {
        this.id = id;
        this.pubName = pubName;
        this.latLng = latLng;
        this.size = size;
        this.prices = prices;
        this.rating = rating;
        this.openingTimes = openingTimes;
        this.topsListIds = topsListIds;
        this.eventsListIds = eventsListIds;
        this.ownerId = ownerId;
    }

    public String getPubName() {
        return pubName;
    }

    public LatLng getLatLng() {

        return latLng;
    }

    public int getSize() {
        return size;
    }

    public void setPubName(String pubName) {
        this.pubName = pubName;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPrices() {
        return prices;
    }

    public void setPrices(int prices) {
        this.prices = prices;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getOpeningTimes() {
        return openingTimes;
    }

    public void setOpeningTimes(String openingTimes) {
        this.openingTimes = openingTimes;
    }

    public ArrayList<Long> getTopsListIds() {
        return topsListIds;
    }

    public void setTopsListIds(ArrayList<Long> topsListIds) {
        this.topsListIds = topsListIds;
    }

    public ArrayList<Long> getEventsListIds() {
        return eventsListIds;
    }

    public void setEventsListIds(ArrayList<Long> eventsListIds) {
        this.eventsListIds = eventsListIds;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "Pub{" +
                "id=" + id +
                ", pubName='" + pubName + '\'' +
                ", latLng=" + latLng +
                ", size=" + size +
                '}';
    }

    public ArrayList<Bitmap> getImages() {
        return images;
    }

    public void setImages(ArrayList<Bitmap> images) {
        this.images = images;
    }

    public void addImage(Bitmap image) {
        images.add(image);
    }

    public void removeImage(Bitmap image) {
        images.remove(image);
    }

    public void removeImage(int imageId) {
        images.remove(imageId);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
