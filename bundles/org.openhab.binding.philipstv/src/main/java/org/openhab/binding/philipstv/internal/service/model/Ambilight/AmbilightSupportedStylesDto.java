package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The {@link AmbilightSupportedStylesDto} class defines the Data Transfer Object
 * for the Philips TV API /ambilight/supportedstyles endpoint to retrieve all ambilight styles with their algorithms.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class AmbilightSupportedStylesDto {

	@JsonProperty("supportedStyles")
	private List<AmbilightSupportedStyleDto> supportedStyles;

	public void setSupportedStyles(List<AmbilightSupportedStyleDto> supportedStyles){
		this.supportedStyles = supportedStyles;
	}

	public List<AmbilightSupportedStyleDto> getSupportedStyles(){
		return supportedStyles;
	}

	@Override
 	public String toString(){
		return 
			"AmbilightSupportedStyles{" + 
			"supportedStyles = '" + supportedStyles + '\'' + 
			"}";
		}
}