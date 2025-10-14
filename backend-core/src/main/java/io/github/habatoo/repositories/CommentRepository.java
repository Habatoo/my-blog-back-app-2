package io.github.habatoo.repositories;

import io.github.habatoo.dto.request.CommentCreateRequest;
import io.github.habatoo.dto.response.CommentResponse;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с комментариями блога.
 * Определяет контракты для операций доступа к данным комментариев.
 *
 * @see CommentRepository
 */
public interface CommentRepository {

    /**
     * Выполняет поиск всех комментариев, связанных с указанным постом.
     * Результаты возвращаются в виде списка объектов CommentResponse.
     *
     * @param postId идентификатор поста для поиска комментариев
     * @return список комментариев для указанного поста, может быть пустым
     * @throws DataAccessException при ошибках доступа к базе данных
     */
    List<CommentResponse> findByPostId(Long postId);

    /**
     * Выполняет поиск конкретного комментария по идентификаторам поста и комментария.
     * Используется для проверки принадлежности комментария к указанному посту.
     *
     * @param postId    идентификатор поста
     * @param commentId идентификатор комментария
     * @return Optional с найденным комментарием или empty если не найден
     * @throws DataAccessException при ошибках доступа к базе данных
     */
    Optional<CommentResponse> findByPostIdAndId(Long postId, Long commentId);

    /**
     * Сохраняет новый комментарий в базе данных и возвращает сгенерированный идентификатор.
     * Автоматически устанавливает временные метки создания и обновления.
     *
     * @param commentCreateRequest DTO с данными для создания комментария
     * @return сгенерированный идентификатор нового комментария
     * @throws DataAccessException   при ошибках сохранения в базу данных
     * @throws IllegalStateException если не удалось получить сгенерированный ключ
     */
    CommentResponse save(CommentCreateRequest commentCreateRequest);

    /**
     * Обновляет текст существующего комментария и временную метку обновления.
     * Возвращает количество обновленных записей (0 или 1).
     *
     * @param postId идентификатор поста
     * @param commentId идентификатор обновляемого комментария
     * @param text      новый текст комментария
     * @return количество обновленных записей
     * @throws DataAccessException при ошибках обновления в базе данных
     */
    CommentResponse updateText(Long postId, Long commentId, String text);

    /**
     * Удаляет комментарий по идентификатору.
     * Возвращает количество удаленных записей (0 или 1).
     *
     * @param commentId идентификатор удаляемого комментария
     * @return количество удаленных записей
     * @throws DataAccessException при ошибках удаления из базы данных
     */
    int deleteById(Long commentId);

}
