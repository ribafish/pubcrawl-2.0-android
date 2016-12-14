package com.ws1617.iosl.pubcrawl20.DataModels;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Gasper Kojek on 3. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class Person {
    private long id;
    private String name;
    private String email;
    private String description;
    private ArrayList<Bitmap> images = new ArrayList<>();
    private ArrayList<Long> friendIds = new ArrayList<>();
    private ArrayList<Long> eventIds = new ArrayList<>();
    private ArrayList<Long> favouritePubIds = new ArrayList<>();
    private ArrayList<Long> ownedPubIds = new ArrayList<>();
    private ArrayList<Long> ownedEventIds = new ArrayList<>();


    public Person(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Person(long id, String name, String email, String description) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.description = description;
    }


    public Person(long id, String name, String email, String description,
                  @NonNull ArrayList<Bitmap> images,
                  @NonNull ArrayList<Long> friendIds,
                  @NonNull ArrayList<Long> eventIds,
                  @NonNull ArrayList<Long> favouritePubIds,
                  @NonNull ArrayList<Long> ownedPubIds,
                  @NonNull ArrayList<Long> ownedEventIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.description = description;
        this.images = images;
        this.friendIds = friendIds;
        this.eventIds = eventIds;
        this.favouritePubIds = favouritePubIds;
        this.ownedPubIds = ownedPubIds;
        this.ownedEventIds = ownedEventIds;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
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

    public ArrayList<Long> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(ArrayList<Long> friendIds) {
        this.friendIds = friendIds;
    }

    public ArrayList<Long> getEventIds() {
        return eventIds;
    }

    public void setEventIds(ArrayList<Long> eventIds) {
        this.eventIds = eventIds;
    }

    public ArrayList<Long> getFavouritePubIds() {
        return favouritePubIds;
    }

    public void setFavouritePubIds(ArrayList<Long> favouritePubIds) {
        this.favouritePubIds = favouritePubIds;
    }

    public ArrayList<Long> getOwnedPubIds() {
        return ownedPubIds;
    }

    public void setOwnedPubIds(ArrayList<Long> ownedPubIds) {
        this.ownedPubIds = ownedPubIds;
    }

    public ArrayList<Long> getOwnedEventIds() {
        return ownedEventIds;
    }

    public void setOwnedEventIds(ArrayList<Long> ownedEventIds) {
        this.ownedEventIds = ownedEventIds;
    }
}