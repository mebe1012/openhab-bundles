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
package org.openhab.binding.lametrictime.api.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.api.test.AbstractTest;

public class FileIconTest extends AbstractTest
{
    @Test
    public void testLocalPathGif()
    {
        FileIcon icon = new FileIcon(getTestDataPath("smile.gif"));
        assertEquals("data:image/gif;base64,R0lGODlhCAAIAPEAAPz+BPz+/AAAAAAAACH5BAkKAAIAIf8LTkVUU0NBUEUyLjADAQAAACwAAAAACAAIAAACEZSAYJfIElREIdaGs3PPNFMAACH5BAkKAAIALAAAAAAIAAgAAAIRlIBgl8gSVEQh1oazU4szJxQAIfkECTIAAgAsAAAAAAgACAAAAhKUgGCXyBJURCHWhlU7fCmzCQUAIfkECRQAAgAsAAAAAAgACAAAAhGUgGCXyBIaClFa1Y5eymRRAAAh+QQJMgACACwAAAAACAAIAAACEpSAYJfIElREIdaGVTt8KbMJBQA7",
                     icon.toRaw());
    }

    @Test
    public void testLocalPathPng()
    {
        FileIcon icon = new FileIcon(getTestDataPath("info.png"));
        assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAL0lEQVQYlWN0NPv3nwEPYIEx9p1kRJFwMofoY0IXQGczMRAAVFSA7EhkNiMhbwIAA/sN+bH1CpgAAAAASUVORK5CYII=",
                     icon.toRaw());
    }

    @Test
    public void testLocalFileGif()
    {
        FileIcon icon = new FileIcon(getTestDataFile("smile.gif"));
        assertEquals("data:image/gif;base64,R0lGODlhCAAIAPEAAPz+BPz+/AAAAAAAACH5BAkKAAIAIf8LTkVUU0NBUEUyLjADAQAAACwAAAAACAAIAAACEZSAYJfIElREIdaGs3PPNFMAACH5BAkKAAIALAAAAAAIAAgAAAIRlIBgl8gSVEQh1oazU4szJxQAIfkECTIAAgAsAAAAAAgACAAAAhKUgGCXyBJURCHWhlU7fCmzCQUAIfkECRQAAgAsAAAAAAgACAAAAhGUgGCXyBIaClFa1Y5eymRRAAAh+QQJMgACACwAAAAACAAIAAACEpSAYJfIElREIdaGVTt8KbMJBQA7",
                     icon.toRaw());
    }

    @Test
    public void testLocalFilePng()
    {
        FileIcon icon = new FileIcon(getTestDataFile("info.png"));
        assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAL0lEQVQYlWN0NPv3nwEPYIEx9p1kRJFwMofoY0IXQGczMRAAVFSA7EhkNiMhbwIAA/sN+bH1CpgAAAAASUVORK5CYII=",
                     icon.toRaw());
    }
}
