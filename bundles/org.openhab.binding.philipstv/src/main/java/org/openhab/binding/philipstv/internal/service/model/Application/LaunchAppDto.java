package org.openhab.binding.philipstv.internal.service.model.Application;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link LaunchAppDto} class defines the Data Transfer Object
 * for the Philips TV API /activities/launch endpoint for launching TV apps and launching search for content.
 *
 * @author Benjamin Meyer - Initial contribution
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