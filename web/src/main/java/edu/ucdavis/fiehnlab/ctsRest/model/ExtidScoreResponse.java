package edu.ucdavis.fiehnlab.ctsRest.model;

/**
 * Created by diego on 3/6/2018.
 */
public class ExtidScoreResponse {
    public String external_id = "";
    public int count = 0;

    @Override
    public String toString() {
        return "external id: " + external_id + " => count: " + count;
    }
}
