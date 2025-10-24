//package io.github.habatoo.security;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
///**
// * Проверяются настройки CORS, предоставляемые {@link CorsProperties},
// * и корректное создание {@link CorsConfigurationSource}
// * с использованием {@link MockHttpServletRequest} для эмуляции запроса.
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("Тест для SecurityConfig")
//public class SecurityConfigUnitTest {
//
//    private static final List<String> ALLOWED_ORIGIN_PATTERNS = List.of("http://localhost:*");
//    private static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
//    private static final List<String> ALLOWED_HEADERS = List.of("*");
//    private static final boolean ALLOW_CREDENTIALS = true;
//    private static final Long MAX_AGE = 3600L;
//    private static final String PATH_PATTERN = "/api/**";
//
//    private CorsProperties corsProperties;
//
//    private SecurityConfig securityConfig;
//
//    @BeforeEach
//    void setup() {
//        corsProperties = mock(CorsProperties.class);
//        when(corsProperties.allowedOriginPatterns()).thenReturn(ALLOWED_ORIGIN_PATTERNS);
//        when(corsProperties.allowedMethods()).thenReturn(ALLOWED_METHODS);
//        when(corsProperties.allowedHeaders()).thenReturn(ALLOWED_HEADERS);
//        when(corsProperties.allowCredentials()).thenReturn(ALLOW_CREDENTIALS);
//        when(corsProperties.maxAge()).thenReturn(MAX_AGE);
//        when(corsProperties.pathPattern()).thenReturn(PATH_PATTERN);
//
//        securityConfig = new SecurityConfig(corsProperties);
//    }
//
//    /**
//     * Тест проверки параметров конфигурации corsConfigurationSource.
//     */
//    @Test
//    @DisplayName("Тест конфигурации corsConfigurationSource")
//    void corsConfigurationSourceTest() {
//        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
//        assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
//
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        request.setRequestURI("/api/test");
//        request.setContextPath("");
//
//        CorsConfiguration corsConfig = source.getCorsConfiguration(request);
//
//        assertThat(corsConfig).isNotNull();
//        assertThat(corsConfig.getAllowedOriginPatterns()).containsExactlyElementsOf(ALLOWED_ORIGIN_PATTERNS);
//        assertThat(corsConfig.getAllowedMethods()).containsAll(ALLOWED_METHODS);
//        assertThat(corsConfig.getAllowedHeaders()).containsAll(ALLOWED_HEADERS);
//        assertThat(corsConfig.getAllowCredentials()).isEqualTo(ALLOW_CREDENTIALS);
//        assertThat(corsConfig.getMaxAge()).isEqualTo(MAX_AGE);
//    }
//}
