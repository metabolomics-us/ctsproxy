package edu.ucdavis.fiehnlab.ctsRest.model;

/**
 * Created by diego on 2/16/2017.
 */
public class FormulaResponse {
	public String formula;
	public String result;
	public String error;

	@Override
	public String toString() {
		return "Formula: " + formula + "\n" + (error == null ? "Error: " + error : "Expanded: " + result);
	}
}
