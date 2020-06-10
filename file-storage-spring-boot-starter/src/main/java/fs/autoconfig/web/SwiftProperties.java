package fs.autoconfig.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fs.swift")
public class SwiftProperties {
    private int connectTimeout = 3000;
    private int readTimeout = 600000;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}
