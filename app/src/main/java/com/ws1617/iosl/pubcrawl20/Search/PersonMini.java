package com.ws1617.iosl.pubcrawl20.Search;

import android.graphics.Bitmap;

import com.ws1617.iosl.pubcrawl20.DataModels.Person;

/**
 * Created by Haneen on 09/02/2017.
 */

public class PersonMini {
    String name;
    long id;
    Bitmap image;

    public PersonMini(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public PersonMini(Person person) {
        this.name = person.getName();
        this.id = person.getId();
        this.image = person.getImage();
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

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}