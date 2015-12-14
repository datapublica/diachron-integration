package com.datapublica.diachron.service.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class DatasetVersion {
    public String id;
    public String recordSet;
    public String schemaSet;
    public Date date;

    private List<Concept> dimensions = new ArrayList<>();
    public List<Concept> measures = new ArrayList<>();

    /* temporary, for inspection */
    private Object rdf;

    public DatasetVersion() {
    }

    public DatasetVersion(String id, String recordSet, String schemaSet, Date date) {
        this.id = id;
        this.recordSet = recordSet;
        this.schemaSet = schemaSet;
        this.date = date;
    }

    public List<Concept> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Concept> measures) {
        this.measures = measures;
    }

    public List<Concept> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<Concept> dimensions) {
        this.dimensions = dimensions;
    }

    public DatasetVersion addDimension(Concept c) {
        this.dimensions.add(c);
        return this;
    }

    public DatasetVersion addMeasure(Concept c) {
        this.measures.add(c);
        return this;
    }

    public Object getModel() {
        return rdf;
    }

    public void setModel(Object model) {
        this.rdf = model;
    }
}
