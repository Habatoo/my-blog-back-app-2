package io.github.habatoo.contfiguration;

import io.github.habatoo.controller.CommentController;
import io.github.habatoo.repository.CommentRepository;
import io.github.habatoo.repository.impl.CommentRepositoryImpl;
import io.github.habatoo.repository.mapper.CommentRowMapper;
import io.github.habatoo.service.CommentService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

@Configuration
@EnableWebMvc
public class TestConfiguration {
    @Bean
    public CommentController commentController() {
        return new CommentController(commentService());
    }

    @Bean
    public CommentService commentService() {
        return Mockito.mock(CommentService.class);
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    public CommentRowMapper commentRowMapper() {
        return new CommentRowMapper();
    }

    @Bean
    public CommentRepository commentRepository(JdbcTemplate jdbcTemplate, CommentRowMapper rowMapper) {
        return new CommentRepositoryImpl(jdbcTemplate, rowMapper);
    }
}

