package io.github.habatoo.repositories.post;

import io.github.habatoo.dto.request.PostRequestDto;
import io.github.habatoo.dto.response.PostResponseDto;
import io.github.habatoo.repositories.mapper.PostListRowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты методов findAllPosts, updatePost, deletePost для PostRepositoryImpl</h2>
 *
 * <p>
 * Класс покрывает функциональность основных операций над постами:
 * <ul>
 *     <li>Поиск всех постов с их тегами</li>
 *     <li>Обновление данных поста и его тегов</li>
 *     <li>Удаление поста</li>
 *     <li>Обработка ошибки при попытке удалить несуществующий пост</li>
 * </ul>
 * Для тестирования используются моки JdbcTemplate и вспомогательные RowMapper-ы.
 * Проверяется правильность передаваемых запросов, возвращаемых данных и поведения при ошибках.
 * </p>
 */
@DisplayName("Тесты методов findAllPosts, updatePost, deletePost PostRepositoryImpl.")
class PostRepositoryCrudTest extends PostRepositoryTestBase {

    /**
     * Параметризованный тест для метода findPosts.
     * <p>
     * Проверяет корректность работы поиска постов при различных сочетаниях входных данных:
     * <ul>
     *   <li>Пустой и непустой searchPart (строка поиска по названию или тексту поста)</li>
     *   <li>Пустой список тегов, список из одного и нескольких тегов</li>
     *   <li>Разные значения pageSize (1, 5, 10, 20, 50)</li>
     *   <li>Корректность лимита количества возвращаемых постов (не больше, чем pageSize)</li>
     * </ul>
     * <p>
     * Для каждого набора параметров проверяется, что:
     * <ul>
     *   <li>Результат не null</li>
     *   <li>Размер результата не превышает выбранный pageSize</li>
     *   <li>Дополнительно рекомендуется проверять соответствие найденных постов условиям поиска и тегам</li>
     * </ul>
     * <p>
     * Использует {@link org.junit.jupiter.params.ParameterizedTest} и {@link org.junit.jupiter.params.provider.MethodSource}
     * для генерации различных сочeтаний параметров тестирования.
     *
     * @param searchPart строка поиска по названию или тексту поста
     * @param tags       список тегов для фильтрации постов
     * @param pageNumber номер страницы (1 и более)
     * @param pageSize   количество постов на странице (1, 5, 10, 20, 50)
     */

    @ParameterizedTest
    @MethodSource("provideParameters")
    @DisplayName("Параметризованный тест метода findPosts с разными входными данными")
    void findPostsTest(String searchPart, List<String> tags, int pageNumber, int pageSize) {
        List<PostResponseDto> result = postRepository.findPosts(searchPart, tags, pageNumber, pageSize);

        assertNotNull(result, "Результат не должен быть null");

        assertTrue(result.size() <= pageSize, "Количество возвращаемых постов не должно превышать pageSize");
    }

    /**
     * Параметризованный тест для проверки работы findPosts с tags == null и tags.isEmpty().
     * <p>
     * Убеждается, что метод возвращает корректный результат для обоих вариантов отсутствующих тегов.
     */
    @ParameterizedTest
    @MethodSource("provideNullOrEmptyTags")
    void testFindPostsWithNullOrEmptyTags(List<String> tags, int expectedSize) {
        List<PostResponseDto> postsFromDb = List.of(createPostDto(POST_ID, List.of()));
        when(jdbcTemplate.query(anyString(), any(Object[].class), any(PostListRowMapper.class)))
                .thenReturn(postsFromDb);

        List<PostResponseDto> result = postRepository.findPosts("test", tags, 1, 10);
        assertNotNull(result);
        assertEquals(expectedSize, result.size());
        assertTrue(result.get(0).tags().isEmpty());
    }

    /**
     * Проверяет, что метод updatePost обновляет существующий пост,
     * возвращает обновлённый объект и корректно маппирует теги.
     */
    @Test
    @DisplayName("Должен обновить пост и вернуть обновленный объект с тегами")
    void updatePostShouldReturnUpdatedPostWithTags() {
        PostRequestDto requestDto = new PostRequestDto(POST_ID, TITLE, TEXT, TAGS);
        PostResponseDto postReturned = createPostDto(POST_ID, List.of());

        when(jdbcTemplate.queryForObject(anyString(), eq(postListRowMapper), any(), any(), any(), any())).thenReturn(postReturned);
        when(jdbcTemplate.update(
                eq("""
                        DELETE FROM post_tag WHERE post_id = ?
                        """),
                eq(POST_ID)
        )).thenReturn(1);
        when(jdbcTemplate.batchUpdate(
                eq("""
                        INSERT INTO tag (name)
                        VALUES (?)
                        ON CONFLICT (name) DO NOTHING;
                        """),
                anyList(),
                anyInt(),
                any())
        ).thenReturn(new int[][]{});
        when(jdbcTemplate.batchUpdate(
                eq("""
                        INSERT INTO post_tag (post_id, tag_id)
                        VALUES (?, (SELECT id FROM tag WHERE name = ?))
                        ON CONFLICT (post_id, tag_id) DO NOTHING;
                        """),
                anyList(),
                anyInt(),
                any())
        ).thenReturn(new int[][]{});
        when(jdbcTemplate.queryForList(
                eq("""
                        SELECT t.name FROM tag t
                        JOIN post_tag pt ON t.id = pt.tag_id
                        WHERE pt.post_id = ?
                        """),
                eq(String.class),
                eq(POST_ID)
        )).thenReturn(TAGS);

        PostResponseDto result = postRepository.updatePost(requestDto);

        assertNotNull(result);
        assertEquals(POST_ID, result.id());
        assertEquals(TAGS, result.tags());
        verify(jdbcTemplate).queryForObject(anyString(), eq(postListRowMapper), any(), any(), any(), any());
        verify(jdbcTemplate).batchUpdate(
                eq("""
                        INSERT INTO tag (name)
                        VALUES (?)
                        ON CONFLICT (name) DO NOTHING;
                        """),
                anyList(),
                anyInt(),
                any()
        );
        verify(jdbcTemplate).batchUpdate(
                eq("""
                        INSERT INTO post_tag (post_id, tag_id)
                        VALUES (?, (SELECT id FROM tag WHERE name = ?))
                        ON CONFLICT (post_id, tag_id) DO NOTHING;
                        """),
                anyList(),
                anyInt(),
                any()
        );
        verify(jdbcTemplate).queryForList(anyString(), eq(String.class), eq(POST_ID));
    }


