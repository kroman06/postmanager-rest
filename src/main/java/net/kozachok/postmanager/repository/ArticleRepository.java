package net.kozachok.postmanager.repository;

import net.kozachok.postmanager.domain.Article;
import net.kozachok.postmanager.domain.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"author", "category"})
    Optional<Article> findById(@NonNull UUID id);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Article> findAllByStatus(ArticleStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Article> findAllByAuthorId(UUID authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Optional<Article> findByIdAndStatus(UUID id, ArticleStatus status);
}
