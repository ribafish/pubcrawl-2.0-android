package com.ws1617.iosl.pubcrawl20.Database;

/**
 * Created by phahne on 19.01.2017.
 */

public class DatabaseException extends Exception {

    public DatabaseException() { super(); }
    public DatabaseException(String message) { super(message); }
    public DatabaseException(String message, Throwable cause) { super(message, cause); }
    public DatabaseException(Throwable cause) { super(cause); }
}
