package io.github.habatoo.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Класс для биндинга настроек из файла конфигурации.
 * <p>
 * Связывает свойства с префиксом "app.image" из application.yml
 * Содержит параметры для настройки паттерна defaultExtension.
 * <p>
 */
@ConfigurationProperties(prefix = "app.image")
public record ImageProperties(
        String defaultExtension
) {
}
