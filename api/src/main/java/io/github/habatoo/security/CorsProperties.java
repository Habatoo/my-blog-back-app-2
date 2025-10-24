//package io.github.habatoo.security;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//
//import java.util.List;
//
///**
// * Класс для биндинга настроек CORS из файла конфигурации.
// * <p>
// * Связывает свойства с префиксом "spring.web.cors" из application.properties или application.yml
// * Содержит параметры для настройки паттерна, разрешённых источников, HTTP-методов, заголовков,
// * параметра allowCredentials и maxAge для CORS.
// * <p>
// * Используется в виде immutable Java record, что упрощает хранение неизменяемых настроек.
// */
//@ConfigurationProperties(prefix = "spring.web.cors")
//public record CorsProperties(
//        String pathPattern,
//        List<String> allowedOriginPatterns,
//        List<String> allowedMethods,
//        List<String> allowedHeaders,
//        boolean allowCredentials,
//        Long maxAge
//) {
//}
