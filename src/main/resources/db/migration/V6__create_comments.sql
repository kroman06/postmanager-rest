CREATE TABLE IF NOT EXISTS comments (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    content    TEXT      NOT NULL,
    author_id  UUID      NOT NULL,
    article_id UUID      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id)  REFERENCES users(id),
    CONSTRAINT fk_comments_article
        FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE
);

CREATE INDEX idx_comments_article_id ON comments(article_id);
CREATE INDEX idx_comments_author_id  ON comments(author_id);