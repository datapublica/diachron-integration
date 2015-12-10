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
        ADD_ATTRIBUTE ("Add_Attribute", "addAtt", "attribute"),
        ADD_CODELIST ("Add_Codelist", "addClist", "codelist"),
        ADD_DATATYPE ("Add_Datatype", "addDtype", "subject", "datatype"),
        ADD_DIMENSION ("Add_Dimension", "addDim", "dimension"),
        ADD_DIMENSION_VALUE_TO_OBSERVATION ("Add_Dimension_Value_To_Observation", "addDimVToOb", "observation", "dimension", "dimension_val"),
        ADD_FACT_TABLE ("Add_Fact_Table", "addFT", "fact_table"),
        ADD_GENERIC_ATTRIBUTE ("Add_Generic_Attribute", "addGenAtt", "subj", "attribute"),
        ADD_GENERIC_VALUE_TO_OBSERVATION ("Add_Generic_Value_To_Observation", "addGenVToOb", "observation", "property", "value"),
        ADD_HIERARCHY ("Add_Hierarchy", "addH", "hierarchy"),
        ADD_INSCHEME ("Add_inScheme", "addInSch", "subj", "scheme"),
        ADD_INSTANCE ("Add_Instance", "addInst", "instance"),
        ADD_INSTANCE_TO_PARENT ("Add_Instance_To_Parent", "addInstToPar", "instance", "parent"),
        ADD_LABEL ("Add_Label", "addLab", "subj_label", "prop_label", "obj_label"),
        ADD_MEASURE ("Add_Measure", "addMeas", "measure"),
        ADD_MEASURE_VALUE_TO_OBSERVATION ("Add_Measure_Value_To_Observation", "addMeasVToOb", "observation", "measure", "measure_val"),
        ADD_OBSERVATION ("Add_Observation", "addOb", "observation"),
        ADD_RELEVANCY ("Add_Relevancy", "addRel", "1st_arg", "2nd_arg"),
        ADD_UNKNOWN_PROPERTY ("Add_Unknown_Property", "addUnProp", "subj_un_prop", "prop_un_prop", "obj_un_prop"),
        ATTACH_ATTR_TO_DIMENSION ("Attach_Attr_To_Dimension", "attAttToDim", "dimension", "attribute"),
        ATTACH_ATTR_TO_MEASURE ("Attach_Attr_To_Measure", "attachAttToMeas", "measure", "attribute"),
        ATTACH_CODELIST_TO_DIMENSION ("Attach_Codelist_To_Dimension", "attClistToDim", "dimension", "codelist"),
        ATTACH_DATATYPE_TO_DIMENSION ("Attach_Datatype_To_Dimension", "attDtypeToDim", "dimension", "datatype"),
        ATTACH_DIMENSION_TO_FT ("Attach_Dimension_To_FT", "attDimToFT", "dimension", "fact_table"),
        ATTACH_HIERARCHY_TO_DIMENSION ("Attach_Hierarchy_To_Dimension", "attHierToDim", "dimension", "hierarchy"),
        ATTACH_INSTANCE_TO_CODELIST ("Attach_Instance_To_Codelist", "attInstToClist", "codelist", "instance"),
        ATTACH_INSTANCE_TO_HIERARCHY ("Attach_Instance_To_Hierarchy", "attInstToHier", "hierarchy", "instance"),
        ATTACH_INSTANCE_TO_PARENT ("Attach_Instance_To_Parent", "attInstToPar", "instance", "parent"),
        ATTACH_MEASURE_TO_FT ("Attach_Measure_To_FT", "attMeasToFT", "measure", "fact_table"),
        ATTACH_OBSERVATION_TO_FT ("Attach_Observation_To_FT", "attObToFT", "observation", "fact_table"),
        ATTACH_TYPE_TO_MEASURE ("Attach_Type_To_Measure", "attTypeToMeas", "type", "measure"),
        DELETE_ATTRIBUTE ("Delete_Attribute", "delAtt", "attribute"),
        DELETE_CODELIST ("Delete_Codelist", "delClist", "codelist"),
        DELETE_DATATYPE ("Delete_Datatype", "delDtype", "1st_arg", "subject", "2nd_arg", "datatype"),
        DELETE_DIMENSION ("Delete_Dimension", "delDim", "dimension"),
        DELETE_DIMENSION_VALUE_FROM_OBSERVATION ("Delete_Dimension_Value_From_Observation", "delDimVFromOb", "observation", "dimension", "dimension_val"),
        DELETE_FACT_TABLE ("Delete_Fact_Table", "delFT", "fact_table"),
        DELETE_GENERIC_ATTRIBUTE ("Delete_Generic_Attribute", "delGenAtt", "subj", "attribute"),
        DELETE_GENERIC_VALUE_FROM_OBSERVATION ("Delete_Generic_Value_From_Observation", "addGenVFromOb", "observation", "property", "value"),
        DELETE_HIERARCHY ("Delete_Hierarchy", "delH", "hierarchy"),
        DELETE_INSCHEME ("Delete_inScheme", "delInSch", "subj", "scheme"),
        DELETE_INSTANCE ("Delete_Instance", "delInst", "instance"),
        DELETE_INSTANCE_FROM_PARENT ("Delete_Instance_From_Parent", "delInstFromPar", "instance", "parent"),
        DELETE_LABEL ("Delete_Label", "delLab", "subj_label", "prop_label", "obj_label"),
        DELETE_MEASURE ("Delete_Measure", "delMeas", "measure"),
        DELETE_MEASURE_VALUE_FROM_OBSERVATION ("Delete_Measure_Value_From_Observation", "delMeasVFromOb","observation", "measure", "measure_val"),
        DELETE_OBSERVATION ("Delete_Observation", "delOb", "observation"),
        DELETE_RELEVANCY ("Delete_Relevancy", "delRel", "1st_arg", "subject", "2nd_arg", "datatype"),
        DELETE_UNKNOWN_PROPERTY ("Delete_Unknown_Property", "delUnProp", "subj_un_prop", "prop_un_prop", "obj_un_prop"),
        DETACH_ATTR_FROM_DIMENSION ("Detach_Attr_From_Dimension", "detachAttFromDim", "dimension", "attribute", "datatype"),
        DETACH_ATTR_FROM_MEASURE ("Detach_Attr_From_Measure", "detAttFromMeas", "measure", "attribute"),
        DETACH_CODELIST_FROM_DIMENSION ("Detach_Codelist_From_Dimension", "detClistFromDim", "codelist", "dimension"),
        DETACH_DATATYPE_FROM_DIMENSION ("Detach_Datatype_From_Dimension", "detDtypeFromDim", "dimension", "attribute", "datatype"),
        DETACH_DIMENSION_FROM_FT ("Detach_Dimension_From_FT", "ddfft", "dimension", "fact_table"),
        DETACH_HIERARCHY_FROM_DIMENSION ("Detach_Hierarchy_From_Dimension", "detHierFromDim", "dimension", "hierarchy"),
        DETACH_INSTANCE_FROM_CODELIST ("Detach_Instance_From_Codelist", "detInstFromClist", "codelist", "hierarchy", "instance"),
        DETACH_INSTANCE_FROM_HIERARCHY ("Detach_Instance_From_Hierarchy", "detInstFromHier", "hierarchy", "instance"),
        DETACH_INSTANCE_FROM_PARENT ("Detach_Instance_From_Parent", "detInstFromPar", "instance", "parent"),
        DETACH_MEASURE_FROM_FT ("Detach_Measure_From_FT", "detMeasToFT", "measure", "fact_table"),
        DETACH_OBSERVATION_FROM_FT ("Detach_Observation_From_FT", "detObFromFT", "observation", "fact_table"),
        DETACH_TYPE_FROM_MEASURE ("Detach_Type_From_Measure", "detTypeFromMeas", "type", "measure");

        private final String uriName;
        private final String parameterBaseName;
        private List<String> parameters;

        Type(String uriName, String parameterBaseName, String... parameters) {
            this.uriName = uriName;
            this.parameterBaseName = parameterBaseName;
            this.parameters = Collections.unmodifiableList(Arrays.asList(parameters));
        }

        public List<String> getParameters() {
            return parameters;
        }

        public boolean hasParameter(String name) {
            return parameters.contains(name);
        }

        public int getParameterIdFromName(String parameterName) {
            final int id = parameters.indexOf(parameterName);
            if (id < 0) {
                throw new IllegalArgumentException("Unknown parameter name "+parameterName+" for change type "+name());
            }
            return id+1;
        }

        public String getUriName() {
            return uriName;
        }

        public String getParameterId(int id) {
            return parameterBaseName+"_p"+id;
        }
    }


}
