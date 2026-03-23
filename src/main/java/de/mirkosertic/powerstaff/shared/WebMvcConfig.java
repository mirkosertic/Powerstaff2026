package de.mirkosertic.powerstaff.shared;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // HTML <input type="date"> sends yyyy-MM-dd; also accept dd.MM.yyyy (German format)
        registry.addConverter(String.class, LocalDate.class, source -> {
            if (source == null || source.isBlank()) return null;
            // Try ISO format (yyyy-MM-dd) — standard HTML date input
            try {
                return LocalDate.parse(source.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e1) {
                // Try German format (dd.MM.yyyy)
                try {
                    return LocalDate.parse(source.trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Ungültiges Datum: " + source + ". Erwartet: TT.MM.JJJJ");
                }
            }
        });

        // LocalDateTime: HTML date input sends yyyy-MM-dd (no time) → treat as midnight
        registry.addConverter(String.class, LocalDateTime.class, source -> {
            if (source == null || source.isBlank()) return null;
            // Try ISO format (yyyy-MM-dd) → midnight
            try {
                return LocalDate.parse(source.trim(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException e1) {
                // Try German format (dd.MM.yyyy) → midnight
                try {
                    return LocalDate.parse(source.trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy")).atStartOfDay();
                } catch (DateTimeParseException e2) {
                    // Try full ISO datetime (yyyy-MM-ddTHH:mm:ss)
                    try {
                        return LocalDateTime.parse(source.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (DateTimeParseException e3) {
                        throw new IllegalArgumentException("Ungültiges Datum: " + source + ". Erwartet: TT.MM.JJJJ");
                    }
                }
            }
        });
    }
}
