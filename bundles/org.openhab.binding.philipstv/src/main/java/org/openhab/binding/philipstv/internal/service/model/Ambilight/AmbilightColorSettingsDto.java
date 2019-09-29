package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AmbilightColorSettingsDto {

    @JsonProperty("color")
    private AmbilightColorDto color;

    @JsonProperty("colorDelta")
    private AmbilightColorDeltaDto colorDelta;

    @JsonProperty("speed")
    private int speed;

    public void setColor(AmbilightColorDto color){
        this.color = color;
    }

    public AmbilightColorDto getColor(){
        return color;
    }

    public void setColorDelta(AmbilightColorDeltaDto colorDelta){
        this.colorDelta = colorDelta;
    }

    public AmbilightColorDeltaDto getColorDelta(){
        return colorDelta;
    }

    public void setSpeed(int speed){
        this.speed = speed;
    }

    public int getSpeed(){
        return speed;
    }

    @Override
    public String toString(){
        return
                "ColorSettings{" +
                        "color = '" + color + '\'' +
                        ",colorDelta = '" + colorDelta + '\'' +
                        ",speed = '" + speed + '\'' +
                        "}";
    }
}