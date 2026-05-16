CREATE TABLE IF NOT EXISTS categories (
      id          INT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
      name        VARCHAR(100) NOT NULL UNIQUE,
      description TEXT,
      created_at  TIMESTAMP    NOT NULL DEFAULT now()
);