    /**
     * Проверяет успешное удаление поста по его идентификатору.
     * Ожидается отсутствие исключений и вызов нужного SQL-запроса.
     */
    @Test
    @DisplayName("Должен удалить пост успешно")
    void shouldDeletePostSuccessfullyTest() {
        when(jdbcTemplate.update(
                """
                        DELETE FROM post WHERE id = ?
                        """,
                POST_ID)
        ).thenReturn(1);

        assertDoesNotThrow(() -> postRepository.deletePost(POST_ID));

        verify(jdbcTemplate).update(
                """
                        DELETE FROM post WHERE id = ?
                        """,
                POST_ID
        );
    }

    /**
     * Проверяет, что при попытке удалить несуществующий пост будет выброшено IllegalStateException с нужным сообщением.
     */
    @Test
    @DisplayName("Должен выбросить IllegalStateException при удалении несуществующего поста")
    void shouldThrowWhenDeleteNonExistingPostTest() {
        when(jdbcTemplate.update(
                """
                        DELETE FROM post WHERE id = ?
                        """,
                NON_EXISTING_POST_ID
        )).thenReturn(0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> postRepository.deletePost(NON_EXISTING_POST_ID));

        assertTrue(ex.getMessage().contains("Пост не найден для удаления"));

        verify(jdbcTemplate).update(
                """
                        DELETE FROM post WHERE id = ?
                        """,
                NON_EXISTING_POST_ID
        );
    }

    /**
     * Параметризованный юнит-тест для метода countPosts,
     * проверяющий его корректную работу при различных сочетаниях строки поиска и списка тегов.
     * <p>
     * Имитация возврата разного количества записей из базы с помощью mock.
     * Проверяется правильность обработки и возвращаемого значения, включая случай, когда result = null.
     *
     * @param searchPart    строка для поиска по названию или тексту поста
     * @param tags          список тегов для фильтрации постов
     * @param mockResult    мок-возвращаемое значение из jdbcTemplate (Integer)
     * @param expectedCount ожидаемое итоговое значение, возвращаемое методом (должно корректно обрабатываться)
     */
    @ParameterizedTest
    @MethodSource("countPostsParameters")
    void testCountPostsParam(String searchPart, List<String> tags, Integer mockResult, int expectedCount) {
        when(jdbcTemplate.queryForObject(
                anyString(),
                any(Object[].class),
                eq(Integer.class)
        )).thenReturn(mockResult);

        int count = postRepository.countPosts(searchPart, tags);
        assertEquals(expectedCount, count);
    }

    /**
     * Параметризованный юнит-тест для проверки работы метода getPostById с различными входными данными.
     * <p>
     * Проверяет сценарии, когда пост с указанным идентификатором существует и когда он отсутствует:
     * <ul>
     *   <li>Если пост найден, проверяет что Optional содержит ожидаемый объект</li>
     *   <li>Если пост не найден, убеждается что возвращается Optional.empty()</li>
     * </ul>
     *
     * @param id         уникальный идентификатор поста для поиска
     * @param postExists флаг наличия поста в mock-данных (true — найден, false — не найден)
     */
    @ParameterizedTest
    @MethodSource("getPostByIdParameters")
    void testGetPostByIdParam(Long id, boolean postExists) {
        PostResponseDto basePost = createPostDto(POST_ID, List.of());

        if (postExists) {
            PostResponseDto enrichedPost = createPostDto(POST_ID, TAGS);

            when(jdbcTemplate.queryForObject(
                    anyString(),
                    any(PostListRowMapper.class),
                    eq(id)
            )).thenReturn(basePost);
            when(jdbcTemplate.queryForList(
                    eq("""
                            SELECT t.name FROM tag t
                            JOIN post_tag pt ON t.id = pt.tag_id
                            WHERE pt.post_id = ?
                            """),
                    eq(String.class),
                    any(Long.class)
            )).thenReturn(TAGS);

            Optional<PostResponseDto> result = postRepository.getPostById(id);

            assertTrue(result.isPresent());
            assertEquals(enrichedPost, result.get());
        } else {
            when(jdbcTemplate.queryForObject(
                    anyString(), any(PostListRowMapper.class), eq(id))
            ).thenThrow(new EmptyResultDataAccessException(1));

            Optional<PostResponseDto> result = postRepository.getPostById(id);
            assertFalse(result.isPresent());
        }
    }
}
