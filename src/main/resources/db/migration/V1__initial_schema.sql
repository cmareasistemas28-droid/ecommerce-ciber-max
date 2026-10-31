CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    auth_provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS usuario_rol (
    usuario_id UUID NOT NULL,
    rol VARCHAR(50) NOT NULL,
    PRIMARY KEY (usuario_id, rol),
    CONSTRAINT fk_usuario_rol_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS empleados (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL UNIQUE,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    dni VARCHAR(10) NOT NULL UNIQUE,
    fecha_nacimiento DATE,
    telefono VARCHAR(255),
    direccion VARCHAR(255),
    referencia VARCHAR(255),
    foto VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_empleado_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
