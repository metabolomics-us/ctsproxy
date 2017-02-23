package edu.ucdavis.fiehnlab.ctsRest.model;

import java.util.List;

/**
 * Created by diego on 2/16/2017.
 */
public class CompoundResponse {
	public String inchikey;
	public String inchicode;
	public double molweight;
	public double exactmass;
	public String formula;
	public List<SynonymDto> synonyms;
	public List<ExtidDto> externalIds;

	@Override
	public String toString() {
		return "inchikey: " + inchikey + ", synonyms(" + synonyms.size() + "), extids(" + externalIds.size() + ")";
	}
}
