package com.datapublica.diachron.service.data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jacques Belissent
 */
public class Codelist extends Concept {

    public Set<String> instances = new HashSet<>();

    public Codelist(String uri, String name, BasicType type) {
        super(uri, name, type);
    }

    public Codelist setInstances(String ... instances) {
        for (String i : instances) this.instances.add(i);
        return this;
    }
}