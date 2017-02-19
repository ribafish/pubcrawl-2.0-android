package com.ws1617.iosl.pubcrawl20.DataModels;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.DESCRIPTION;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.EMAIL;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.IMAGE;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.PERSON_ID;
import static com.ws1617.iosl.pubcrawl20.Database.Contracts.PersonContract.USERNAME;

/**
 * Created by Gasper Kojek on 3. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class Person {
    private long id;
    private String name;
    private String email;
    private String description = "";
    private Bitmap image;
    private String imageUrl;
    private ArrayList<Long> friendIds = new ArrayList<>();
    private ArrayList<Long> eventIds = new ArrayList<>();
    private ArrayList<Long> favouritePubIds = new ArrayList<>();
    private ArrayList<Long> ownedPubIds = new ArrayList<>();
    private ArrayList<Long> ownedEventIds = new ArrayList<>();


    public Person(long id) {
        this.id = id;
    }

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
                  Bitmap image,
                  @NonNull ArrayList<Long> friendIds,
                  @NonNull ArrayList<Long> eventIds,
                  @NonNull ArrayList<Long> favouritePubIds,
                  @NonNull ArrayList<Long> ownedPubIds,
                  @NonNull ArrayList<Long> ownedEventIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.description = description;
        this.image = image;
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

    public String getMail() {
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

    public Bitmap getImage() {
        return image;
    }

    public byte[] getImageInBytes(int pos) {
        if (image == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public void setImage(Bitmap image) {
        this.image = image;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(PERSON_ID, getId());
        values.put(DESCRIPTION, getDescription());
        values.put(IMAGE, getImageInBytes(0));
        values.put(EMAIL, getMail());
        values.put(USERNAME, getName());
        return  values;
    }
}