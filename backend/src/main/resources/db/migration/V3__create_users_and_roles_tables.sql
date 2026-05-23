CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    display_name VARCHAR(200) NOT NULL,

    password_hash VARCHAR(255),
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),

    CONSTRAINT ck_users_status
       CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(300),

    CONSTRAINT uk_roles_code UNIQUE (code)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
);

INSERT INTO roles (id, code, name, description) VALUES
    ('00000000-0000-0000-0000-000000000101', 'ANALYST', 'Analyst', 'Uploads files and reviews own processing results'),
    ('00000000-0000-0000-0000-000000000102', 'ADMIN', 'Administrator', 'Triggers processing and approves or rejects completed flows'),
    ('00000000-0000-0000-0000-000000000103', 'AUDITOR', 'Auditor', 'Reviews processing and approval history without modifying it');

INSERT INTO users (
    id,
    username,
    email,
    display_name,
    password_hash,
    must_change_password,
    status
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'analyst01',
    'analyst01@example.local',
    'Analyst 01',
    NULL,
    FALSE,
    'ACTIVE'
);

INSERT INTO user_roles (
    user_id,
    role_id
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000101'
);

INSERT INTO users (
    id,
    username,
    email,
    display_name,
    password_hash,
    must_change_password,
    status
) VALUES (
    '00000000-0000-0000-0000-000000000002',
    'admin01',
    'admin01@example.local',
    'Administrator 01',
    NULL,
    FALSE,
    'ACTIVE'
);

INSERT INTO user_roles (
    user_id,
    role_id
) VALUES (
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000102'
);
