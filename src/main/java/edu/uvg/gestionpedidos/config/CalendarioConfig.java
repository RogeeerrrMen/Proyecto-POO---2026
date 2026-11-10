package edu.uvg.gestionpedidos.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalendarioConfig {
    @Bean("calendarioClock")
    public Clock calendarioClock() {
        return Clock.system(ZoneId.of("America/Guatemala"));
    }
}
