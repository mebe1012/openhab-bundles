package org.openhab.binding.philipstv.internal.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link LaunchAppDto} and {@link CurrentAppDto}
 *
 * @author Benjamin Meyer - initial contribution
 */
public class ComponentDto {

    @JsonProperty
    private String className;

    @JsonProperty
    private String packageName;

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String toString() {
        return "Component{" + "className = '" + className + '\'' + ",packageName = '" + packageName + '\'' + "}";
    }
}