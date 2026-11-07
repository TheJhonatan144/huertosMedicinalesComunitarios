ALTER TABLE planta ADD COLUMN plantada BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX ix_planta_plantada ON planta (huerto_id, nombre_comun) WHERE plantada;
