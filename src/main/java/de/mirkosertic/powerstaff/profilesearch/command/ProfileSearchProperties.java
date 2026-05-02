package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "powerstaff.profilesearch")
public class ProfileSearchProperties {

    private int maxContextTokens = 128000;
    private Duration streamingTimeout = Duration.ofMinutes(5);

    public int getMaxContextTokens() {
        return maxContextTokens;
    }

    public void setMaxContextTokens(final int maxContextTokens) {
        this.maxContextTokens = maxContextTokens;
    }

    public Duration getStreamingTimeout() {
        return streamingTimeout;
    }

    public void setStreamingTimeout(final Duration streamingTimeout) {
        this.streamingTimeout = streamingTimeout;
    }
}
