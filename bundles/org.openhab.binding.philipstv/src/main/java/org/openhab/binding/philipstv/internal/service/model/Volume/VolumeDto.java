package org.openhab.binding.philipstv.internal.service.model.Volume;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link VolumeDto} class defines the Data Transfer Object (POJO)
 * for the Philips TV API /audio/volume endpoint.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class VolumeDto {

  @JsonProperty("current")
  private String currentVolume;

  @JsonProperty
  private boolean muted;

  public String getCurrentVolume() {
    return currentVolume;
  }

  public void setCurrentVolume(String currentVolume) {
    this.currentVolume = currentVolume;
  }

  public boolean isMuted() {
    return muted;
  }

  public void setMuted(boolean muted) {
    this.muted = muted;
  }
}
