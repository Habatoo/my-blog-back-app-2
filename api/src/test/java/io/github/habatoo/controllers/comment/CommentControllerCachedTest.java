package io.github.habatoo.controllers.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.habatoo.controllers.CommentController;
import io.github.habatoo.dto.request.CommentCreateRequestDto;
import io.github.habatoo.dto.request.CommentRequestDto;
import io.github.habatoo.dto.response.CommentResponseDto;
import io.github.habatoo.handlers.GlobalExceptionHandler;
import io.github.habatoo.service.CommentService;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * <h2>Тесты для CommentController c максимальным кешированием MockMvc</h2>
 *
 * <p>
 * Класс покрывает unit-тесты основных методов контроллера CommentController с использованием Standalone MockMvc.
 * MockMvc и тестовые данные инициализируются единожды в @BeforeAll для максимальной производительности.
 * Каждый тест проверяет корректность эндпоинтов, обработку ошибок и возврат ожидаемых ответов.
 * Тесты полностью изолированы от Spring-контекста — мокируется только сервисный слой CommentService.
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Тесты unit уровня методов контроллера CommentController с использованием Cached MockMvc.")
class CommentControllerCachedTest {

    private MockMvc mockMvc;
    private CommentService commentService;
    private ObjectMapper objectMapper;

    private CommentResponseDto mockComment1;
    private CommentResponseDto mockComment2;
    private CommentCreateRequestDto mockCreateRequest;
    private CommentRequestDto mockUpdateRequest;

    @BeforeAll
    void setUpAll() {
        commentService = mock(CommentService.class);
        CommentController commentController = new CommentController(commentService);
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        initializeTestData();
    }

    /**
     * Инициализация тестовых данных
     */
    private void initializeTestData() {
        mockComment1 = new CommentResponseDto(1L, "Первый комментарий", 1L);
        mockComment2 = new CommentResponseDto(2L, "Второй комментарий", 1L);

        mockCreateRequest = new CommentCreateRequestDto(1L, "Новый комментарий");
        mockUpdateRequest = new CommentRequestDto(1L, "Обновленный комментарий", 1L);
    }

    @BeforeEach
    void setUp() {
        reset(commentService);
    }

    /**
     * Тест успешного получения списка комментариев
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/comments - должен вернуть список комментариев")
    void getCommentsByPostIdWithValidPostIdTest() throws Exception {
        Long postId = 1L;
        List<CommentResponseDto> mockComments = Arrays.asList(mockComment1, mockComment2);
        when(commentService.getCommentsByPostId(postId)).thenReturn(mockComments);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        assertTrue(responseContent.contains("\"id\":1"));
        assertTrue(responseContent.contains("\"postId\":1"));
        assertTrue(responseContent.contains("Первый комментарий"));
        assertTrue(responseContent.contains("\"id\":2"));
        assertTrue(responseContent.contains("Второй комментарий"));
        assertTrue(responseContent.startsWith("["));
        assertTrue(responseContent.endsWith("]"));

        List<Map<String, Object>> responseList = objectMapper.readValue(responseContent, List.class);
        assertEquals(2, responseList.size());

        Map<String, Object> firstComment = responseList.get(0);
        assertEquals(1, firstComment.get("id"));
        assertEquals(1, firstComment.get("postId"));
        assertEquals("Первый комментарий", firstComment.get("text"));

        Map<String, Object> secondComment = responseList.get(1);
        assertEquals(2, secondComment.get("id"));
        assertEquals(1, secondComment.get("postId"));
        assertEquals("Второй комментарий", secondComment.get("text"));

        verify(commentService, times(1)).getCommentsByPostId(postId);
    }

    /**
     * Тест получения пустого списка комментариев
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/comments - должен вернуть пустой список")
    void getCommentsByPostIdWithNoCommentsTest() throws Exception {
        Long postId = 2L;
        when(commentService.getCommentsByPostId(postId)).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string("[]"));

        verify(commentService, times(1)).getCommentsByPostId(postId);
    }

    /**
     * Тест получения несуществующего комментария
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/comments/{commentId} - должен вернуть 404 для несуществующего комментария")
    void getCommentByPostIdAndIdWithNonExistentCommentTest() throws Exception {
        Long postId = 1L;
        Long commentId = 999L;
        when(commentService.getCommentByPostIdAndId(postId, commentId))
                .thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(commentService, times(1)).getCommentByPostIdAndId(postId, commentId);
    }

    /**
     * Тест успешного получения комментария по ID
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/comments/{commentId} - должен вернуть комментарий")
    void getCommentByPostIdAndIdWithValidIdsTest() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;
        when(commentService.getCommentByPostIdAndId(postId, commentId))
                .thenReturn(Optional.of(mockComment1));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        CommentResponseDto responseComment = objectMapper.readValue(responseContent, CommentResponseDto.class);

        assertEquals(mockComment1.id(), responseComment.id());
        assertEquals(mockComment1.postId(), responseComment.postId());
        assertEquals(mockComment1.text(), responseComment.text());

        verify(commentService, times(1)).getCommentByPostIdAndId(postId, commentId);
    }

    /**
     * Тест успешного создания комментария
     */
    @Test
    @DisplayName("POST /api/posts/{postId}/comments - должен создать комментарий")
    void createCommentWithValidRequestTest() throws Exception {
        Long postId = 1L;
        when(commentService.createComment(any(CommentCreateRequestDto.class)))
                .thenReturn(mockComment1);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        CommentResponseDto responseComment = objectMapper.readValue(responseContent, CommentResponseDto.class);

        assertEquals(mockComment1.id(), responseComment.id());
        assertEquals(mockComment1.postId(), responseComment.postId());
        assertEquals(mockComment1.text(), responseComment.text());

        verify(commentService, times(1)).createComment(any(CommentCreateRequestDto.class));
    }

