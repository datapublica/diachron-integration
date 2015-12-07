package com.datapublica.diachron.service.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jacques Belissent
 */
public class Dataset {

    private String uri;
    private String name;
    private Date creationDate;
    private List<DatasetVersion> versions = new ArrayList<>();

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<DatasetVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<DatasetVersion> versions) {
        this.versions = versions;
    }

    public void addVersion(DatasetVersion version) {
        this.versions.add(version);
    }
}
