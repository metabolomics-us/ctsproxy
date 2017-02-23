package edu.ucdavis.fiehnlab.ctsRest.model;

/**
 * Created by diego on 2/21/2017.
 */
public class ExtidDto {
	public String name;
	public String value;
	public String url;

	public ExtidDto() {
	}

	public ExtidDto(String name, String value, String url) {
		this.name = name;
		this.value = value;
		this.url = url;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ExtidDto) {
			ExtidDto sOther = (ExtidDto) other;
			if (name.equals(sOther.name) && url.equals(sOther.url) && value.equals(sOther.value))
				return true;
		}

		return false;
	}

}
