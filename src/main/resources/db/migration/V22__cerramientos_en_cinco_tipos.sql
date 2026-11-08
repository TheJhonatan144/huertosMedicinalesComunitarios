-- Sustituye la agrupación amplia por los cinco tipos de cerca definidos por el proyecto.
ALTER TABLE huerto_cerramiento DROP CONSTRAINT IF EXISTS huerto_cerramiento_grupo_check;

-- Cuando existe el tipo detallado anterior, úsalo como fuente para separar cada grupo fielmente.
-- Para registros nuevos del breve modelo de tres grupos, conserva el grupo más cercano sin inventar
-- una mezcla de materiales que el gestor no había especificado.
UPDATE huerto_cerramiento hc
SET grupo = CASE
    WHEN h.cerramiento = 'VIVO' OR left(h.cerramiento, 5) = 'VIVO_' THEN 'VIVA'
    WHEN h.cerramiento = 'NATURAL' OR left(h.cerramiento, 8) = 'NATURAL_' THEN 'NATURAL'
    WHEN h.cerramiento = 'RECICLADO' OR left(h.cerramiento, 10) = 'RECICLADO_' THEN 'RECICLADO'
    WHEN h.cerramiento = 'METALICO' OR left(h.cerramiento, 9) = 'METALICO_' THEN 'METALICO'
    WHEN h.cerramiento = 'CONVENCIONAL' OR left(h.cerramiento, 13) = 'CONVENCIONAL_' THEN 'CONVENCIONAL'
    WHEN hc.grupo = 'REUTILIZADO' THEN 'RECICLADO'
    WHEN hc.grupo = 'CONSTRUIDO' THEN 'CONVENCIONAL'
    ELSE 'NATURAL'
END
FROM huerto h
WHERE h.id = hc.huerto_id;

ALTER TABLE huerto_cerramiento
    ADD CONSTRAINT huerto_cerramiento_grupo_check
    CHECK (grupo IN ('VIVA', 'NATURAL', 'RECICLADO', 'METALICO', 'CONVENCIONAL'));
