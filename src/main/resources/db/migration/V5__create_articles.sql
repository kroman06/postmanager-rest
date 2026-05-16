CREATE TABLE IF NOT EXISTS articles (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    author_id    UUID         NOT NULL,
    category_id  INT,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT now(),
    published_at TIMESTAMP,
    CONSTRAINT chk_article_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT fk_articles_author
        FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT fk_articles_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX idx_articles_author_id     ON articles(author_id);
CREATE INDEX idx_articles_status        ON articles(status);
CREATE INDEX idx_articles_author_status ON articles(author_id, status);