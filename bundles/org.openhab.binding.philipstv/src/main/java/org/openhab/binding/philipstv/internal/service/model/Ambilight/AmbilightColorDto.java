package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AmbilightColorDto {

    @JsonProperty("saturation")
    private int saturation;

    @JsonProperty("brightness")
    private int brightness;

    @JsonProperty("hue")
    private int hue;

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getHue() {
        return hue;
    }

    @Override
    public String toString() {
        return "Color{" + "saturation = '" + saturation + '\'' + ",brightness = '" + brightness + '\'' + ",hue = '" +
                hue + '\'' + "}";
    }
}