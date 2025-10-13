package io.github.habatoo.repository.mapper;

import io.github.habatoo.dto.response.CommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <h2>Параметризованный тест для {@link CommentRowMapper}, покрывающий все валидные и невалидные случаи.</h2>
 *
 * <p>
 * Класс проверяет:
 * <ul>
 *   <li>Корректное преобразование валидных данных ResultSet в CommentResponse</li>
 *   <li>Бросание IllegalArgumentException на любые невалидные значения (null, пустые строки и т.д.)</li>
 * </ul>
 * Для генерации данных используется Mockito, тесты не требуют Spring-контекста.
 * Параметризованный тест покрывает все сценарии, включая валидный, пустой, null и пробельный текст.
 * </p>
 */
@DisplayName("Юнит-тесты для CommentRowMapper (валидные/невалидные входные данные)")
class CommentRowMapperTest {

    /**
     * Проверяет корректный маппинг валидных полей ResultSet в объект CommentResponse.
     */
    @Test
    @DisplayName("Тест успешного маппинга валидных данных ResultSet в CommentResponse")
    void mapRowBasicFieldsTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getLong("id")).thenReturn(100L);
        Mockito.when(rs.getString("text")).thenReturn("Комментарий");
        Mockito.when(rs.getLong("post_id")).thenReturn(7L);

        CommentRowMapper mapper = new CommentRowMapper();
        CommentResponse resp = mapper.mapRow(rs, 0);

        assertEquals(100L, resp.id());
        assertEquals("Комментарий", resp.text());
        assertEquals(7L, resp.postId());
    }

    /**
     * <p>
     * Параметризованный тест, который проверяет поведение CommentRowMapper при различных валидных и невалидных входных данных.
     * <ul>
     *   <li>Если shouldThrow=true, должен выброситься {@link IllegalArgumentException} с ожидаемым сообщением.</li>
     *   <li>Если shouldThrow=false, происходит корректный маппинг всех полей в CommentResponse.</li>
     * </ul>
     * </p>
     *
     * @param scenario          Описание случая
     * @param id                Значение поля id
     * @param text              Значение поля text
     * @param postId            Значение поля postId
     * @param shouldThrow       True, если ожидается исключение; иначе false
     * @param expectedMsgPrefix Префикс сообщения об ошибке для исключения
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("commentCases")
    @DisplayName("Параметризованный тест CommentRowMapper: проверка корректных и ошибочных значений")
    void mapRowTest(String scenario,
                    Long id, String text, Long postId,
                    boolean shouldThrow, String expectedMsgPrefix) throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.getLong("id")).thenReturn(id != null ? id : 0L);
        Mockito.when(rs.wasNull()).thenReturn(id == null);
        Mockito.when(rs.getString("text")).thenReturn(text);
        Mockito.when(rs.getLong("post_id")).thenReturn(postId != null ? postId : 0L);
        Mockito.when(rs.wasNull()).thenReturn(postId == null);

        CommentRowMapper mapper = new CommentRowMapper();

        if (shouldThrow) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> mapper.mapRow(rs, 0)
            );
            assertTrue(ex.getMessage().startsWith(expectedMsgPrefix),
                    "Expected prefix: " + expectedMsgPrefix + ", but was: " + ex.getMessage());
        } else {
            CommentResponse resp = mapper.mapRow(rs, 0);
            assertEquals(id, resp.id());
            assertEquals(text, resp.text());
            assertEquals(postId, resp.postId());
        }
    }

    /**
     * Набор тестовых случаев для проверки различных комбинаций валидных и невалидных значений.
     */
    static Stream<Arguments> commentCases() {
        return Stream.of(
                Arguments.of("Valid values", 101L, "Комментарий", 8L, false, ""),
                Arguments.of("Empty text", 101L, "", 8L, true, "Comment text cannot be null or empty"),
                Arguments.of("Null text", 102L, null, 9L, true, "Comment text cannot be null or empty"),
                Arguments.of("Text is spaces", 103L, "   ", 10L, true, "Comment text cannot be null or empty")
        );
    }
}
