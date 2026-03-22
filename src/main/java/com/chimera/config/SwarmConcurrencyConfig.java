package com.chimera.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides an {@link ExecutorService} backed by virtual threads for Worker-scale I/O concurrency.
 *
 * <p><b>SRS:</b> §3.1 Worker concurrency model, Java 21 virtual threads.
 * <b>User stories:</b> US-001 (trend fetch workers), US-004–US-007 (generator / publisher workers).</p>
 */
@Configuration
public class SwarmConcurrencyConfig {

    /**
     * Executor used for fire-and-forget Worker tasks. Destroy delegates to {@link ExecutorService#close()}.
     *
     * @return new virtual-thread-per-task executor (never a fixed thread pool)
     */
    @Bean(destroyMethod = "close")
    public ExecutorService swarmWorkerExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
