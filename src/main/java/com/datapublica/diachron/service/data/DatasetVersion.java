package com.datapublica.diachron.service.data;

import java.util.Date;

/**
 *
 */
public class DatasetVersion {
    public String id;
    public String recordSet;
    public Date date;

    public DatasetVersion() {
    }

    public DatasetVersion(String id, String recordSet, Date date) {
        this.id = id;
        this.recordSet = recordSet;
        this.date = date;
    }
}
