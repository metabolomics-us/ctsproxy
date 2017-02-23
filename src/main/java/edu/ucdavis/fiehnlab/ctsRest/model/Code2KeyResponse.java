package edu.ucdavis.fiehnlab.ctsRest.model;

/**
 * Created by diego on 2/16/2017.
 */
public class Code2KeyResponse {
	public String inchicode;
	public String inchikey;

	@Override
	public String toString() {
		return "inchikey: " + inchikey + "\ninchi code: " + inchicode;
	}
}
