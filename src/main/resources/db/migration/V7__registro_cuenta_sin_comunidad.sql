ALTER TABLE usuario ADD COLUMN correo VARCHAR(254);
ALTER TABLE usuario ADD COLUMN telefono VARCHAR(30);
ALTER TABLE usuario ADD COLUMN fecha_nacimiento DATE;
ALTER TABLE usuario ADD COLUMN genero VARCHAR(40);
ALTER TABLE usuario ADD COLUMN talentos_certificados TEXT;
ALTER TABLE usuario ADD COLUMN nivel_experiencia VARCHAR(20);

ALTER TABLE usuario DROP CONSTRAINT usuario_rol_check;
ALTER TABLE usuario ADD CONSTRAINT usuario_rol_check
    CHECK (rol IN ('GESTOR_HUERTO', 'PARTICIPANTE', 'ADMIN_EVIDENCIAS', 'SIN_ASIGNAR'));
