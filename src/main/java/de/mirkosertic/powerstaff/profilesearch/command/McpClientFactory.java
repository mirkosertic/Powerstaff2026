package de.mirkosertic.powerstaff.profilesearch.command;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;

/**
 * Factory für die Erstellung von MCP (Model Context Protocol) Client-Instanzen.
 * <p>
 * Erstellt pro Request eine neue MCP-Session mit automatischer Retry-Logik bei
 * Connection-Fehlern. Die Konfiguration erfolgt via {@link McpConnectionProperties}.
 * <p>
 * <strong>Retry-Verhalten:</strong>
 * <ul>
 *   <li>Bei Connection-Fehler: maxRetries Versuche mit retryDelay Pause dazwischen</li>
 *   <li>DEBUG-Log bei jedem Retry-Versuch</li>
 *   <li>McpConnectionException nach allen fehlgeschlagenen Versuchen</li>
 * </ul>
 *
 * @see McpConnectionProperties
 * @see McpConnectionException
 */
@Component
public class McpClientFactory {

    private static final Logger logger = LoggerFactory.getLogger(McpClientFactory.class);

    private final McpConnectionProperties properties;

    public McpClientFactory(final McpConnectionProperties properties) {
        this.properties = properties;
    }

    /**
     * Erstellt einen neuen MCP-Client mit Retry-Logik.
     * <p>
     * Die Methode versucht maxRetries+1 mal eine Verbindung aufzubauen.
     * Zwischen den Versuchen wird retryDelay gewartet.
     *
     * @return initialisierter McpSyncClient
     * @throws McpConnectionException wenn MCP deaktiviert ist oder alle Verbindungsversuche fehlschlagen
     */
    public McpSyncClient createClient() throws McpConnectionException {
        if (!properties.isEnabled()) {
            throw new McpConnectionException("MCP ist deaktiviert (enabled=false)");
        }

        int attempt = 0;
        Exception lastException = null;
        final int totalAttempts = properties.getMaxRetries() + 1;

        while (attempt < totalAttempts) {
            try {
                final McpSyncClient client = buildClient();
                logger.debug("MCP Client erfolgreich erstellt (Versuch {}/{})", attempt + 1, totalAttempts);
                return client;
            } catch (final Exception e) {
                lastException = e;
                attempt++;
                if (attempt < totalAttempts) {
                    final long delayMs = properties.getRetryDelay().toMillis();
                    logger.debug("MCP Verbindung fehlgeschlagen (Versuch {}/{}), retry in {}ms: {}",
                            attempt, totalAttempts, delayMs, e.getMessage());
                    sleep(properties.getRetryDelay());
                }
            }
        }

        // Alle Versuche fehlgeschlagen
        logger.error("MCP Verbindung nach {} Versuchen fehlgeschlagen", totalAttempts, lastException);
        throw new McpConnectionException(
                String.format("MCP Verbindung nach %d Versuchen fehlgeschlagen", totalAttempts),
                lastException
        );
    }

    /**
     * Baut den MCP-Client mit HTTP-Streaming-Transport.
     * Verwendet Spring AI's Builder-API für HttpClientStreamableHttpTransport.
     */
    private McpSyncClient buildClient() {
        final String mcpServerUrl = properties.getUrl() + properties.getEndpoint();

        // 1. JSON Mapper erstellen
        final JsonMapper jsonMapper = JsonMapper.builder().build();

        // 2. Transport aufbauen
        final HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(mcpServerUrl)
                .clientBuilder(HttpClient.newBuilder()
                        .connectTimeout(properties.getRequestTimeout()))
                .jsonMapper(new JacksonMcpJsonMapper(jsonMapper))
                .build();

        // 3. Client-Info
        final McpSchema.Implementation clientInfo = new McpSchema.Implementation(
                "powerstaff-profilesearch",
                "1.0.0"
        );

        // 4. McpSyncClient aufbauen
        return McpClient.sync(transport)
                .clientInfo(clientInfo)
                .requestTimeout(properties.getRequestTimeout())
                .build();
    }

    /**
     * Sleep-Wrapper für Retry-Delay.
     */
    private void sleep(final java.time.Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Retry-Sleep wurde unterbrochen", e);
        }
    }
}
