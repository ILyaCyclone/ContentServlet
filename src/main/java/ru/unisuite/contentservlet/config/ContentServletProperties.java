package ru.unisuite.contentservlet.config;

public class ContentServletProperties {
    private static final String PREFIX = "contentservlet.";

    private final String applicationName;
    private final String contentUrlPattern;
    private final String contentSecureUrlPattern;

    private final String datasourceJndiName;
    private final String datasourceUrl;
    private final String datasourcePassword;
    private final String datasourceUsername;

    private final String cacheControl;
    private final String resizerType;
    private final String imageQuality;

    private final boolean enableMetrics;

    public ContentServletProperties(PropertyResolver propertyResolver) {
        this.applicationName = propertyResolver.resolve(PREFIX + "application-name");
        this.contentUrlPattern = propertyResolver.resolve(PREFIX + "content-url-pattern");
        this.contentSecureUrlPattern = propertyResolver.resolve(PREFIX + "content-secure-url-pattern");

        this.datasourceJndiName = propertyResolver.resolve(PREFIX + "datasource.jndi-name");
        this.datasourceUrl = propertyResolver.resolve(PREFIX + "datasource.url");
        this.datasourceUsername = propertyResolver.resolve(PREFIX + "datasource.username");
        this.datasourcePassword = propertyResolver.resolve(PREFIX + "datasource.password");

        this.cacheControl = propertyResolver.resolve(PREFIX + "cachecontrol");

        this.resizerType = propertyResolver.resolve(PREFIX + "resizer-type");
        this.imageQuality = propertyResolver.resolve(PREFIX + "image-quality");

        this.enableMetrics = Boolean.parseBoolean(propertyResolver.resolve(PREFIX + "enable-metrics"));
    }



    public String getApplicationName() {
        return applicationName;
    }

    public String getContentUrlPattern() {
        return contentUrlPattern;
    }

    public String getContentSecureUrlPattern() {
        return contentSecureUrlPattern;
    }

    public String getDatasourceJndiName() {
        return datasourceJndiName;
    }

    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public String getDatasourcePassword() {
        return datasourcePassword;
    }

    public String getDatasourceUsername() {
        return datasourceUsername;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public String getResizerType() {
        return resizerType;
    }

    public String getImageQuality() {
        return imageQuality;
    }

    public boolean isEnableMetrics() {
        return enableMetrics;
    }

    @Override
    public String toString() {
        //@formatter:off
        return "ContentServletProperties{" +
                (applicationName != null ?           "applicationName='" + applicationName + '\'' : "") +
                (contentUrlPattern != null ?       ", contentUrlPattern='" + contentUrlPattern + '\'' : "") +
                (contentSecureUrlPattern != null ? ", contentSecureUrlPattern='" + contentSecureUrlPattern + '\'' : "") +
                (datasourceJndiName != null ?      ", datasourceJndiName='" + datasourceJndiName + '\'' : "") +
                (datasourceUrl != null ?           ", datasourceUrl='" + datasourceUrl + '\'' : "") +
                (datasourcePassword != null ?      ", datasourcePassword='***'" : "") +
                (datasourceUsername != null ?      ", datasourceUsername='" + datasourceUsername + '\'' : "") +
                (cacheControl != null ?            ", cacheControl='" + cacheControl + '\'' : "") +
                (resizerType != null ?             ", resizerType='" + resizerType + '\'' : "") +
                (imageQuality != null ?            ", imageQuality='" + imageQuality + '\'' : "") +
                ", enableMetrics=" + enableMetrics +
                '}';
        //@formatter:on
    }
}
