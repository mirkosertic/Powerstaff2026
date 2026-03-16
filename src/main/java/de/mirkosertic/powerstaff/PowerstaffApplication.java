package de.mirkosertic.powerstaff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootApplication
@EnableJdbcAuditing
public class PowerstaffApplication {

    public static void main(String[] args) {
        SpringApplication.run(PowerstaffApplication.class, args);
    }
}
