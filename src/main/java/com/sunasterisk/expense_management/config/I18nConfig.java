package com.sunasterisk.expense_management.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.io.IOException;
import java.util.Locale;

/**
 * Configuration for i18n (Internationalization)
 * Supports Vietnamese (vi) and English (en)
 * Uses YAML format for message files
 */

@Configuration
public class I18nConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() throws IOException {
        YamlMessageSource messageSource = new YamlMessageSource();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // Load all YAML message files from i18n directory and subdirectories
        Resource[] resources = resolver.getResources("classpath:i18n/**/*_*.yml");

        messageSource.setResources(resources);

        return messageSource;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // ?lang=en or ?lang=vi
        return interceptor;
    }

    @Bean
    public ApiLocaleInterceptor apiLocaleInterceptor() {
        ApiLocaleInterceptor interceptor = new ApiLocaleInterceptor();
        return interceptor;
    }


    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("vi"));
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Enable WebJars support
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiLocaleInterceptor())
            .addPathPatterns("/api/**");

        registry.addInterceptor(localeChangeInterceptor())
                .addPathPatterns("/admin/**");
    }
}
