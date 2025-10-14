package io.github.habatoo.handlers;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Тестовый REST контроллер, демонстрирующий выбрасываемые исключения.
 *
 * <p>Содержит методы, которые намеренно выбрасывают разные исключения,
 * чтобы проверить глобальную обработку ошибок и поведение приложения при ошибках.</p>
 */
@RestController
public class TestDummyController {

    /**
     * Метод, при вызове которого выбрасывается исключение EmptyResultDataAccessException,
     * имитирующее отсутствие результата в базе данных.
     *
     * @throws EmptyResultDataAccessException всегда
     */
    @GetMapping("/notfound")
    public void notFound() {
        throw new EmptyResultDataAccessException(1);
    }

    /**
     * Метод, при вызове которого выбрасывается IllegalArgumentException,
     * имитирующее ошибку при некорректном аргументе.
     *
     * @throws IllegalArgumentException всегда
     */
    @GetMapping("/illegal")
    public void illegal() {
        throw new IllegalArgumentException("bad arg");
    }

    /**
     * Метод, при вызове которого выбрасывается анонимное исключение DataAccessException,
     * имитирующее ошибку доступа к данным (например, сбой базы данных).
     *
     * @throws DataAccessException всегда
     */
    @GetMapping("/data")
    public void data() {
        throw new DataAccessException("db fail") {
        };
    }

    /**
     * Метод, при вызове которого выбрасывается общее исключение RuntimeException,
     * имитирующее неожиданную ошибку приложения.
     *
     * @throws RuntimeException всегда
     */
    @GetMapping("/common")
    public void common() {
        throw new RuntimeException("boom");
    }
}
