CREATE TABLE IF NOT EXISTS refresh_tokens (
      id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id    UUID        NOT NULL,
      token_hash VARCHAR(64) NOT NULL UNIQUE,
      expires_at TIMESTAMP   NOT NULL,
      revoked    BOOLEAN     NOT NULL DEFAULT false,
      created_at TIMESTAMP   NOT NULL DEFAULT now(),
      CONSTRAINT fk_refresh_tokens_user
          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);