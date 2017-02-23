package edu.ucdavis.fiehnlab.ctsRest.model;

import java.util.List;
import java.util.Map;

/**
 * Created by diego on 2/16/2017.
 */
public class HitResponse {
	public String result;
	public String query;
	public String algorithm;
	public double score;
	public String scoring_algorithm;
	public List<Map<String, String>> enhancements;

	@Override
	public String toString() {
		return "query: " + query + "\nresult: " + result + "\nscore: " + score + "\nalgorithm: " + algorithm + "\nscoring algorithm: " + scoring_algorithm + "\nenhancements: " + enhancements.toString();
	}
}
