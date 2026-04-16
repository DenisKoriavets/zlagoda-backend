package ua.edu.ukma.zlagodabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZlagodaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZlagodaBackendApplication.class, args);
    }

}
