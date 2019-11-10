package org.openhab.binding.philipstv.internal.service.model.ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;


public class AmbilightLoungeDto {

	@JsonProperty("color")
	private AmbilightColorDto color;

	public void setColor(AmbilightColorDto color){
		this.color = color;
	}

	public AmbilightColorDto getColor(){
		return color;
	}

	@Override
 	public String toString(){
		return 
			"AmbilightLoungeDto{" + 
			"color = '" + color + '\'' +
			"}";
		}
}