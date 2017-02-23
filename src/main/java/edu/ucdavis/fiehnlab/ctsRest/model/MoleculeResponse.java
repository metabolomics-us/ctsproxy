package edu.ucdavis.fiehnlab.ctsRest.model;

/**
 * Created by diego on 2/16/2017.
 */
public class MoleculeResponse {
	public String molecule;
	public String message;

	@Override
	public String toString() {
		return molecule + "\n" + message;
	}
}
