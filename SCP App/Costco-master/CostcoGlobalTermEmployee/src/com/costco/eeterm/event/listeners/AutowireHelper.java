package com.costco.eeterm.event.listeners;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings
public final class AutowireHelper implements ApplicationContextAware {
	private static final AutowireHelper INSTANCE = new AutowireHelper();
	private static ApplicationContext applicationContext;

	private AutowireHelper() {
	}

	public static void autowire(Object classToAutowire, Object... beansToAutowireInClass) {
		for (Object bean : beansToAutowireInClass) {
			if (bean == null) {
				applicationContext.getAutowireCapableBeanFactory().autowireBean(classToAutowire);
			}
		}
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		AutowireHelper.applicationContext = applicationContext;
	}

	public static AutowireHelper getInstance() {
		return INSTANCE;
	}

}
