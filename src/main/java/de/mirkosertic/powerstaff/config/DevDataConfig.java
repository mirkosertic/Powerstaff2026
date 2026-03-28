package de.mirkosertic.powerstaff.config;

import org.flywaydb.core.api.Location;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Aktiviert das Dev-Testdaten-Skript (db/devdata/V200__dev_testdata.sql) in Flyway,
 * wenn die JVM-System-Property {@code devmode=true} gesetzt ist.
 *
 * <p>Starten mit: {@code java -Ddevmode=true -jar powerstaff.jar}
 * oder im IDE-Run-Config als VM-Option: {@code -Ddevmode=true}
 */
@Configuration
@ConditionalOnProperty(name = "devmode", havingValue = "true")
class DevDataConfig {

    @Bean
    FlywayConfigurationCustomizer devDataFlywayCustomizer() {
        return configuration -> {
            final Location[] existing = configuration.getLocations();
            final Location[] extended = Arrays.copyOf(existing, existing.length + 1);
            extended[existing.length] = new Location("classpath:db/devdata");
            configuration.locations(extended);
        };
    }
}
