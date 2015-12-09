package com.datapublica.diachron.service.data;

import org.obolibrary.oboformat.diff.Diff;

import java.util.*;

/**
 * @author Jacques Belissent
 */
public class Difference {

    private final Type type;
    private Map<String, Object> properties = new HashMap<>();

    public Type getType() {
        return type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Difference setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public Difference setProperty(String name, Object value) {
        if (type.hasParameter(name)) {
            this.properties.put(name, value);
        }
        return this;
    }

    public Difference(Type type, Object... values) {
        this.type = type;
        for (int i = 0; i < values.length; i++) {
            properties.put(type.getParameters().get(i), values[i]);
        }
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }


    public Difference(Type type) {
        this.type = type;
    }

    public static enum Type {
        ADD_ATTRIBUTE("attribute"),
        ADD_CODELIST("codelist"),
        ADD_DATATYPE("datatype", "subject"),
        ADD_DIMENSION("dimension"),
        ADD_DIMENSION_VALUE_TO_OBSERVATION("dimension", "dimension_val"),
        ADD_FACT_TABLE("fact_table"),
        ADD_GENERIC_ATTRIBUTE("attribute", "subj"),
        ADD_GENERIC_VALUE_TO_OBSERVATION("observation", "property", "value"),
        ADD_HIERARCHY("hierarchy"),
        ADD_INSCHEME("scheme", "subj"),
        ADD_INSTANCE("instance"),
        ADD_INSTANCE_TO_PARENT("instance", "parent"),
        ADD_LABEL("obj_label", "prop_label", "subj_label"),
        ADD_MEASURE("measure"),
        ADD_MEASURE_VALUE_TO_OBSERVATION("measure", "measure_val"),
        ADD_OBSERVATION("observation"),
        ADD_RELEVANCY("1st_arg", "2nd_arg"),
        ADD_UNKNOWN_PROPERTY("obj_un_prop", "prop_un_prop", "subj_un_prop"),
        ATTACH_ATTR_TO_DIMENSION("attribute", "dimension"),
        ATTACH_ATTR_TO_MEASURE("attribute", "measure"),
        ATTACH_CODELIST_TO_DIMENSION("codelist", "dimension"),
        ATTACH_DATATYPE_TO_DIMENSION("datatype", "dimension"),
        ATTACH_DIMENSION_TO_FT("dimension", "fact_table"),
        ATTACH_HIERARCHY_TO_DIMENSION("dimension", "hierarchy"),
        ATTACH_INSTANCE_TO_CODELIST("codelist", "instance"),
        ATTACH_INSTANCE_TO_HIERARCHY("hierarchy", "instance"),
        ATTACH_INSTANCE_TO_PARENT("instance", "parent"),
        ATTACH_MEASURE_TO_FT("fact_table", "measure"),
        ATTACH_OBSERVATION_TO_FT("fact_table", "observation"),
        ATTACH_TYPE_TO_MEASURE("measure", "type"),
        DELETE_ATTRIBUTE("attribute"),
        DELETE_CODELIST("codelist"),
        DELETE_DATATYPE("1st_arg", "2nd_arg", "datatype", "subject"),
        DELETE_DIMENSION("dimension"),
        DELETE_DIMENSION_VALUE_FROM_OBSERVATION("dimension", "dimension_val"),
        DELETE_FACT_TABLE("fact_table"),
        DELETE_GENERIC_ATTRIBUTE("attribute", "subj"),
        DELETE_GENERIC_VALUE_FROM_OBSERVATION("observation", "property", "value"),
        DELETE_HIERARCHY("hierarchy"),
        DELETE_INSCHEME("scheme", "subj", "instance"),
        DELETE_INSTANCE_FROM_PARENT("instance", "parent"),
        DELETE_LABEL("obj_label", "prop_label", "subj_label"),
        DELETE_MEASURE("measure"),
        DELETE_MEASURE_VALUE_FROM_OBSERVATION("measure", "measure_val"),
        DELETE_OBSERVATION("observation"),
        DELETE_RELEVANCY("1st_arg", "2nd_arg", "datatype", "subject"),
        DELETE_UNKNOWN_PROPERTY("obj_un_prop", "prop_un_prop", "subj_un_prop"),
        DETACH_ATTR_FROM_DIMENSION("attribute", "datatype", "dimension"),
        DETACH_ATTR_FROM_MEASURE("attribute", "measure"),
        DETACH_CODELIST_FROM_DIMENSION("dimension"),
        DETACH_DATATYPE_FROM_DIMENSION("attribute", "datatype", "dimension"),
        DETACH_DIMENSION_FROM_FT("dimension", "fact_table"),
        DETACH_HIERARCHY_FROM_DIMENSION("dimension", "hierarchy"),
        DETACH_INSTANCE_FROM_CODELIST("codelist", "hierarchy", "instance"),
        DETACH_INSTANCE_FROM_HIERARCHY("hierarchy", "instance"),
        DETACH_INSTANCE_FROM_PARENT("instance", "parent"),
        DETACH_MEASURE_FROM_FT("fact_table", "measure"),
        DETACH_OBSERVATION_FROM_FT("fact_table", "observation"),
        DETACH_TYPE_FROM_MEASURE("measure", "type");

        private List<String> parameters;

        private Type(String... parameters) {
            this.parameters = Collections.unmodifiableList(Arrays.asList(parameters));
        }

        public List<String> getParameters() {
            return parameters;
        }

        public boolean hasParameter(String name) {
            return parameters.contains(name);
        }

    }


}
