UPDATE roles
SET code = 'ADMINISTRATOR',
    name = 'Administrator'
WHERE code = 'ADMIN';

INSERT INTO roles (id, code, name, description)
VALUES (
   '00000000-0000-0000-0000-000000000104',
   'ROOT',
   'Root',
   'Manages users, passwords, roles and sessions'
)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE auth_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,

    refresh_token_hash VARCHAR(255) NOT NULL,

    user_agent VARCHAR(500),
    ip_address VARCHAR(100),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    revoked_at TIMESTAMP,
    revoked_by_user_id UUID,
    revoke_reason VARCHAR(300),

    CONSTRAINT fk_auth_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT fk_auth_sessions_revoked_by
        FOREIGN KEY (revoked_by_user_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX uk_auth_sessions_active_user
    ON auth_sessions (user_id)
    WHERE revoked_at IS NULL;

CREATE INDEX idx_auth_sessions_user_id
    ON auth_sessions (user_id);

CREATE INDEX idx_auth_sessions_expires_at
    ON auth_sessions (expires_at);