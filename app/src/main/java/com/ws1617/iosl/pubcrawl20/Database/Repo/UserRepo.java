package com.ws1617.iosl.pubcrawl20.Database.Repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.ws1617.iosl.pubcrawl20.Database.DBRepoHelper;
import com.ws1617.iosl.pubcrawl20.Database.DatabaseDefI;
import com.ws1617.iosl.pubcrawl20.Database.Models.DatabaseManager;
import com.ws1617.iosl.pubcrawl20.Database.Models.User;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Icke-Hier on 03.12.2016.
 */

public class UserRepo implements DatabaseDefI, DBRepoHelper<User> {

  public UserRepo() {}

  public static String createTable()  {
    return
      CREATE_TABLE + User.DATABASE_TABLE_NAME + OPEN_BRACKET
      + User.ID + INT_PRIMERY_AUTOINC + COMMA_SEP
      + User.MAIL + TYPE_TEXT + COMMA_SEP
      + User.NAME + TYPE_TEXT + COMMA_SEP
      + User.DESCRIPTION + TYPE_TEXT + COMMA_SEP
      + User.EVENTLIST + TYPE_TEXT + COMMA_SEP
      + User.FRIENDLIST + TYPE_TEXT + CLOSE_INSTRUCTION;
  }

  public User insert(User user)
  {
    SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    ContentValues values = new ContentValues();
    values.put(User.NAME, user.getName());
    values.put(User.MAIL, user.getEmail());
    values.put(User.DESCRIPTION, user.getDescription());
    values.put(User.EVENTLIST, user.getEvents());
    values.put(User.FRIENDLIST, user.getFriends());
    // Inserting Row
    int id = (int)db.insert(User.DATABASE_TABLE_NAME, null, values);
    DatabaseManager.getInstance().closeDatabase();
    user.setId(id);
    return user;
  }

  public User getbyID(int id)
  {
    SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

    Cursor cursor = db.query(User.DATABASE_TABLE_NAME, new String[]{User.ID,
        User.NAME, User.MAIL, User.DESCRIPTION, User.EVENTLIST, User.FRIENDLIST}, User.ID + "=?",
      new String[]{String.valueOf(id)}, null, null, null, null);
    if (cursor == null)
      return null;
    cursor.moveToFirst();
    DatabaseManager.getInstance().closeDatabase();
    System.out.println(cursor.getColumnIndex(User.ID));
    System.out.println(cursor.getInt(cursor.getColumnIndex(User.ID)));
    return new User(
      cursor.getInt(cursor.getColumnIndex(User.ID)),
      cursor.getString(cursor.getColumnIndex(User.NAME)),
      cursor.getString(cursor.getColumnIndex(User.MAIL)),
      cursor.getString(cursor.getColumnIndex(User.DESCRIPTION)),
      cursor.getString(cursor.getColumnIndex(User.EVENTLIST)),
      cursor.getString(cursor.getColumnIndex(User.FRIENDLIST)));
  }

  public List<User> getAll(@Nullable String orderby, boolean asc)
  {
    List<User> users = new LinkedList<>();
    SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    String selectQuery = "SELECT  * FROM " + User.DATABASE_TABLE_NAME;
    if (orderby != null)
      selectQuery += " ORDER BY " + orderby + (asc ? ORDER_ASC : ORDER_DESC);
    Cursor cursor = db.rawQuery(selectQuery, null);
    if (cursor == null || cursor.getCount() == 0) {
      return null;
    }
    if (cursor.moveToFirst()) {
      do {
        User user = new User(
          cursor.getInt(cursor.getColumnIndex(User.ID)),
          cursor.getString(cursor.getColumnIndex(User.NAME)),
          cursor.getString(cursor.getColumnIndex(User.MAIL)),
          cursor.getString(cursor.getColumnIndex(User.DESCRIPTION)),
          cursor.getString(cursor.getColumnIndex(User.EVENTLIST)),
          cursor.getString(cursor.getColumnIndex(User.FRIENDLIST)));
        users.add(user);
      } while (cursor.moveToNext());
    }
    DatabaseManager.getInstance().closeDatabase();
    return users;
  }

  @Override
  public void delete(User user) {

  }

  @Override
  public void deletebyID(int id) {
    SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    db.delete(User.DATABASE_TABLE_NAME,
      User.ID+" = ?",
      new String[] { String.valueOf(id)});
    DatabaseManager.getInstance().closeDatabase();
  }

  @Override
  public int update(User user) {
    SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    ContentValues values = new ContentValues();
    values.put(User.ID, user.getId());
    values.put(User.NAME, user.getName());
    values.put(User.MAIL, user.getEmail());
    values.put(User.DESCRIPTION, user.getDescription());
    values.put(User.EVENTLIST, user.getEvents());
    values.put(User.FRIENDLIST, user.getFriends());

    int i = db.update(User.DATABASE_TABLE_NAME,
      values,
      User.ID+" = ?",
      new String[] { String.valueOf(user.getId())});
    DatabaseManager.getInstance().closeDatabase();
    return i;
  }

  @Override
  public void clearDB() {
    SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
    db.delete(User.DATABASE_TABLE_NAME,null,null);
    DatabaseManager.getInstance().closeDatabase();
  }

  public void insertTestUsers()
  {
    insert(new User("Haneen", "haneen@mail.com", "1,2,3", "The girl", "2,3,4"));
    insert(new User("Gasper", "gasper@mail.com", "1,2,3", "The one guy", "1,3,4"));
    insert(new User("Leo", "leo@mail.com", "1,2,3", "The other", "1,2,4"));
    insert(new User("Peter", "p.hahne@campus.tu-berlin.de", "1,2,3", "The master of disaster", "1,2,3"));
  }
}
