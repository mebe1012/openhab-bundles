package org.openhab.binding.philipstv.internal.service.model;

public class VolumeDetails {

  private String currentVolume;
  private boolean muted;

  public static VolumeDetails ofCurrentVolumeAndMuted(String currentVolume, boolean muted) {
    VolumeDetails volumeDetails = new VolumeDetails();
    volumeDetails.setCurrentVolume(currentVolume);
    volumeDetails.setMuted(muted);
    return volumeDetails;
  }

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
