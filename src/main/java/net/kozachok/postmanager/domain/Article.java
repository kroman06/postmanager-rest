package net.kozachok.postmanager.domain;

import net.kozachok.postmanager.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "articles")
public class Article extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status;

    @Column
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public boolean isOwnedBy(UUID userId) {
        return author != null && author.getId().equals(userId);
    }

    public boolean canPublish() {
        return isDraft();
    }

    public boolean isDraft() {
        return status == ArticleStatus.DRAFT;
    }

    public boolean canArchive() {
        return status == ArticleStatus.PUBLISHED;
    }

    public boolean canRestore() {
        return status == ArticleStatus.ARCHIVED;
    }
}
