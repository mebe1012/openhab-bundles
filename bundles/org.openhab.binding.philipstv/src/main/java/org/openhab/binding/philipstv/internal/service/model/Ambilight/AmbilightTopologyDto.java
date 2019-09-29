package org.openhab.binding.philipstv.internal.service.model.Ambilight;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AmbilightTopologyDto} class defines the Data Transfer Object
 * for the Philips TV API /ambilight/topology endpoint to retrieve the ambilight topology information.
 * <p> Endpoint returns:
 * <p> layers (integer): The number of layers.
 * <p> left (integer): The number of pixels on the left.
 * <p> top (integer): The number of pixels on the top.
 * <p> right (integer): The number of pixels on the right.
 * <p> bottom (integer): The number of pixels on the bottom.
 *
 * @author Benjamin Meyer - initial contribution
 */
public class AmbilightTopologyDto {

    @JsonProperty("top")
    private int top;

    @JsonProperty("left")
    private int left;

    @JsonProperty("bottom")
    private int bottom;

    @JsonProperty("layers")
    private int layers;

    @JsonProperty("right")
    private int right;

    public void setTop(int top) {
        this.top = top;
    }

    public int getTop() {
        return top;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getLeft() {
        return left;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getBottom() {
        return bottom;
    }

    public void setLayers(int layers) {
        this.layers = layers;
    }

    public int getLayers() {
        return layers;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getRight() {
        return right;
    }

    @JsonIgnore
    public int getPixelSizeForGivenSide(String side) {
        int value;
        switch (side) {
        case "left":
            value = left;
            break;
        case "right":
            value = right;
            break;
        case "top":
            value = top;
            break;
        case "bottom":
            value = bottom;
            break;
        default:
            throw new IllegalStateException("Unexpected side: " + side);
        }
        return value;
    }

    @Override
    public String toString() {
        return "AmbilightTopologyDto{" + "top = '" + top + '\'' + ",left = '" + left + '\'' + ",bottom = '" + bottom +
                '\'' + ",layers = '" + layers + '\'' + ",right = '" + right + '\'' + "}";
    }
}