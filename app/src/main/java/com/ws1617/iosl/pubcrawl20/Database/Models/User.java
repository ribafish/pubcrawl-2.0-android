package com.ws1617.iosl.pubcrawl20.Database.Models;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Peter Hahne on 01.12.2016.
 */

public class User {

  public static final String DATABASE_TABLE_NAME = "users";
  public static final String ID = "_id";
  public static final String NAME = "name";
  public static final String MAIL = "mail";
  public static final String DESCRIPTION = "description";
  public static final String EVENTLIST = "eventlist";
  public static final String FRIENDLIST = "friendlist";

  private User()  {}

  public User(int id, String name, String mail, String events, String description, String friends)
  {
    this.id = id;
    this.name = name;
    this.email = mail;
    this.events = events;
    this.description = description;
    this.friends = friends;
  }

  public User(String name, String mail, String events, String description, String friends)
  {
    this.name = name;
    this.email = mail;
    this.events = events;
    this.description = description;
    this.friends = friends;
  }

  private ArrayList<String> getEventsfromString(String s)
  {
    return null;
  }


  private ArrayList<Integer> getFrindsfromString(String s)
  {
    return null;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEvents() { return events;  }

  public void setEvents(String events) {
    this.events = events;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFriends() {
    return friends;
  }

  public void setFriends(String friends) {
    this.friends = friends;
  }

  private int id;
  private String name;
  private String email;
  private String events;
  private String description;
  //TODO
  //private Bitmap profilepricture;
  private String friends;
}
