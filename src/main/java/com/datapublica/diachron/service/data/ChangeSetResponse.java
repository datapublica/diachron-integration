package com.datapublica.diachron.service.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by loic on 10/12/2015.
 */
public class ChangeSetResponse {
    private Map<String, DifferenceValue> resolved;
    private List<Difference> results;
    private long total;
    private Facets facets;

    public List<Difference> getResults() {
        return results;
    }

    public void setResults(List<Difference> results) {
        this.results = results;
    }

    public Facets getFacets() {
        return facets;
    }

    public void setFacets(Facets facets) {
        this.facets = facets;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Map<String, DifferenceValue> getResolved() {
        return resolved;
    }

    public void setResolved(Map<String, DifferenceValue> resolved) {
        this.resolved = resolved;
    }

    public static class Facets {
        private Map<Difference.Type, Long> types;
        private Map<String, Map<String, Long>> parameters = new LinkedHashMap<>();
        private Map<Difference.Type, Long> joinTypes;

        public Map<Difference.Type, Long> getJoinTypes() {
            return joinTypes;
        }

        public void setJoinTypes(Map<Difference.Type, Long> joinTypes) {
            this.joinTypes = joinTypes;
        }

        public Map<Difference.Type, Long> getTypes() {
            return types;
        }

        public void setTypes(Map<Difference.Type, Long> types) {
            this.types = types;
        }

        public Map<String, Map<String, Long>> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Map<String, Long>> parameters) {
            this.parameters = parameters;
        }
    }
}
