package io.github.habatoo.configurations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest(classes = Application.class)
@SpringBootTest
@DisplayName("Integration test for CorsProperties loading from application.yaml")
public class PropertiesTest {

    @Autowired
    private Properties properties;

    @Test
    @DisplayName("should load properties from application.yaml")
    void shouldLoadPropertiesFromYaml() {
        assertThat(properties.pathPattern()).isEqualTo("/api/**");
        assertThat(properties.allowedOriginPatterns()).contains("http://localhost");
        assertThat(properties.allowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(properties.allowedHeaders()).contains("*");
        assertThat(properties.allowCredentials()).isTrue();
        assertThat(properties.maxAge()).isEqualTo(3600L);
    }
}
