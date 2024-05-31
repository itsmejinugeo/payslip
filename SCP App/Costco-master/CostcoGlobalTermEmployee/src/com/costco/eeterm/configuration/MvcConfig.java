package com.costco.eeterm.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.costco.eeterm.auth.AuthorizationHandler;
import com.costco.eeterm.logging.ApiLogger;


/**
 * 
 * Web Mvc Configuration
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 */
@EnableWebMvc
@Configuration
@EnableScheduling
@ComponentScan(basePackages = ConfigurationValues.BASE_PACKAGES)
public class MvcConfig extends WebMvcConfigurerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MvcConfig.class);


	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
		logger.info("Default servlet handling configured");
	}

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("i18n/messages");
		return messageSource;
	}

	@Bean
	public MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
	    registry.addInterceptor(new ApiLogger());
	    registry.addInterceptor(new AuthorizationHandler()).addPathPatterns("/admin/**");
	}

}