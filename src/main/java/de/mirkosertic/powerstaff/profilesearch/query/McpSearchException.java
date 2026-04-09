package de.mirkosertic.powerstaff.profilesearch.query;

/**
 * Runtime Exception für Fehler bei der MCP-basierten Profilsuche.
 * Wird an den Controller propagiert und dort via @ExceptionHandler
 * in eine benutzerfreundliche Fehlermeldung umgewandelt.
 */
public class McpSearchException extends RuntimeException {

    public McpSearchException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
