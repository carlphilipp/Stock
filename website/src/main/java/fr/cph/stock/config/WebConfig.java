package fr.cph.stock.config;

import fr.cph.stock.filter.CookieFilter;
import fr.cph.stock.filter.SessionFilter;
import fr.cph.stock.filter.SleuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@ComponentScan({"fr.cph.stock"})
public class WebConfig {

	@Bean
	public FilterRegistrationBean uuidFilter() {
		final FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new SleuthFilter());
		registration.setOrder(1);
		registration.addUrlPatterns("/*");
		return registration;
	}

	@Bean
	public FilterRegistrationBean sessionFilter() {
		final FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new SessionFilter());
		registration.setOrder(2);
		registration.addUrlPatterns("/home/*", "/history/*", "/accounts/*", "/charts/*", "/performance/*", "/currencies/*", "/options/*", "/language/*");
		return registration;
	}

	@Bean
	public FilterRegistrationBean cookieFilter() {
		final FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new CookieFilter());
		registration.setOrder(3);
		registration.addUrlPatterns("/home/*");
		return registration;
	}
}
