package io.github.habatoo.repositories.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты методов updateImageMetadata в ImageRepositoryImpl</h2>
 *
 * <p>
 * Класс проверяет корректную работу метода обновления метаданных изображений поста:
 * <ul>
 *     <li>Успешное обновление метаданных — метод не выбрасывает исключений, вызывается корректный SQL-запрос</li>
 *     <li>Попытка обновить метаданные несуществующего поста — выбрасывается EmptyResultDataAccessException с ожидаемым сообщением</li>
 *     <li>Обработка кейса, когда update возвращает 0 строк (ни один пост не обновлён) — выбрасывается EmptyResultDataAccessException</li>
 * </ul>
 * Для тестирования используется мок JdbcTemplate. Проверяется правильность запроса, аргументов и реакции метода на различные исходы.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты методов ImageRepository.")
class ImageRepositoryUpdateImageMetadataTest extends ImageRepositoryTestBase {

    /**
     * Проверяет, что метод updateImageMetadata успешно обновляет метаданные изображения для поста.
     * Ожидается отсутствие исключения и корректный вызов учётного SQL-запроса.
     */
    @Test
    @DisplayName("Должен обновить метаданные изображения поста при успешном обновлении")
    void shouldUpdateImageMetadataSuccessfullyTest() {
        when(jdbcTemplate.update(anyString(), anyString(), anyLong(), anyString(), anyLong())).thenReturn(1);

        assertDoesNotThrow(() -> imageRepository.updateImageMetadata(EXISTING_POST_ID, IMAGE_NAME, IMAGE_SIZE, URL));

        verify(jdbcTemplate).update(
                eq("""
                        UPDATE post
                        SET image_name = ?, image_size = ?, image_url = ?
                        WHERE id = ?
                        """),
                eq(IMAGE_NAME),
                eq(IMAGE_SIZE),
                eq(URL),
                eq(EXISTING_POST_ID)
        );
    }

    /**
     * Проверяет, что метод выбрасывает EmptyResultDataAccessException,
     * если при обновлении метаданных не найден пост с указанным id (update=0).
     * Проверяет текст ошибки и корректность вызова SQL.
     */
    @Test
    @DisplayName("Должен выбросить исключение при обновлении метаданных изображения несуществующего поста")
    void shouldThrowWhenUpdateImageMetadataFailsTest() {
        when(jdbcTemplate.update(anyString(), anyString(), anyLong(), anyString(), anyLong())).thenReturn(0);

        EmptyResultDataAccessException ex = assertThrows(EmptyResultDataAccessException.class,
                () -> imageRepository.updateImageMetadata(EXISTING_POST_ID, IMAGE_NAME, IMAGE_SIZE, URL));

        assertTrue(ex.getMessage().contains("Post not found with id"));
        verify(jdbcTemplate).update(
                eq("""
                        UPDATE post
                        SET image_name = ?, image_size = ?, image_url = ?
                        WHERE id = ?
                        """),
                eq(IMAGE_NAME),
                eq(IMAGE_SIZE),
                eq(URL),
                eq(EXISTING_POST_ID)
        );
    }

    /**
     * Проверяет, что если updateImageMetadata обновляет 0 строк (нет обновлённого поста),
     * вызывается EmptyResultDataAccessException с верным описанием.
     */
    @Test
    @DisplayName("Должен выбрасывать EmptyResultDataAccessException если updateImageMetadata обновляет 0 строк")
    void shouldThrowEmptyResultDataAccessExceptionWhenNoRowsUpdatedTest() {
        when(jdbcTemplate.update(anyString(), anyString(), anyLong(), anyString(), anyLong())).thenReturn(0);

        EmptyResultDataAccessException ex = assertThrows(EmptyResultDataAccessException.class, () ->
                imageRepository.updateImageMetadata(NON_EXISTING_POST_ID, IMAGE_NAME, IMAGE_SIZE, URL));

        assertTrue(ex.getMessage().contains("Post not found with id"));
        verify(jdbcTemplate).update(
                eq("""
                        UPDATE post
                        SET image_name = ?, image_size = ?, image_url = ?
                        WHERE id = ?
                        """),
                eq(IMAGE_NAME),
                eq(IMAGE_SIZE),
                eq(URL),
                eq(NON_EXISTING_POST_ID));
    }
}