    /**
     * Тест успешного обновления комментария
     */
    @Test
    @DisplayName("PUT /api/posts/{postId}/comments/{commentId} - должен обновить комментарий")
    void updateCommentWithValidRequestTest() throws Exception {
        Long postId = 1L;
        Long commentId = 2L;
        CommentResponseDto updatedComment = new CommentResponseDto(2L, "Обновленный комментарий", 1L);

        when(commentService.updateComment(any()))
                .thenReturn(updatedComment);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        CommentResponseDto responseComment = objectMapper.readValue(responseContent, CommentResponseDto.class);

        assertEquals(updatedComment.id(), responseComment.id());
        assertEquals(updatedComment.postId(), responseComment.postId());
        assertEquals(updatedComment.text(), responseComment.text());
        assertEquals("Обновленный комментарий", responseComment.text());

        verify(commentService, times(1)).updateComment(mockUpdateRequest);
    }

    /**
     * Тест успешного удаления комментария
     */
    @Test
    @DisplayName("DELETE /api/posts/{postId}/comments/{commentId} - должен удалить комментарий")
    void deleteCommentWithValidIdsTest() throws Exception {
        Long postId = 1L;
        Long commentId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(commentService, times(1)).deleteComment(postId, commentId);
    }

    /**
     * Тест обработки исключения при невалидных данных
     */
    @Test
    @DisplayName("POST /api/posts/{postId}/comments - должен вернуть 400 при невалидных данных")
    void createCommentWithInvalidDataTest() throws Exception {
        Long postId = 1L;
        when(commentService.createComment(any(CommentCreateRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Invalid comment data"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCreateRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(commentService, times(1)).createComment(any(CommentCreateRequestDto.class));
    }

    /**
     * Тест обработки исключения при ошибке доступа к данным
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/comments - должен вернуть 500 при ошибке БД")
    void getCommentsByPostIdWithDataAccessErrorTest() throws Exception {
        Long postId = 1L;
        when(commentService.getCommentsByPostId(postId))
                .thenThrow(new org.springframework.dao.DataAccessException("Database error") {
                });

        mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        verify(commentService, times(1)).getCommentsByPostId(postId);
    }

    /**
     * Тест различных валидных ID постов
     */
    @Test
    @DisplayName("GET /api/posts/{postId}/comments - должен работать с разными ID постов")
    void getCommentsByPostIdWithDifferentPostIdsTest() throws Exception {
        Long[] postIds = {1L, 100L, 9999L};
        for (Long postId : postIds) {
            reset(commentService);
            List<CommentResponseDto> comments = Collections.singletonList(
                    new CommentResponseDto(1L, "Comment for post " + postId, postId));

            when(commentService.getCommentsByPostId(postId)).thenReturn(comments);

            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/{postId}/comments", postId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(commentService, times(1)).getCommentsByPostId(postId);
        }
    }
}