package com.sun.expense_management.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.io.IOException;
import java.util.Arrays;
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
        Resource[] resources = resolver.getResources("classpath:i18n/messages_*.yml");

        messageSource.setResources(resources);

        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.forLanguageTag("vi"));
        localeResolver.setSupportedLocales(Arrays.asList(
            Locale.forLanguageTag("vi"), // Vietnamese
            Locale.forLanguageTag("en")  // English
        ));
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // ?lang=en or ?lang=vi
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
