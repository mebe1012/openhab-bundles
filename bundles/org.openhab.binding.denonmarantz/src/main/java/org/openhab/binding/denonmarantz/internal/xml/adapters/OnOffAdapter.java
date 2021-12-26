/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.denonmarantz.internal.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Maps 'On' and 'Off' string values to a boolean
 *
 * @author Jeroen Idserda - Initial contribution
 */
public class OnOffAdapter extends XmlAdapter<String, Boolean> {

    @Override
    public Boolean unmarshal(String v) throws Exception {
        if (v != null) {
            return Boolean.valueOf(v.toLowerCase().equals("on"));
        }

        return Boolean.FALSE;
    }

    @Override
    public String marshal(Boolean v) throws Exception {
        return v ? "On" : "Off";
    }
}
