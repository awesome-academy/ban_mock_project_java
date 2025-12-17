package com.sunasterisk.expense_management.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;

import java.text.MessageFormat;
import java.util.*;

/**
 * Custom MessageSource that loads messages from YAML files
 * Uses Spring's YamlPropertiesFactoryBean which is part of spring-boot
 */
public class YamlMessageSource extends AbstractMessageSource {

    private final Map<Locale, Properties> propertiesMap = new HashMap<>();

    public void setResources(Resource[] resources) {
        loadResources(resources);
    }

    private void loadResources(Resource[] resources) {
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) continue;

            // Extract locale from filename (e.g., messages_vi.yml -> vi)
            Locale locale = extractLocale(filename);

            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource);
            Properties properties = factory.getObject();

            if (properties != null) {
                // Merge properties for the same locale
                Properties existingProperties = propertiesMap.get(locale);
                if (existingProperties == null) {
                    propertiesMap.put(locale, properties);
                } else {
                    // Merge new properties into existing ones
                    existingProperties.putAll(properties);
                }
            }
        }
    }

    private Locale extractLocale(String filename) {
        // Default locale
        if (!filename.contains("_")) {
            return Locale.forLanguageTag("vi");
        }

        // Extract locale string between _ and .
        int underscoreIndex = filename.indexOf("_");
        int dotIndex = filename.lastIndexOf(".");

        if (underscoreIndex != -1 && dotIndex != -1 && underscoreIndex < dotIndex) {
            String localeStr = filename.substring(underscoreIndex + 1, dotIndex);
            return Locale.forLanguageTag(localeStr);
        }

        return Locale.forLanguageTag("vi");
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        Properties properties = propertiesMap.get(locale);

        // Fallback to Vietnamese if locale not found
        if (properties == null) {
            properties = propertiesMap.get(Locale.forLanguageTag("vi"));
        }

        // Fallback to English if Vietnamese also not found
        if (properties == null) {
            properties = propertiesMap.get(Locale.forLanguageTag("en"));
        }

        if (properties == null) {
            return null;
        }

        String message = properties.getProperty(code);
        if (message == null) {
            return null;
        }

        return new MessageFormat(message, locale);
    }
}
