package com.ws1617.iosl.pubcrawl20.DisplayEvents.Models;

/**
 * Created by gaspe on 18. 11. 2016.
 */

public class Person {
    private long id;
    private String name;
    private String email;
    private String description;

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
}
