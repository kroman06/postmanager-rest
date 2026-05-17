package net.kozachok.postmanager.repository;

import net.kozachok.postmanager.domain.Article;
import net.kozachok.postmanager.domain.ArticleStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"author", "category"})
    Optional<Article> findById(@NonNull UUID id);

    @EntityGraph(attributePaths = {"author", "category"})
    Optional<Article> findByIdAndStatus(UUID id, ArticleStatus status);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Article> findAllByStatusIn(List<ArticleStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Article> findAllByAuthorIdAndStatus(UUID authorId, ArticleStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Article> findAllByStatusAndCategoryId(ArticleStatus status, Integer categoryId, Pageable pageable);
}
