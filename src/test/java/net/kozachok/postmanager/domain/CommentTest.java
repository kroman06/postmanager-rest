package net.kozachok.postmanager.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    private Comment commentWithAuthor(UUID authorId) {
        User author = new User();
        author.setId(authorId);

        Comment comment = new Comment();
        comment.setAuthor(author);
        return comment;
    }

    @Test
    void isOwnedBy_shouldReturnTrue_whenUserIsAuthor() {
        UUID authorId = UUID.randomUUID();
        assertThat(commentWithAuthor(authorId).isOwnedBy(authorId)).isTrue();
    }

    @Test
    void isOwnedBy_shouldReturnFalse_whenUserIsNotAuthor() {
        assertThat(commentWithAuthor(UUID.randomUUID()).isOwnedBy(UUID.randomUUID())).isFalse();
    }

    @Test
    void isOwnedBy_shouldReturnFalse_whenAuthorIsNull() {
        Comment comment = new Comment();
        assertThat(comment.isOwnedBy(UUID.randomUUID())).isFalse();
    }
}