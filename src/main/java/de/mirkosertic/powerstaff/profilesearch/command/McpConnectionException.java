package de.mirkosertic.powerstaff.profilesearch.command;

/**
 * Checked Exception für Fehler beim Aufbau der MCP-Verbindung.
 * Wird von {@link McpClientFactory} geworfen wenn die Verbindung zum MCP-Server
 * nach allen Retry-Versuchen fehlschlägt oder MCP deaktiviert ist.
 */
public class McpConnectionException extends Exception {

    public McpConnectionException(final String message) {
        super(message);
    }

    public McpConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
