//package io.github.habatoo.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
///**
// * Конфигурация безопасности Spring Security с настройками CORS.
// * <p>
// * Использует параметры из бинда CorsProperties для централизованной настройки CORS.
// * Включает CORS с политиками, заданными в CorsProperties, и отключает CSRF.
// * Все HTTP-запросы разрешены (для упрощения, настройте по своему требованию).
// */
//@Configuration
//public class SecurityConfig {
//
//    private final CorsProperties corsProperties;
//
//    public SecurityConfig(CorsProperties corsProperties) {
//        this.corsProperties = corsProperties;
//    }
//
//    /**
//     * Основной фильтр безопасности, который конфигурирует CORS, CSRF и авторизацию.
//     *
//     * @param http HttpSecurity для настройки безопасности HTTP
//     * @return сборка SecurityFilterChain с заданными настройками
//     * @throws Exception в случае ошибок конфигурации
//     */
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.cors(Customizer.withDefaults())
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
//
//        return http.build();
//    }
//
//
//    /**
//     * Конфигурация источника CORS с использованием параметров из CorsProperties.
//     *
//     * @return CorsConfigurationSource, задающий политики CORS для указанных путей
//     */
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        configuration.setAllowedOriginPatterns(corsProperties.allowedOriginPatterns());
//        configuration.setAllowedMethods(corsProperties.allowedMethods());
//        configuration.setAllowedHeaders(corsProperties.allowedHeaders());
//        configuration.setAllowCredentials(corsProperties.allowCredentials());
//        configuration.setMaxAge(corsProperties.maxAge());
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration(corsProperties.pathPattern(), configuration);
//        return source;
//    }
//}
