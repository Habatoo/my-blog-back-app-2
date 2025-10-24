package io.github.habatoo.repositories.mapper;

import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты для {@link PostListRowMapper}.
 * <p>
 * Класс проверяет корректную работу маппера по преобразованию данных ResultSet в PostResponseDto:
 * <ul>
 *   <li> Маппинг валидных записей </li>
 *   <li> Обработка обрезки длинного текста </li>
 *   <li> Корректная генерация исключений при недопустимых значениях полей (null/empty) </li>
 * </ul>
 */
@DisplayName("Тесты для PostListRowMapper")
class PostListRowMapperTest {

    /**
     * Проверяет корректное преобразование валидной строки ResultSet в PostResponseDto.
     * Ожидается полный успех и соответствие всех полей.
     */
    @Test
    @DisplayName("Корректный маппинг валидного поста из ResultSet")
    void mapsSimplePostCorrectlyTest() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(42L);
        when(rs.getString("title")).thenReturn("Заголовок");
        when(rs.getString("text")).thenReturn("Темы Spring и посты");
        when(rs.getInt("likes_count")).thenReturn(13);
        when(rs.getInt("comments_count")).thenReturn(2);

        PostListRowMapper mapper = new PostListRowMapper();
        PostResponseDto response = mapper.mapRow(rs, 0);

        assertEquals(42L, response.id());
        assertEquals("Заголовок", response.title());
        assertEquals("Темы Spring и посты", response.text());
        assertEquals(13, response.likesCount());
        assertEquals(2, response.commentsCount());
        assertTrue(response.tags().isEmpty());
    }

    /**
     * Проверяет, что длинный текст корректно обрезается до ограниченной длины.
     * В результат должен попасть только первые 128 символов с символом "…".
     */
    @Test
    @DisplayName("Обрезка текста длиной более 128 символов")
    void mapsTruncatedTextCorrectlyTest() throws Exception {
        String longText = "a".repeat(200);
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("title")).thenReturn("Title");
        when(rs.getString("text")).thenReturn(longText);
        when(rs.getInt("likes_count")).thenReturn(3);
        when(rs.getInt("comments_count")).thenReturn(5);

        PostListRowMapper mapper = new PostListRowMapper();
        PostResponseDto response = mapper.mapRow(rs, 0);

        assertEquals(1L, response.id());
        assertEquals("Title", response.title());
        assertEquals("a".repeat(128) + "…", response.text());
        assertEquals(3, response.likesCount());
        assertEquals(5, response.commentsCount());
    }

    /**
     * Параметризованный тест: проверяет, что при некорректных полях (null или пустое значение title/text)
     * выбрасывается {@link IllegalArgumentException} с корректным сообщением.
     *
     * @param scenario      — описание кейса
     * @param id            — идентификатор поста
     * @param title         — заголовок поста
     * @param text          — текст поста
     * @param tags          — список тегов (фиктивно для маппера)
     * @param likesCount    — количество лайков
     * @param commentsCount — количество комментариев
     * @param expectedMsg   — ожидаемый префикс сообщения об ошибке
     */
    @ParameterizedTest(name = "{0}")
    @DisplayName("Проверка выброса исключений при некорректных данных")
    @MethodSource("invalidCases")
    void mapRowInvalidValuesThrowsTest(String scenario,
                                       Long id, String title, String text, List<String> tags,
                                       Integer likesCount, Integer commentsCount,
                                       String expectedMsg) throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(id != null ? id : 0L);
        when(rs.wasNull()).thenReturn(id == null);
        when(rs.getString("title")).thenReturn(title);
        when(rs.getString("text")).thenReturn(text);
        when(rs.getInt("likes_count")).thenReturn(likesCount != null ? likesCount : 0);
        when(rs.getInt("comments_count")).thenReturn(commentsCount != null ? commentsCount : 0);

        PostListRowMapper mapper = new PostListRowMapper();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> mapper.mapRow(rs, 0));
        assertTrue(exception.getMessage().startsWith(expectedMsg), "Wrong exception message");
    }

    /**
     * Набор параметров для проверки некорректных значений title/text.
     */
    static Stream<Arguments> invalidCases() {
        return Stream.of(
                Arguments.of("title is null", 1L, null, "Text", List.of("tag"), 1, 1, "Post title cannot be null or empty"),
                Arguments.of("title is empty", 1L, "   ", "Text", List.of("tag"), 1, 1, "Post title cannot be null or empty"),
                Arguments.of("text is null", 1L, "Title", null, List.of("tag"), 1, 1, "Post text cannot be null or empty"),
                Arguments.of("text is empty", 1L, "Title", " ", List.of("tag"), 1, 1, "Post text cannot be null or empty")
        );
    }
}
