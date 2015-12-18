package com.datapublica.diachron.service.data;

/**
 * @author Jacques Belissent
 */
public class Concept {

    public String uri;

    public String label;

    public BasicType type;

    public static enum BasicType {
        STRING, INTEGER, DECIMAL, ENUM
    }

    public Concept() {
    }

    public Concept(String uri, String name, BasicType type) {
        this.uri = uri;
        this.label = name;
        this.type = type;
    }
}
