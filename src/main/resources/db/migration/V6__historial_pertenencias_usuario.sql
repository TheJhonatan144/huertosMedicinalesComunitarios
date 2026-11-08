-- Conserva las pertenencias dadas de baja y permite que la cuenta se vincule a otra comunidad después.
-- La unicidad aplica únicamente a membresías activas: una cuenta no puede estar activa en dos comunidades.
ALTER TABLE integrante DROP CONSTRAINT integrante_usuario_id_key;
CREATE UNIQUE INDEX uq_integrante_usuario_activo
    ON integrante (usuario_id)
    WHERE activo;
