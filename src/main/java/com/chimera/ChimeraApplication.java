package com.chimera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Spring Boot entrypoint for the Chimera runtime host.
 *
 * <p>JPA and JDBC auto-configuration are excluded until PostgreSQL is wired for deployment;
 * domain scaffolding remains valid without a live database.</p>
 *
 * <p><b>SRS:</b> §3.1 FastRender Swarm, §6.2 orchestration boundaries.
 * <b>User stories:</b> US-016 (operator campaign visibility), US-018 (developer test workflow —
 * application must compile and start for integration milestones).</p>
 */
@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        },
        scanBasePackages = "com.chimera"
)
public class ChimeraApplication {

    /**
     * Boots the Spring context. No swarm business logic runs here (scaffold only).
     *
     * @param args standard CLI args
     */
    public static void main(String[] args) {
        SpringApplication.run(ChimeraApplication.class, args);
    }
}
