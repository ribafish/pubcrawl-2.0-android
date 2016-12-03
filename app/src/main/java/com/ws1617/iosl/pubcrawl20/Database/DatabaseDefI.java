package com.ws1617.iosl.pubcrawl20.Database;

/**
 * </br>
 * The interface TestDB implements some basic SQL stuff
 * <p/>
 * *****************************************************************************
 * <dl compact>
 * <dt><b> Project:   </b></dt><dd>  Verleihsystem       </dd>
 * <dt><b> Author:    </b></dt><dd>  Peter Hahne         </dd>
 * <dt><b> Created:   </b></dt><dd>  16.10.2015       </dd>
 * </dl>
 * *****************************************************************************
 * <br/>
 * Changes:
 * <table>
 * <tr><td> 01.12.2015 </td><td>Change: {@code clearDB} added.</td></tr>
 * <tr><td> 16.10.2015 </td><td>Introduced.</td></tr>
 * </table>
 */
public interface DatabaseDefI {

  String DATABASE_NAME = "PUB.db";
  int DATABASE_VERSION = 1;

  String TYPE_TEXT            = " TEXT";
  String TYPE_INT             = " INTEGER";
  String TYPE_REAL            = " REAL";
  String INT_PRIMERY          = " INTEGER PRIMARY KEY";
  String INT_PRIMERY_AUTOINC  = " INTEGER PRIMARY KEY AUTOINCREMENT";
  String COMMA_SEP            = ",";
  String OPEN_BRACKET         = " (";
  String CLOSE_BRACKET        = ")";
  String CLOSE_INSTRUCTION    = ");";
  String CREATE_TABLE         = "CREATE TABLE IF NOT EXISTS ";
  String DROP_TABLE           = "drop table if exists ";
  String ORDER_ASC            = " ASC";
  String ORDER_DESC           = " DESC";

}
