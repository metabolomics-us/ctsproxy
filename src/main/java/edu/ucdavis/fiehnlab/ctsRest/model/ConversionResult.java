package edu.ucdavis.fiehnlab.ctsRest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diego on 2/15/2017.
 */
public class ConversionResult {
    @JsonProperty("fromIdentifier")
    public String from;
    @JsonProperty("toIdentifier")
    public String to;
    @JsonProperty("searchTerm")
    public String searchTerm;
    @JsonProperty("result")
    public List<String> results;

    public ConversionResult(String f, String t, String st, List<String> r) {
        from = f;
        to = t;
        searchTerm = st;
        results = r;
    }

    @Override
    public String toString() {
        return (searchTerm + "'s " + to + "s: " + results.toString());
    }
}
