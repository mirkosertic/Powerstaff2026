package de.mirkosertic.powerstaff.profilesearch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProfileSearchConfig {

    @Bean
    public LlmService llmService() {
        return new StubLlmService();
    }
}
