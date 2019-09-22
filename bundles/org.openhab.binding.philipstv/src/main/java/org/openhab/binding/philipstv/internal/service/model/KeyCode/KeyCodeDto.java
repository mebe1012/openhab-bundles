package org.openhab.binding.philipstv.internal.service.model.KeyCode;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openhab.binding.philipstv.internal.service.KeyCode;

/**
 * The {@link KeyCodeDto} class defines the Data Transfer Object
 * for the Philips TV API /input/key endpoint for remote controller emulation.
 * @author Benjamin Meyer - initial contribution
 */
public class KeyCodeDto {

    @JsonProperty
    private KeyCode key;

    public KeyCode getKey() {
        return key;
    }

    public void setKey(KeyCode key) {
        this.key = key;
    }
}
