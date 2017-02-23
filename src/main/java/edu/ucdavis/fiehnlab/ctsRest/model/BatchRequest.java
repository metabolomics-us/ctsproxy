package edu.ucdavis.fiehnlab.ctsRest.model;

import java.util.List;

/**
 * Created by diego on 2/15/2017.
 */
public class BatchRequest {
	public String from;
	public List<String> to;
	public List<String> searchTerms;

	public BatchRequest() {
	}

	public BatchRequest(String from, List<String> to, List<String> searchTerms) {
		this.from = from;
		this.to = to;
		this.searchTerms = searchTerms;
	}
}
