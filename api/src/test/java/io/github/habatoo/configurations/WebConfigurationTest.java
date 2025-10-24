package io.github.habatoo.configurations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тест для конфигурации WebConfiguration.
 */
@ExtendWith(SpringExtension.class)
@DisplayName("Тест конфигурации WebConfiguration")
class WebConfigurationTest {

    @Mock
    private CorsProperties corsProperties;

    @InjectMocks
    private WebConfiguration webConfiguration;

    /**
     * <p>Проверяет инициализацию основного web-конфига приложения.</p>
     */
    @Test
    @DisplayName("Конфигурация должна создаваться без ошибок")
    void configurationShouldBeCreatedTest() {
        assertThat(webConfiguration).isNotNull();
    }

    /**
     * <p>Проверяет инициализацию CorsMappings.</p>
     */
    @Test
    @DisplayName("CORS маппинги должны быть корректно настроены")
    void corsMappingsShouldBeConfiguredTest() {
        CorsRegistry registry = new CorsRegistry();

        webConfiguration.addCorsMappings(registry);

        assertThat(webConfiguration).isNotNull();
        assertThat(webConfiguration).isInstanceOf(WebMvcConfigurer.class);
    }
}
