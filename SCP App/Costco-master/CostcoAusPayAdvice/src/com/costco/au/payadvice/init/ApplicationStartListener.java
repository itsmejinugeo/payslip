package com.costco.au.payadvice.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@SuppressFBWarnings
public class ApplicationStartListener {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationStartListener.class);

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {

		String id = event.getApplicationContext().getId();
		if (id.startsWith("org.springframework.web.context.WebApplicationContext:") && !id.endsWith("SpringDispatcher")) {

			// initService.initialise(PersonService.SYSTEM_USER_NAME);
			logger.info("Initializing Tenant...");
		}
	}
}
