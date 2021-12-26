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
package org.openhab.binding.sleepiq.api.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class SleepersResponseTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception {
        SleepersResponse sleepersResponse = new SleepersResponse()
                .withSleepers(Arrays.asList(new Sleeper().withFirstName("Alice"), new Sleeper().withFirstName("Bob")));
        assertEquals(readJson("sleepers-response.json"), gson.toJson(sleepersResponse));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("sleepers-response.json"))) {
            SleepersResponse sleepersResponse = gson.fromJson(reader, SleepersResponse.class);

            List<Sleeper> sleepers = sleepersResponse.getSleepers();
            assertNotNull(sleepers);
            assertEquals(2, sleepers.size());
            assertEquals("Alice", sleepers.get(0).getFirstName());
            assertEquals("Bob", sleepers.get(1).getFirstName());
        }
    }
}
