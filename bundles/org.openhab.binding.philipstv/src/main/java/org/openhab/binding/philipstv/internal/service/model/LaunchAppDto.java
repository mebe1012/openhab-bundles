package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link LaunchAppDto} class defines the Data Transfer Object (POJO)
 * for the Philips TV API /activities/launch endpoint for launching TV apps and launching search for content.
 * @author Benjamin Meyer - initial contribution
 */
public class LaunchAppDto{

	@JsonProperty
	private IntentDto intent;

	public void setIntent(IntentDto intent){
		this.intent = intent;
	}

	public IntentDto getIntent(){
		return intent;
	}

	@Override
 	public String toString(){
		return 
			"LaunchAppDto{" + 
			"intent = '" + intent + '\'' + 
			"}";
		}
}