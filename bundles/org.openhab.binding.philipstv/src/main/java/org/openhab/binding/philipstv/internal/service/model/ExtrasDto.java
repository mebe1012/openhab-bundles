package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link IntentDto}
 *
 * @author Benjamin Meyer - initial contribution
 */
public class ExtrasDto {

    @JsonProperty
    private String query;

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "Extras{" + "query = '" + query + '\'' + "}";
    }
}