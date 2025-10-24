//package io.github.habatoo.security;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DisplayName("Integration test for CorsProperties loading from application.yaml")
//@SpringBootTest
//public class CorsPropertiesIntegrationTest {
//
//    @Autowired
//    private CorsProperties corsProperties;
//
//    @Test
//    @DisplayName("should load properties from application.yaml")
//    void shouldLoadPropertiesFromYaml() {
//        assertThat(corsProperties.pathPattern()).isEqualTo("/api/**");
//        assertThat(corsProperties.allowedOriginPatterns()).containsExactly("http://localhost");
//        assertThat(corsProperties.allowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS");
//        assertThat(corsProperties.allowedHeaders()).containsExactly("*");
//        assertThat(corsProperties.allowCredentials()).isTrue();
//        assertThat(corsProperties.maxAge()).isEqualTo(3600L);
//    }
//}
