package de.mirkosertic.powerstaff.profilesearch.command;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration Properties für MCP (Model Context Protocol) Client-Verbindung.
 * <p>
 * Konfiguration via {@code powerstaff.mcp.*} in application.yml:
 * <ul>
 *   <li>{@code enabled} – MCP aktivieren (default: false für Produktion/E2E-Tests)</li>
 *   <li>{@code url} – MCP-Server-URL (z.B. http://localhost:9000)</li>
 *   <li>{@code endpoint} – MCP-Endpoint-Pfad (z.B. /mcp/message)</li>
 *   <li>{@code requestTimeout} – Timeout für HTTP-Requests (default: 10s)</li>
 *   <li>{@code maxRetries} – Anzahl Retry-Versuche bei Connection-Fehlern (default: 2)</li>
 *   <li>{@code retryDelay} – Verzögerung zwischen Retries (default: 500ms)</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "powerstaff.mcp")
public class McpConnectionProperties {

    private boolean enabled = false;
    private String url = "http://localhost:9000";
    private String endpoint = "/mcp/message";
    private Duration requestTimeout = Duration.ofSeconds(10);
    private int maxRetries = 2;
    private Duration retryDelay = Duration.ofMillis(500);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(final Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(final Duration retryDelay) {
        this.retryDelay = retryDelay;
    }
}
