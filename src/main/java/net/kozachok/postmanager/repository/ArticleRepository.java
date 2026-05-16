package net.kozachok.postmanager.repository;

import net.kozachok.postmanager.domain.Article;
import net.kozachok.postmanager.domain.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
    Page<Article> findAllByStatus(ArticleStatus status, Pageable pageable);
    Page<Article> findAllByAuthorId(UUID authorId, Pageable pageable);
    Optional<Article> findByIdAndStatus(UUID id, ArticleStatus status);
}
