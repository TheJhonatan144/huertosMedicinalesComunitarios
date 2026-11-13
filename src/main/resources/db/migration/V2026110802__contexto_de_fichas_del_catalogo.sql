-- Amplía únicamente la ficha común; no modifica siembras ni existencias.
ALTER TABLE especie
 ADD COLUMN observaciones_altitud VARCHAR(1000),
 ADD COLUMN observaciones_ciclo VARCHAR(1000),
 ADD COLUMN contexto_cualidad VARCHAR(1500);

ALTER TABLE especie DROP CONSTRAINT especie_cualidad_check;
ALTER TABLE especie ADD CONSTRAINT especie_cualidad_check
 CHECK (cualidad IN ('CALIENTE', 'FRIA', 'FRESCA', 'VARIABLE'));
ALTER TABLE especie ADD CONSTRAINT especie_contexto_variable_check
 CHECK (cualidad IS DISTINCT FROM 'VARIABLE' OR NULLIF(btrim(contexto_cualidad), '') IS NOT NULL);
ALTER TABLE especie ADD CONSTRAINT especie_contexto_fuente_check
 CHECK (NULLIF(btrim(contexto_cualidad), '') IS NULL OR NULLIF(btrim(fuente_documental), '') IS NOT NULL);
