/*
 * Copyright 2021-2026 OpenAIRE AMKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.uoa.di.madgik.catalogue.http;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicAddressGuardTest {

    private final PublicAddressGuard guard = new PublicAddressGuard(false);

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "0.0.0.0", "10.1.2.3", "172.16.0.1", "192.168.1.1", "169.254.169.254",
            "100.64.0.1", "224.0.0.1", "240.0.0.1", "::1", "fe80::1", "fd00::1", "::ffff:127.0.0.1"})
    void nonPublicAddressesAreRefused(String address) throws Exception {
        assertFalse(PublicAddressGuard.isPublic(InetAddress.getByName(address)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"8.8.8.8", "1.1.1.1", "2001:4860:4860::8888"})
    void publicAddressesAreAllowed(String address) throws Exception {
        assertTrue(PublicAddressGuard.isPublic(InetAddress.getByName(address)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://127.0.0.1/", "http://localhost:8080/x", "http://169.254.169.254/latest/meta-data/",
            "http://[::1]/", "http://10.0.0.5/"})
    void urlsToNonPublicHostsAreBlocked(String url) {
        assertThrows(BlockedUrlException.class, () -> guard.check(URI.create(url)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"file:///etc/passwd", "ftp://example.org/", "gopher://example.org/", "http:///nohost"})
    void otherSchemesAndHostlessUrlsAreBlocked(String url) {
        assertThrows(BlockedUrlException.class, () -> guard.check(URI.create(url)));
    }

    @Test
    void literalPublicAddressIsAllowedWithoutLookup() {
        assertDoesNotThrow(() -> guard.check(URI.create("https://8.8.8.8/")));
    }

    @Test
    void optOutChecksOnlyTheScheme() {
        PublicAddressGuard permissive = new PublicAddressGuard(true);

        assertDoesNotThrow(() -> permissive.check(URI.create("http://127.0.0.1:8080/")));
        assertThrows(BlockedUrlException.class, () -> permissive.check(URI.create("file:///etc/passwd")));
    }

    @Test
    void allowsReportsTheCheckWithoutThrowing() {
        assertFalse(guard.allows(URI.create("http://127.0.0.1/")));
        assertTrue(guard.allows(URI.create("https://8.8.8.8/")));
    }
}
