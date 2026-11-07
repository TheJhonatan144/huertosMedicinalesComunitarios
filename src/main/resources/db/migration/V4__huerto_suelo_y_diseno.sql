-- V4: datos para el huerto virtual 2D. Ambos campos son opcionales y SIN valor por defecto:
-- un huerto sin registrar se dibuja con apariencia neutra y se avisa qué campo falta (no se inventan valores).
-- Las listas de valores son una propuesta técnica pendiente de aprobación (ver docs/pendientes.md).

ALTER TABLE huerto
    ADD COLUMN tipo_suelo VARCHAR(20)
        CONSTRAINT ck_huerto_tipo_suelo CHECK (tipo_suelo IN ('FRANCO', 'ARCILLOSO', 'ARENOSO', 'LIMOSO', 'HUMIFERO')),
    ADD COLUMN diseno VARCHAR(20)
        CONSTRAINT ck_huerto_diseno CHECK (diseno IN ('TIERRA', 'VERTICAL', 'COLGANTE', 'PISO'));
