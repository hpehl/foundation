/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.env;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void nil() {
        // Null value should return EMPTY_VERSION
        Version result = Version.parseVersion(null);
        assertEquals(Version.EMPTY_VERSION, result);
    }

    @Test
    void empty() {
        // Empty string should return EMPTY_VERSION
        Version result = Version.parseVersion("");
        assertEquals(Version.EMPTY_VERSION, result);
    }

    @Test
    void validSemVer() {
        // Parse value that should parse correctly
        Version result = Version.parseVersion("1.2.3.Final");
        assertEquals(1, result.major());
        assertEquals(2, result.minor());
        assertEquals(3, result.micro());
        assertEquals("Final", result.qualifier());
    }

    @Test
    void validMaven() {
        // Parse value that should parse correctly
        Version result = Version.parseVersion("1.2.3-SNAPSHOT");
        assertEquals(1, result.major());
        assertEquals(2, result.minor());
        assertEquals(3, result.micro());
        assertEquals("SNAPSHOT", result.qualifier());
    }

    @Test
    void invalidFormat() {
        // Version format is invalid, so exception is expected
        assertThrows(IllegalArgumentException.class, () -> Version.parseVersion("1.2.3.alpha.4"));
    }

    @Test
    void invalidQualifier() {
        // Qualifiers are invalid, so exception is expected
        assertThrows(IllegalArgumentException.class, () -> Version.parseVersion("1.2.3-@lpha"));
    }

    @Test
    void negativeNumbers() {
        // Negative numbers in version is invalid, so exception is expected
        assertThrows(IllegalArgumentException.class, () -> Version.parseVersion("-1.2.3"));
    }

    @Test
    void sameVersions() {
        Version version1 = Version.parseVersion("1.2.3-alpha");
        Version version2 = Version.parseVersion("1.2.3-alpha");
        assertEquals(0, version1.compareTo(version2));
    }

    @Test
    void differentMajors() {
        Version version1 = Version.parseVersion("1.2.3-alpha");
        Version version2 = Version.parseVersion("2.2.3-alpha");
        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    void differentMinors() {
        Version version1 = Version.parseVersion("1.2.3-alpha");
        Version version2 = Version.parseVersion("1.3.3-alpha");
        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    void differentMicros() {
        Version version1 = Version.parseVersion("1.2.3-alpha");
        Version version2 = Version.parseVersion("1.2.4-alpha");
        assertTrue(version1.compareTo(version2) < 0);
    }

    @Test
    void differentQualifiers() {
        Version version1 = Version.parseVersion("1.2.3-alpha");
        Version version2 = Version.parseVersion("1.2.3-beta");
        assertTrue(version1.compareTo(version2) < 0);
    }
}
