package io.github.habatoo.repositories.image;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Optional;

import static io.github.habatoo.repositories.sql.ImageSqlQueries.GET_IMAGE_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h2>Тесты метода findImageFileNameByPostId в ImageRepository</h2>
 *
 * <p>
 * Класс покрывает сценарии поиска имени файла изображения по идентификатору поста:
 * <ul>
 *     <li>Если пост с заданным id существует, должен возвращаться Optional с именем файла изображения</li>
 *     <li>Если пост не найден (EmptyResultDataAccessException), должен возвращаться пустой Optional</li>
 * </ul>
 * Для тестов используется мок JdbcTemplate, чтобы эмулировать поведение слоя данных.
 * Проверяется не только результат, но и корректность вызова SQL-запроса.
 * </p>
 */
@DisplayName("Тесты поиска имени изображения по postId в ImageRepository")
public class ImageRepositoryFindImageTest extends ImageRepositoryTestBase {

    /**
     * Проверяет, что при наличии поста с заданным id, метод возвращает Optional с именем изображения.
     * Проверяется корректность результата и вызова JdbcTemplate.
     */
    @Test
    @DisplayName("Должен вернуть имя файла изображения, если пост существует")
    void shouldReturnImageFileNameIfPostExistsTest() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(existingPostId))).thenReturn(imageName);

        Optional<String> result = imageRepository.findImageFileNameByPostId(existingPostId);

        assertTrue(result.isPresent());
        assertEquals(imageName, result.get());

        verify(jdbcTemplate).queryForObject(GET_IMAGE_FILE_NAME, String.class, existingPostId);
    }

    /**
     * Проверяет, что если пост не найден (выброшен EmptyResultDataAccessException), метод возвращает пустой Optional.
     * Также проверяет корректность вызова JdbcTemplate.
     */
    @Test
    @DisplayName("Должен вернуть пустой Optional, если пост не найден")
    void shouldReturnEmptyOptionalIfPostNotFoundTest() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(nonExistingPostId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Optional<String> result = imageRepository.findImageFileNameByPostId(nonExistingPostId);

        assertTrue(result.isEmpty());
        verify(jdbcTemplate).queryForObject(GET_IMAGE_FILE_NAME, String.class, nonExistingPostId);
    }
}
