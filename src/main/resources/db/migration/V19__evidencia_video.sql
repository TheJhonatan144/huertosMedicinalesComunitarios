ALTER TABLE envio_evidencia DROP CONSTRAINT IF EXISTS envio_evidencia_tipo_contenido_check;
ALTER TABLE envio_evidencia ADD CONSTRAINT envio_evidencia_tipo_contenido_check
    CHECK (tipo_contenido IN ('image/jpeg', 'image/png', 'video/mp4'));
ALTER TABLE envio_evidencia ADD COLUMN duracion_segundos INTEGER;
ALTER TABLE envio_evidencia ADD CONSTRAINT envio_evidencia_duracion_check
    CHECK ((tipo_contenido = 'video/mp4' AND duracion_segundos BETWEEN 1 AND 120)
        OR (tipo_contenido IN ('image/jpeg', 'image/png') AND duracion_segundos IS NULL));
