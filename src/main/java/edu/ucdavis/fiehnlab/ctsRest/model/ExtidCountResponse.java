package edu.ucdavis.fiehnlab.ctsRest.model;

/**
 * Created by diego on 2/16/2017.
 */
public class ExtidCountResponse {
	public int datasource_count;

	@Override
	public String toString() {
		return "count: " + datasource_count;
	}
}
