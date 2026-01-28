package com.emenu.shared.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Service for IP geolocation lookup using free ip-api.com service
 * Rate limit: 45 requests per minute (free tier)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpGeolocationService {

    private static final String IP_API_URL = "http://ip-api.com/json/";
    private static final int TIMEOUT_SECONDS = 5;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();

    /**
     * Get geolocation info for an IP address (async to not block login)
     */
    @Async
    public CompletableFuture<GeoLocation> getLocationAsync(String ipAddress) {
        return CompletableFuture.supplyAsync(() -> getLocation(ipAddress));
    }

    /**
     * Get geolocation info for an IP address (sync)
     */
    public GeoLocation getLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || isPrivateIp(ipAddress)) {
            return GeoLocation.unknown();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IP_API_URL + ipAddress + "?fields=status,country,countryCode,region,regionName,city,zip,lat,lon,timezone,isp,org,as,query"))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                IpApiResponse apiResponse = objectMapper.readValue(response.body(), IpApiResponse.class);
                if ("success".equals(apiResponse.getStatus())) {
                    return GeoLocation.builder()
                            .country(apiResponse.getCountry())
                            .countryCode(apiResponse.getCountryCode())
                            .region(apiResponse.getRegionName())
                            .city(apiResponse.getCity())
                            .zipCode(apiResponse.getZip())
                            .latitude(apiResponse.getLat())
                            .longitude(apiResponse.getLon())
                            .timezone(apiResponse.getTimezone())
                            .isp(apiResponse.getIsp())
                            .organization(apiResponse.getOrg())
                            .build();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get geolocation for IP: {} - {}", ipAddress, e.getMessage());
        }

        return GeoLocation.unknown();
    }

    /**
     * Check if IP is private/local (not routable on internet)
     */
    private boolean isPrivateIp(String ipAddress) {
        if (ipAddress == null) return true;

        // Localhost
        if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            return true;
        }

        // Private IPv4 ranges
        if (ipAddress.startsWith("10.") ||
            ipAddress.startsWith("172.16.") || ipAddress.startsWith("172.17.") ||
            ipAddress.startsWith("172.18.") || ipAddress.startsWith("172.19.") ||
            ipAddress.startsWith("172.20.") || ipAddress.startsWith("172.21.") ||
            ipAddress.startsWith("172.22.") || ipAddress.startsWith("172.23.") ||
            ipAddress.startsWith("172.24.") || ipAddress.startsWith("172.25.") ||
            ipAddress.startsWith("172.26.") || ipAddress.startsWith("172.27.") ||
            ipAddress.startsWith("172.28.") || ipAddress.startsWith("172.29.") ||
            ipAddress.startsWith("172.30.") || ipAddress.startsWith("172.31.") ||
            ipAddress.startsWith("192.168.")) {
            return true;
        }

        return "Unknown".equals(ipAddress);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IpApiResponse {
        private String status;
        private String country;
        private String countryCode;
        private String region;
        private String regionName;
        private String city;
        private String zip;
        private Double lat;
        private Double lon;
        private String timezone;
        private String isp;
        private String org;
        private String as;
        private String query;
    }

    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GeoLocation {
        private String country;
        private String countryCode;
        private String region;
        private String city;
        private String zipCode;
        private Double latitude;
        private Double longitude;
        private String timezone;
        private String isp;
        private String organization;

        public static GeoLocation unknown() {
            return GeoLocation.builder()
                    .country("Unknown")
                    .city("Unknown")
                    .build();
        }

        public String getLocationDisplay() {
            if (city != null && country != null && !"Unknown".equals(city)) {
                return city + ", " + country;
            }
            if (country != null && !"Unknown".equals(country)) {
                return country;
            }
            return "Unknown Location";
        }
    }
}
