package com.ws1617.iosl.pubcrawl20.dataModels;

import java.util.Date;

/**
 * Created by Gasper Kojek on 3. 12. 2016.
 * Github: https://github.com/ribafish/
 */

public class TimeSlot {
    private long pubId;
    private Date startTime;
    private Date endTime;

    public TimeSlot(long pubId, Date startTime, Date endTime) {
        this.pubId = pubId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getPubId() {
        return pubId;
    }

    public void setPubId(long pubId) {
        this.pubId = pubId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
