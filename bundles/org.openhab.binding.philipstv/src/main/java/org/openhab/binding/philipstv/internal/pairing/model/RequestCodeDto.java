package org.openhab.binding.philipstv.internal.pairing.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

public class RequestCodeDto {

	@JsonProperty("scope")
	private List<String> scope;

	@JsonProperty("device")
	private DeviceDto device;

	public void setScope(List<String> scope){
		this.scope = scope;
	}

	public List<String> getScope(){
		return scope;
	}

	public void setDevice(DeviceDto device){
		this.device = device;
	}

	public DeviceDto getDevice(){
		return device;
	}

	@Override
 	public String toString(){
		return 
			"RequestPinDto{" + 
			"scope = '" + scope + '\'' + 
			",device = '" + device + '\'' + 
			"}";
		}
}