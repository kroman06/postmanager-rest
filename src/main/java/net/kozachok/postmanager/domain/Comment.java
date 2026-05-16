package net.kozachok.postmanager.domain;

import jakarta.persistence.*;
import lombok.*;
import net.kozachok.postmanager.domain.base.BaseEntity;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    public boolean isOwnedBy(UUID userId) {
        return author != null && author.getId().equals(userId);
    }
}
