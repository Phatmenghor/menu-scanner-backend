package com.emenu.shared.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserAgentParser {

    public static ParsedUserAgent parse(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return new ParsedUserAgent("Unknown", "Unknown", "Unknown");
        }

        String browser = parseBrowser(userAgent);
        String browserVersion = parseBrowserVersion(userAgent);
        String os = parseOS(userAgent);

        return new ParsedUserAgent(browser, browserVersion, os);
    }

    private static String parseBrowser(String ua) {
        String uaLower = ua.toLowerCase();

        if (uaLower.contains("edg/")) return "Edge";
        if (uaLower.contains("chrome/") && !uaLower.contains("edg")) return "Chrome";
        if (uaLower.contains("firefox/")) return "Firefox";
        if (uaLower.contains("safari/") && !uaLower.contains("chrome")) return "Safari";
        if (uaLower.contains("opera/") || uaLower.contains("opr/")) return "Opera";
        if (uaLower.contains("msie") || uaLower.contains("trident/")) return "Internet Explorer";

        return "Unknown";
    }

    private static String parseBrowserVersion(String ua) {
        try {
            String browser = parseBrowser(ua);
            String versionKey = switch (browser) {
                case "Edge" -> "edg/";
                case "Chrome" -> "chrome/";
                case "Firefox" -> "firefox/";
                case "Safari" -> "version/";
                case "Opera" -> "opr/";
                default -> null;
            };

            if (versionKey != null) {
                int index = ua.toLowerCase().indexOf(versionKey);
                if (index != -1) {
                    String versionPart = ua.substring(index + versionKey.length());
                    String[] parts = versionPart.split("[\\s;)]");
                    if (parts.length > 0) {
                        String version = parts[0];
                        // Get major version only
                        String[] versionParts = version.split("\\.");
                        return versionParts.length > 0 ? versionParts[0] : version;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error parsing browser version: {}", e.getMessage());
        }
        return "";
    }

    private static String parseOS(String ua) {
        String uaLower = ua.toLowerCase();

        if (uaLower.contains("windows nt 10")) return "Windows 10";
        if (uaLower.contains("windows nt 6.3")) return "Windows 8.1";
        if (uaLower.contains("windows nt 6.2")) return "Windows 8";
        if (uaLower.contains("windows nt 6.1")) return "Windows 7";
        if (uaLower.contains("windows")) return "Windows";

        if (uaLower.contains("mac os x")) {
            int index = uaLower.indexOf("mac os x");
            String osPart = ua.substring(index);
            if (osPart.length() > 20) {
                return "macOS";
            }
            return "macOS";
        }

        if (uaLower.contains("iphone")) return "iOS";
        if (uaLower.contains("ipad")) return "iPadOS";
        if (uaLower.contains("android")) return "Android";
        if (uaLower.contains("linux")) return "Linux";

        return "Unknown";
    }

    public static class ParsedUserAgent {
        private final String browser;
        private final String browserVersion;
        private final String os;

        public ParsedUserAgent(String browser, String browserVersion, String os) {
            this.browser = browser;
            this.browserVersion = browserVersion;
            this.os = os;
        }

        public String getBrowser() {
            return browser;
        }

        public String getBrowserVersion() {
            return browserVersion;
        }

        public String getOs() {
            return os;
        }

        public String getBrowserWithVersion() {
            if (browserVersion != null && !browserVersion.isEmpty()) {
                return browser + " " + browserVersion;
            }
            return browser;
        }
    }
}
