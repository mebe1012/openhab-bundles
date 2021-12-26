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
package org.openhab.binding.gardena.internal.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to convert a String to a date or calendar.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DateUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);
    private static final String[] DATE_FORMATS = new String[] { "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm'Z'" };

    /**
     * Converts a string to a Date, trying different date formats used by Gardena.
     */
    public static Date parseToDate(String text) {
        if (StringUtils.isNotBlank(text)) {
            Date parsedDate = null;
            for (String dateFormat : DATE_FORMATS) {
                try {
                    parsedDate = new SimpleDateFormat(dateFormat).parse(text);
                    ZonedDateTime gmt = ZonedDateTime.ofInstant(parsedDate.toInstant(), ZoneOffset.UTC);
                    LocalDateTime here = gmt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                    parsedDate = Date.from(here.toInstant(ZoneOffset.UTC));
                    break;
                } catch (ParseException ex) {
                }
            }
            if (parsedDate == null) {
                LOGGER.error("Can't parse date {}", text);
            }
            return parsedDate;
        } else {
            return null;
        }
    }

    /**
     * Converts a string to a Calendar, trying different date formats used by Gardena.
     */
    public static Calendar parseToCalendar(String text) {
        Date parsedDate = parseToDate(text);
        if (parsedDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(parsedDate);
            return cal;
        }
        return null;
    }
}
