package io.github.habatoo.handlers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <h2>Тесты методов GlobalExceptionHandler глобального перехвата исключений.</h2>
 *
 * <p>
 * Данный класс проверяет обработку типовых исключений, возникающих при работе REST-контроллеров,
 * с помощью слоя глобального перехвата {@link GlobalExceptionHandler}.
 * Тесты не поднимают Spring Boot-контекст и используют {@link MockMvc} с ручной настройкой Advice.
 * Проверяются все предопределённые обработчики исключений:
 * <ul>
 *     <li>{@link org.springframework.dao.EmptyResultDataAccessException}</li>
 *     <li>{@link IllegalArgumentException}</li>
 *     <li>{@link org.springframework.dao.DataAccessException}</li>
 *     <li>{@link RuntimeException} (и производные)</li>
 * </ul>
 * Каждый тест эмулирует выброс нужного исключения контроллером и проверяет,
 * что обработчик GlobalExceptionHandler корректно возвращает нужный HTTP-status и JSON-ответ.
 * </p>
 *
 * <p>
 * Контекст теста инициализируется один раз для всех тестов — благодаря аннотации {@code @TestInstance(PER_CLASS)}.
 * Для эмуляции исключений в тесте используется {@code TestDummyController} с ручными @GetMapping эндпойнтами.
 * </p>
 *
 * @see GlobalExceptionHandler
 * @see MockMvc
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestConfig.class)
@DisplayName("Тесты методов GlobalExceptionHandler глобального перехвата исключений.")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    /**
     * <p>
     * Инициализация {@link MockMvc} c настройкой dummy-контроллера и глобального Advice.
     * Выполняется один раз для всего класса тестов.
     * </p>
     */
    @BeforeAll
    void setUpAll() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestDummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Проверяет, что эндпоинт /api/posts/invalid вызывает MethodArgumentTypeMismatchException
     * и возвращает статус 400 с корректным текстом ошибки.
     */
    @Test
    void invalidEndpointShouldReturnBadRequestAndErrorJson() throws Exception {
        mockMvc.perform(get("/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"error\":\"Invalid path or query parameter: abc\"}"));
    }

    /**
     * <p>
     * Проверяет корректную обработку и возврат ответа для исключения {@link org.springframework.dao.EmptyResultDataAccessException}.
     * Ожидается статус 404 и JSON: {"error": "Resource not found"}
     * </p>
     */
    @Test
    @DisplayName("Тесты перехвата EmptyResultDataAccessException.")
    void testNotFound() throws Exception {
        mockMvc.perform(get("/notfound").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":\"Resource not found\"}"));
    }

    /**
     * <p>
     * Проверяет корректную обработку и возврат ответа для исключения {@link IllegalArgumentException}.
     * Ожидется статус 400 и JSON с текстом исключения.
     * </p>
     */
    @Test
    @DisplayName("Тесты перехвата IllegalArgumentException.")
    void testIllegalArgument() throws Exception {
        mockMvc.perform(get("/illegal").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"error\":\"bad arg\"}"));
    }

    /**
     * <p>
     * Проверяет корректную обработку и возврат ответа для исключения {@link org.springframework.dao.DataAccessException}.
     * Ожидется статус 500 и стандартный текст ошибки "Database error".
     * </p>
     */
    @Test
    @DisplayName("Тесты перехвата DataAccessException.")
    void testDataAccess() throws Exception {
        mockMvc.perform(get("/data").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"error\":\"Database error\"}"));
    }

    /**
     * <p>
     * Проверяет обработку любого другого типа {@link RuntimeException}.
     * Ожидется статус 500 и стандартный текст ошибки "Internal server error".
     * </p>
     */
    @Test
    @DisplayName("Тесты перехвата RuntimeException.")
    void testGenericException() throws Exception {
        mockMvc.perform(get("/common").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"error\":\"Internal server error\"}"));
    }
}
