package com.costco.eeterm.configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.costco.eeterm.event.listeners.AutowireHelper;

/**
 * 
 * Network Compliance application configuration
 *
 */
@Configuration
@EnableScheduling
@EnableAsync
@EnableWebMvc
@EnableAspectJAutoProxy
@ComponentScan(basePackages = ConfigurationValues.BASE_PACKAGES)
public class ApplicationConfig {

	/**
	 * Configuration to avoid concurrency on scheduled tasks
	 * 
	 * @return Executors
	 */
	@Bean(destroyMethod = "shutdown")
	public Executor taskScheduler() {
		return Executors.newCachedThreadPool();
	}

	@Bean
	public AutowireHelper autowireHelper() {
		return AutowireHelper.getInstance();
	}

}