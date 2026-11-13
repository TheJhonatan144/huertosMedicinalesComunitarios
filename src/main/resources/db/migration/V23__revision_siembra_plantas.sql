-- La siembra se muestra y crece desde su fecha real, aunque siga pendiente de revisión.
ALTER TABLE planta ADD COLUMN estado_revision VARCHAR(20) NOT NULL DEFAULT 'APROBADA';
ALTER TABLE planta ADD COLUMN plantada_en TIMESTAMP WITH TIME ZONE;
ALTER TABLE planta ADD COLUMN observaciones_revision TEXT;
UPDATE planta SET plantada_en = creada_en WHERE plantada = TRUE;
ALTER TABLE planta ADD CONSTRAINT ck_planta_estado_revision
    CHECK (estado_revision IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'));
CREATE INDEX ix_planta_revision_pendiente ON planta (huerto_id, estado_revision) WHERE plantada = TRUE;