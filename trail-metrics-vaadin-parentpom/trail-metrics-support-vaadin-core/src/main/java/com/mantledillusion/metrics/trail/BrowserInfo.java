package com.mantledillusion.metrics.trail;

public final class BrowserInfo {

    public static final String ATTRIBUTE_KEY_APPLICATION = "application";
    public static final String ATTRIBUTE_KEY_TYPE = "browserType";
    public static final String ATTRIBUTE_KEY_VERSION = "browserVersion";
    public static final String ATTRIBUTE_KEY_ENVIRONMENT = "systemEnvironment";

    public enum BrowserType {

        CHROME,
        EDGE,
        FIREFOX,
        IE,
        OPERA,
        SAFARI,

        UNKNOWN;

        static BrowserType of(boolean isChrome, boolean isEdge, boolean isFirefox,
                                     boolean isInternetExplorer, boolean isOpera, boolean isSafari) {
            if (isChrome) {
                return CHROME;
            } else if (isEdge) {
                return EDGE;
            } else if (isFirefox) {
                return FIREFOX;
            } else if (isInternetExplorer) {
                return IE;
            } else if (isOpera) {
                return OPERA;
            } else if (isSafari) {
                return SAFARI;
            } else {
                return UNKNOWN;
            }
        }
    }

    public enum SystemEnvironmentType {

        ANDROID,
        IPAD,
        IPHONE,
        LINUX,
        MACOSX,
        WINDOWS,
        WINDOWS_PHONE,

        UNKNOWN;

        static SystemEnvironmentType of(boolean android, boolean iPad, boolean iPhone, boolean linux, boolean macOSX,
                                               boolean windows, boolean windowsPhone) {
            if (android) {
                return ANDROID;
            } else if (iPad) {
                return IPAD;
            } else if (iPhone) {
                return IPHONE;
            } else if (linux) {
                return LINUX;
            } else if (macOSX) {
                return MACOSX;
            } else if (windows) {
                return WINDOWS;
            } else if (windowsPhone) {
                return WINDOWS_PHONE;
            } else {
                return UNKNOWN;
            }
        }
    }

    private final String application;
    private final BrowserType browser;
    private final String version;
    private final SystemEnvironmentType environment;

    BrowserInfo(String application, BrowserType browser, String version, SystemEnvironmentType environment) {
        this.application = application;
        this.browser = browser;
        this.version = version;
        this.environment = environment;
    }

    String getApplication() {
        return application;
    }

    BrowserType getBrowser() {
        return browser;
    }

    String getVersion() {
        return version;
    }

    SystemEnvironmentType getEnvironment() {
        return environment;
    }
}
