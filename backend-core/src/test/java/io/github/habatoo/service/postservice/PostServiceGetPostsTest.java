package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostListResponse;
import io.github.habatoo.dto.response.PostResponse;
import io.github.habatoo.service.impl.PostServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Тесты метода getPosts класса PostServiceImpl
 */
@DisplayName("Тесты метода getPosts для проверки фильтрации, тегов и пагинации")
class PostServiceGetPostsTest extends PostServiceTestBase {

    @ParameterizedTest
    @CsvSource({
            "'', 5",
            "'Spring', 1",
            "'ramework', 2",
            "'#java', 2",
            "'Spring #java', 1",
            "'#backend', 2",
            "'#unknowntag', 0",
            "'Hash', 1",
            "'Java', 1",
            "'Framework', 2"
    })
    @DisplayName("Тесты поиска и фильтрации getPosts")
    void shouldReturnSearchedPosts(
            String search,
            int expectedCount) {
        List<PostResponse> data = List.of(
                new PostResponse(1L, "Spring Framework", "Spring — это каркас...", List.of("java", "backend"), 5, 2),
                new PostResponse(2L, "PostgreSQL Integration", "Настроим postgres...", List.of("db", "backend"), 3, 1),
                new PostResponse(3L, "Советы по Markdown", "Учимся оформлять post", List.of("markdown"), 1, 0),
                new PostResponse(4L, "Framework Tips", "Framework rocks!", List.of("framework"), 2, 5),
                new PostResponse(5L, "Java Collections", "about HashMap и List", List.of("java"), 4, 3)
        );
        when(postRepository.findAllPosts()).thenReturn(data);
        postService = new PostServiceImpl(postRepository, fileStorageService);

        PostListResponse result = postService.getPosts(search, 1, 100);
        assertEquals(expectedCount, result.posts().size());

        List<String> words = getWords(search);
        List<String> tags = getTags(words);
        String searchPart = getSearchPart(words);

        for (PostResponse post : result.posts()) {
            boolean titleOrTextMatch = searchPart.isEmpty()
                    || post.title().contains(searchPart)
                    || post.text().contains(searchPart);

            boolean tagMatch = tags.isEmpty()
                    || tags.stream().allMatch(tag ->
                    post.tags().stream().anyMatch(t -> t.equals(tag))
            );

            assertTrue(titleOrTextMatch && tagMatch);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, 1",       // страница 1, размер 1 — всегда 1 пост на странице
            "5, 1, 5",       // страница 1, размер 5
            "10, 2, 10",     // страница 2, размер 10 (10 постов начиная с 11)
            "20, 3, 13",     // страница 3, размер 20 (последние 13 постов на странице)
            "50, 2, 3"       // страница 2, размер 50 (только последние 3 поста)
    })
    void shouldPaginateProperly(int pageSize, int page, int expectedCount) {
        List<PostResponse> data = IntStream.rangeClosed(1, 53)
                .mapToObj(i -> new PostResponse(
                        (long) i,
                        "Title " + i,
                        "Text " + i,
                        List.of("tag" + (i % 5)),
                        i, i
                ))
                .toList();
        when(postRepository.findAllPosts()).thenReturn(data);
        postService = new PostServiceImpl(postRepository, fileStorageService);

        PostListResponse response = postService.getPosts("", page, pageSize);
        assertEquals(expectedCount, response.posts().size());

        int totalCount = 53;
        int fromIndex = Math.min((page - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);

        assertEquals(fromIndex > 0, response.hasPrev());
        assertEquals(toIndex < totalCount, response.hasNext());
        assertEquals(totalCount, response.lastPage());
    }

    private static String getSearchPart(List<String> words) {
        return words.stream()
                .filter(w -> !w.startsWith("#"))
                .collect(Collectors.joining(" "));
    }

    private static List<String> getTags(List<String> words) {
        return words.stream()
                .filter(w -> w.startsWith("#"))
                .map(w -> w.substring(1))
                .toList();
    }

    private static List<String> getWords(String search) {
        return Arrays.stream(search.split("\\s+"))
                .filter(w -> !w.isBlank())
                .toList();
    }
}
