package de.conciso.junit5extensionsforintegrationtests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "de.conciso.junit5extensionsforintegrationtests")
@EntityScan(basePackages = "de.conciso.junit5extensionsforintegrationtests")
public class Junit5ExtensionsForIntegrationTestsApplication {

    public static void main(String[] args) {
        SpringApplication.run(Junit5ExtensionsForIntegrationTestsApplication.class, args);
    }
}
