//package io.github.habatoo.security;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
//import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
//import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
///**
// * Класс тестирования конфигурации безопасности SecurityConfig.
// * <p>
// * Тест проверяет корректность вызовов методов конфигурации безопасности
// * (cors, csrf, authorizeHttpRequests) при построении цепочки SecurityFilterChain.
// */
//@DisplayName("Тесты конфигурации SecurityConfig для настройки фильтров безопасности")
//public class SecurityConfigFilterTest {
//
//    @Mock
//    private HttpSecurity httpSecurity;
//
//    @Mock
//    private CorsConfigurer<HttpSecurity> corsConfigurer;
//
//    @Mock
//    private CsrfConfigurer<HttpSecurity> csrfConfigurer;
//
//    @Mock
//    private AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authRegistry;
//
//    private SecurityConfig securityConfig;
//
//    private AutoCloseable mocks;
//
//    @BeforeEach
//    void setup() throws Exception {
//        mocks = MockitoAnnotations.openMocks(this);
//
//        when(httpSecurity.cors(any())).thenReturn(httpSecurity);
//        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
//        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
//
//        securityConfig = new SecurityConfig(mock(CorsProperties.class));
//    }
//
//    @AfterEach
//    void tearDown() throws Exception {
//        mocks.close();
//    }
//
//    /**
//     * Проверяет, что при вызове filterChain вызываются методы cors, csrf и authorizeHttpRequests
//     * именно один раз, а также что в cors() передается не-null Customizer.
//     */
//    @Test
//    @DisplayName("Тест filterChain с параметрами")
//    @SuppressWarnings("unchecked")
//    void filterChainTest() throws Exception {
//        securityConfig.filterChain(httpSecurity);
//
//        verify(httpSecurity, times(1)).cors(any());
//        verify(httpSecurity, times(1)).csrf(any());
//        verify(httpSecurity, times(1)).authorizeHttpRequests(any());
//
//        ArgumentCaptor<Customizer<CorsConfigurer<HttpSecurity>>> corsCaptor =
//                ArgumentCaptor.forClass((Class) Customizer.class);
//        verify(httpSecurity).cors(corsCaptor.capture());
//        assertThat(corsCaptor.getValue()).isNotNull();
//    }
//}
//
