package io.github.habatoo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.habatoo.contfiguration.TestConfiguration;
import io.github.habatoo.dto.response.CommentResponse;
import io.github.habatoo.service.CommentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class CommentControllerIntegrationTest {

    @Resource
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Resource
    private CommentService commentService; // mock

    private ObjectMapper objectMapper = new ObjectMapper();

//    @Before
//    public void setup() {
//
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
//    }

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(new CommentController(commentService)).build();
    }

    @Test
    public void testGetCommentsByPostId_returnsCommentsList() throws Exception {
        List<CommentResponse> responses = Arrays.asList(
                new CommentResponse(1L, "Comment 1", 100L),
                new CommentResponse(2L, "Comment 2", 100L)
        );
        when(commentService.getCommentsByPostId(100L)).thenReturn(responses);

        mockMvc.perform(get("/api/posts/100/comments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Comment 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].text").value("Comment 2"));
    }
}

