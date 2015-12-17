package com.datapublica.diachron.service.data;

import java.util.HashMap;
import java.util.Map;

public class ChangeSetQuery {
    private Difference.Type type;
    private Map<String, Object> properties = new HashMap<>();
    private Difference.Type joinType;
    private int page = 0;

    public ChangeSetQuery(Difference.Type type) {
        this.type = type;
    }

    public ChangeSetQuery(Difference.Type type, Difference.Type joinType) {
        this.type = type;
        this.joinType = joinType;
    }

    public ChangeSetQuery() {
    }

    public Difference.Type getType() {
        return type;
    }

    public void setType(Difference.Type type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    public Difference.Type getJoinType() {
        return joinType;
    }

    public void setJoinType(Difference.Type joinType) {
        this.joinType = joinType;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
