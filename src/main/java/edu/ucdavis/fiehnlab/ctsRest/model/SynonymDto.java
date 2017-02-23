package edu.ucdavis.fiehnlab.ctsRest.model;

/**
 * Created by diego on 2/21/2017.
 */
public class SynonymDto {
	public String type;
	public String name;
	public int score;

	public SynonymDto() {
	}

	public SynonymDto(String name, String type, int score) {
		this.name = name;
		this.type = type;
		this.score = score;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SynonymDto) {
			SynonymDto sOther = (SynonymDto) other;
			if (name.equals(sOther.name) && type.equals(sOther.type) && score == sOther.score)
				return true;
		}

		return false;
	}
}

