//package io.github.habatoo.security;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.context.properties.bind.Binder;
//import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
//
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Unit test for {@link CorsProperties} record.
// * Проверяет корректность биндинга настроек из property map.
// */
//@DisplayName("Unit tests for CorsProperties @ConfigurationProperties binding")
//public class CorsPropertiesUnitTest {
//
//    /**
//     * Проверяет, что вызове корректно подтягиваются properties.
//     */
//    @Test
//    @DisplayName("should bind properties from map to CorsProperties record")
//    void shouldBindProperties() {
//        Map<String, Object> props = Map.ofEntries(
//                Map.entry("spring.web.cors.path-pattern", "/api/**"),
//                Map.entry("spring.web.cors.allowed-origin-patterns[0]", "http://localhost:*"),
//                Map.entry("spring.web.cors.allowed-methods[0]", "GET"),
//                Map.entry("spring.web.cors.allowed-methods[1]", "POST"),
//                Map.entry("spring.web.cors.allowed-methods[2]", "PUT"),
//                Map.entry("spring.web.cors.allowed-methods[3]", "DELETE"),
//                Map.entry("spring.web.cors.allowed-methods[4]", "OPTIONS"),
//                Map.entry("spring.web.cors.allowed-headers[0]", "*"),
//                Map.entry("spring.web.cors.allow-credentials", true),
//                Map.entry("spring.web.cors.max-age", 3600L)
//        );
//
//        Binder binder = new Binder(new MapConfigurationPropertySource(props));
//        CorsProperties corsProperties = binder.bind("spring.web.cors", CorsProperties.class).get();
//
//        assertThat(corsProperties.pathPattern()).isEqualTo("/api/**");
//        assertThat(corsProperties.allowedOriginPatterns()).containsExactly("http://localhost:*");
//        assertThat(corsProperties.allowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS");
//        assertThat(corsProperties.allowedHeaders()).containsExactly("*");
//        assertThat(corsProperties.allowCredentials()).isTrue();
//        assertThat(corsProperties.maxAge()).isEqualTo(3600L);
//    }
//}
