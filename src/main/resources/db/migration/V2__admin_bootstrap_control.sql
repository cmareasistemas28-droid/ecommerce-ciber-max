CREATE TABLE IF NOT EXISTS admin_bootstrap_control (
    id SMALLINT PRIMARY KEY,
    initialized BOOLEAN NOT NULL DEFAULT FALSE,
    initialized_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT ck_admin_bootstrap_control_singleton CHECK (id = 1)
);

INSERT INTO admin_bootstrap_control (id, initialized)
VALUES (1, FALSE)
ON CONFLICT (id) DO NOTHING;
