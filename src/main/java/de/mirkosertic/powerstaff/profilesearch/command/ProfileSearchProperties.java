package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "powerstaff.profilesearch")
public class ProfileSearchProperties {

    private int maxContextTokens = 128000;

    public int getMaxContextTokens() {
        return maxContextTokens;
    }

    public void setMaxContextTokens(final int maxContextTokens) {
        this.maxContextTokens = maxContextTokens;
    }
}
