package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link TvSettingsCurrentDto}
 *
 * @author Benjamin Meyer - initial contribution
 */
public class NodesDto {

	@JsonProperty("nodeid")
	private int nodeid;

	public void setNodeid(int nodeid){
		this.nodeid = nodeid;
	}

	public int getNodeid(){
		return nodeid;
	}

	@Override
 	public String toString(){
		return 
			"NodesItem{" + 
			"nodeid = '" + nodeid + '\'' + 
			"}";
		}
}