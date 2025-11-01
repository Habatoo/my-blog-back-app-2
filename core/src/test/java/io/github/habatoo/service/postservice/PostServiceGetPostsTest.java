package io.github.habatoo.service.postservice;

import io.github.habatoo.dto.response.PostListResponseDto;
import io.github.habatoo.dto.response.PostResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Тесты метода getPosts класса PostServiceImpl
 */
@DisplayName("Тесты метода getPosts для проверки фильтрации, тегов и пагинации")
class PostServiceGetPostsTest extends PostServiceTestBase {

    /**
     * Параметризованный тест: проверяет поиск и фильтрацию постов по строке поиска и тегам через getPosts().
     */
    @ParameterizedTest
    @MethodSource("provideSearchFilters")
    @DisplayName("Параметризованный тест поиска и фильтрации постов через getPosts")
    void shouldReturnSearchedPosts(String search, int expectedCount, List<PostResponseDto> mockPosts) {
        List<String> words = Arrays.stream(search.split("\\s+"))
                .filter(w -> !w.isBlank())
                .toList();
        List<String> tags = words.stream()
                .filter(w -> w.startsWith("#"))
                .map(w -> w.substring(1))
                .toList();
        String searchPart = words.stream()
                .filter(w -> !w.startsWith("#"))
                .collect(Collectors.joining(" "));

        when(postRepository.findPosts(eq(searchPart), eq(tags), anyInt(), anyInt()))
                .thenReturn(mockPosts.stream()
                        .filter(post -> {
                            boolean titleOrTextMatch = searchPart.isEmpty()
                                    || post.title().contains(searchPart)
                                    || post.text().contains(searchPart);
                            boolean tagMatch = tags.isEmpty()
                                    || tags.stream().allMatch(tag -> post.tags().contains(tag));
                            return titleOrTextMatch && tagMatch;
                        }).toList());

        when(postRepository.countPosts(eq(searchPart), eq(tags)))
                .thenReturn(expectedCount);

        PostListResponseDto result = postService.getPosts(search, 1, 100);

        assertEquals(expectedCount, result.posts().size());

        for (PostResponseDto post : result.posts()) {
            boolean titleOrTextMatch = searchPart.isEmpty()
                    || post.title().contains(searchPart)
                    || post.text().contains(searchPart);

            boolean tagMatch = tags.isEmpty()
                    || tags.stream().allMatch(tag ->
                    post.tags().contains(tag)
            );
            assertTrue(titleOrTextMatch && tagMatch);
        }
    }

    /**
     * Параметризованный тест: проверяет корректность возврата постов для разных номеров страниц и размеров страницы через getPosts().
     */
    @ParameterizedTest
    @MethodSource("providePagination")
    @DisplayName("Параметризованный тест проверки пагинации через getPosts")
    void shouldPaginateProperly(int pageSize, int pageNumber, int expectedCount, List<PostResponseDto> mockPosts) {
        int totalCount = mockPosts.size();

        int fromIndex = Math.min((pageNumber - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);

        List<PostResponseDto> pagedPosts = mockPosts.subList(fromIndex, toIndex);

        when(postRepository.findPosts(anyString(), anyList(), eq(pageNumber), eq(pageSize)))
                .thenReturn(pagedPosts);
        when(postRepository.countPosts(anyString(), anyList()))
                .thenReturn(totalCount);

        PostListResponseDto response = postService.getPosts("", pageNumber, pageSize);

        assertEquals(expectedCount, response.posts().size());
        assertEquals(fromIndex > 0, response.hasPrev());
        assertEquals(toIndex < totalCount, response.hasNext());
        assertEquals((int) Math.ceil((double) totalCount / pageSize), response.lastPage());
    }
}
