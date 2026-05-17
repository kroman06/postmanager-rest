package net.kozachok.postmanager.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {

    private Article articleWithStatus(ArticleStatus status) {
        Article article = new Article();
        article.setStatus(status);
        return article;
    }

    private Article articleWithAuthor(UUID authorId) {
        User author = new User();
        author.setId(authorId);

        Article article = new Article();
        article.setStatus(ArticleStatus.DRAFT);
        article.setAuthor(author);
        return article;
    }

    //  canPublish

    @Test
    void canPublish_shouldReturnTrue_whenStatusIsDraft() {
        assertThat(articleWithStatus(ArticleStatus.DRAFT).canPublish()).isTrue();
    }

    @Test
    void canPublish_shouldReturnFalse_whenStatusIsPublished() {
        assertThat(articleWithStatus(ArticleStatus.PUBLISHED).canPublish()).isFalse();
    }

    @Test
    void canPublish_shouldReturnFalse_whenStatusIsArchived() {
        assertThat(articleWithStatus(ArticleStatus.ARCHIVED).canPublish()).isFalse();
    }

    // canArchive

    @Test
    void canArchive_shouldReturnTrue_whenStatusIsPublished() {
        assertThat(articleWithStatus(ArticleStatus.PUBLISHED).canArchive()).isTrue();
    }

    @Test
    void canArchive_shouldReturnFalse_whenStatusIsDraft() {
        assertThat(articleWithStatus(ArticleStatus.DRAFT).canArchive()).isFalse();
    }

    @Test
    void canArchive_shouldReturnFalse_whenStatusIsArchived() {
        assertThat(articleWithStatus(ArticleStatus.ARCHIVED).canArchive()).isFalse();
    }

    //  canRestore

    @Test
    void canRestore_shouldReturnTrue_whenStatusIsArchived() {
        assertThat(articleWithStatus(ArticleStatus.ARCHIVED).canRestore()).isTrue();
    }

    @Test
    void canRestore_shouldReturnFalse_whenStatusIsDraft() {
        assertThat(articleWithStatus(ArticleStatus.DRAFT).canRestore()).isFalse();
    }

    @Test
    void canRestore_shouldReturnFalse_whenStatusIsPublished() {
        assertThat(articleWithStatus(ArticleStatus.PUBLISHED).canRestore()).isFalse();
    }

    // isOwnedBy

    @Test
    void isOwnedBy_shouldReturnTrue_whenUserIsAuthor() {
        UUID authorId = UUID.randomUUID();
        assertThat(articleWithAuthor(authorId).isOwnedBy(authorId)).isTrue();
    }

    @Test
    void isOwnedBy_shouldReturnFalse_whenUserIsNotAuthor() {
        assertThat(articleWithAuthor(UUID.randomUUID()).isOwnedBy(UUID.randomUUID())).isFalse();
    }

    @Test
    void isOwnedBy_shouldReturnFalse_whenAuthorIsNull() {
        Article article = new Article();
        assertThat(article.isOwnedBy(UUID.randomUUID())).isFalse();
    }
}