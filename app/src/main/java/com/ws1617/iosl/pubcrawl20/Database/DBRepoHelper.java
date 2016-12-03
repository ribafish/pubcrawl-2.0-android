package com.ws1617.iosl.pubcrawl20.Database;

import java.util.List;

/**
 * Created by Icke-Hier on 03.12.2016.
 */

public interface DBRepoHelper<T> {

  T getbyID(int id);

  void insert(T object);

  void delete(T object);

  int update(T object);

  List<T> getAll(String orderby, boolean asc);
}
