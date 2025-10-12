package io.github.habatoo.handler;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestDummyController {

    @GetMapping("/notfound")
    public void notFound() {
        throw new EmptyResultDataAccessException(1);
    }

    @GetMapping("/illegal")
    public void illegal() {
        throw new IllegalArgumentException("bad arg");
    }

    @GetMapping("/data")
    public void data() {
        throw new DataAccessException("db fail") {
        };
    }

    @GetMapping("/common")
    public void common() {
        throw new RuntimeException("boom");
    }
}
