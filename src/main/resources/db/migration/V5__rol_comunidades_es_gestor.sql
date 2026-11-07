-- La administración de comunidades corresponde al rol gestor existente; no se agrega un rol independiente.
ALTER TABLE usuario DROP CONSTRAINT usuario_rol_check;
ALTER TABLE usuario ADD CONSTRAINT usuario_rol_check
    CHECK (rol IN ('GESTOR_HUERTO', 'PARTICIPANTE', 'ADMIN_EVIDENCIAS'));
