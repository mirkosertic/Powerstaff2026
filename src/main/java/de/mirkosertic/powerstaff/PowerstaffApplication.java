package de.mirkosertic.powerstaff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@SpringBootApplication
@EnableJdbcAuditing
public class PowerstaffApplication {

    static void main(String[] args) {
        SpringApplication.run(PowerstaffApplication.class, args);
    }
}
