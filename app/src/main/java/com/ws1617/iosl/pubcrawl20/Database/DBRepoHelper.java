package com.ws1617.iosl.pubcrawl20.Database;

import java.util.List;

/**
 * Created by Icke-Hier on 03.12.2016.
 */

public interface DBRepoHelper<T> {

  T getbyID(int id);

  T insert(T object);

  void delete(T object);

  void deletebyID(int id);

  int update(T object);

  void clearDB();

  List<T> getAll(String orderby, boolean asc);
}
