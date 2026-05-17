package net.kozachok.postmanager.repository;

import net.kozachok.postmanager.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByArticleId(UUID articleId);
}
