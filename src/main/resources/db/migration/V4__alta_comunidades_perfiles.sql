ALTER TABLE usuario DROP CONSTRAINT usuario_rol_check;
ALTER TABLE usuario ADD CONSTRAINT usuario_rol_check
    CHECK (rol IN ('GESTOR_HUERTO', 'PARTICIPANTE', 'ADMIN_EVIDENCIAS', 'ADMIN_COMUNIDADES'));

ALTER TABLE comunidad ADD COLUMN contacto_telefono VARCHAR(30) NOT NULL DEFAULT '';
ALTER TABLE comunidad ADD COLUMN contacto_correo VARCHAR(254) NOT NULL DEFAULT '';

ALTER TABLE integrante ADD COLUMN fecha_nacimiento DATE;
ALTER TABLE integrante ADD COLUMN genero VARCHAR(40);
ALTER TABLE integrante ADD COLUMN talentos_certificados TEXT;
ALTER TABLE integrante ADD COLUMN nivel_experiencia VARCHAR(20) NOT NULL DEFAULT 'APRENDIZ'
    CHECK (nivel_experiencia IN ('APRENDIZ', 'OFICIAL', 'MAESTRO'));
