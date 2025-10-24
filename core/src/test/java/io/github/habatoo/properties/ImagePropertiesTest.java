package io.github.habatoo.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки корректной загрузки и связывания конфигурационных свойств
 * из {@code application.yaml} (или других источников конфигурации) в бин {@link ImageProperties}.
 */
@ActiveProfiles("test")
@DisplayName("Тест загрузки ImageProperties")
public class ImagePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ImagePropertiesTest.TestConfig.class)
            .withPropertyValues("app.image.default-extension=jpg");

    @EnableConfigurationProperties(ImageProperties.class)
    static class TestConfig {
    }

    /**
     * Проверяет, что свойство {@code app.image.default-extension} корректно
     * считывается из конфигурации и связывается в объект {@link ImageProperties}.
     */
    @Test
    @DisplayName("Тест загрузки defaultExtension из YAML")
    void shouldLoadPropertiesFromYaml() {
        contextRunner.run(context -> {
            var props = context.getBean(ImageProperties.class);
            assertThat(props.defaultExtension()).isEqualTo("jpg");
        });
    }
}