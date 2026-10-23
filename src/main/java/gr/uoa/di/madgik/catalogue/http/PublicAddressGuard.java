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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Decides whether a URL taken from a request may be fetched by the server: only {@code http} and
 * {@code https} URLs whose host resolves exclusively to public addresses. Loopback, wildcard, private,
 * link-local (including cloud metadata endpoints), carrier-grade NAT, multicast and IPv6 unique-local
 * addresses are refused.
 *
 * <p>The host is resolved again by the HTTP client when the request is made, so a DNS answer that changes in
 * between (DNS rebinding) is not covered.
 */
public class PublicAddressGuard {

    private final boolean allowPrivateAddresses;

    /**
     * @param allowPrivateAddresses when {@code true}, only the scheme is checked
     */
    public PublicAddressGuard(boolean allowPrivateAddresses) {
        this.allowPrivateAddresses = allowPrivateAddresses;
    }

    /**
     * @throws BlockedUrlException if the URL may not be fetched
     */
    public void check(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new BlockedUrlException("Only http and https URLs are allowed: " + uri);
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new BlockedUrlException("URL has no host: " + uri);
        }
        if (allowPrivateAddresses) {
            return;
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new BlockedUrlException("Host could not be resolved: " + host);
        }
        for (InetAddress address : addresses) {
            if (!isPublic(address)) {
                throw new BlockedUrlException("URL points to a non-public address: " + uri);
            }
        }
    }

    /**
     * Returns whether the URL may be fetched, without throwing.
     */
    public boolean allows(URI uri) {
        try {
            check(uri);
            return true;
        } catch (BlockedUrlException e) {
            return false;
        }
    }

    static boolean isPublic(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            int first = bytes[0] & 0xff;
            int second = bytes[1] & 0xff;
            // 0.0.0.0/8, 100.64.0.0/10 (carrier-grade NAT), 192.0.0.0/24, 198.18.0.0/15, 240.0.0.0/4
            return first != 0
                    && !(first == 100 && second >= 64 && second <= 127)
                    && !(first == 192 && second == 0 && (bytes[2] & 0xff) == 0)
                    && !(first == 198 && (second == 18 || second == 19))
                    && first < 240;
        }
        if (address instanceof Inet6Address) {
            // fc00::/7 unique-local
            return (bytes[0] & 0xfe) != 0xfc;
        }
        return true;
    }
}
