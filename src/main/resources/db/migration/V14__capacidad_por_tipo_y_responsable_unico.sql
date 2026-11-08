-- Una persona tiene capacidad 1, familia 10 y comunidades de barrio/condominio hasta el máximo global.
CREATE OR REPLACE FUNCTION validar_cupo_comunidad() RETURNS trigger AS $$
DECLARE
    activos INTEGER;
    capacidad INTEGER;
BEGIN
    IF NEW.activo THEN
        PERFORM 1 FROM comunidad WHERE id = NEW.comunidad_id FOR UPDATE;
        SELECT CASE tipo_comunidad
                 WHEN 'PERSONA' THEN 1
                 WHEN 'FAMILIA' THEN LEAST(10, 25)
                 ELSE 25
               END
          INTO capacidad FROM comunidad WHERE id = NEW.comunidad_id;
        SELECT count(*) INTO activos
          FROM integrante
         WHERE comunidad_id = NEW.comunidad_id
           AND activo
           AND id IS DISTINCT FROM NEW.id;
        IF activos >= capacidad THEN
            RAISE EXCEPTION 'cupo_comunidad_excedido: la comunidad % alcanzó su capacidad de % integrantes', NEW.comunidad_id, capacidad
                USING ERRCODE = '23514';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Conserva un único responsable por comunidad: prioriza administrador MSP activo y, luego,
-- la asignación más antigua. La relación y su historial siguen usando la tabla existente.
WITH ordenadas AS (
    SELECT a.id, row_number() OVER (
        PARTITION BY a.comunidad_id
        ORDER BY (u.rol = 'ADMIN_MINISTERIO_SALUD' AND u.activo) DESC, a.asignada_en, a.id
    ) AS posicion
    FROM asignacion_revisor a JOIN usuario u ON u.id = a.usuario_id
)
DELETE FROM asignacion_revisor a USING ordenadas o WHERE a.id = o.id AND o.posicion > 1;

CREATE UNIQUE INDEX uq_asignacion_revisor_comunidad ON asignacion_revisor(comunidad_id);

-- Distribuye comunidades existentes que aún no tengan responsable entre administradores activos.
DO $$
DECLARE
    comunidad_pendiente BIGINT;
    responsable BIGINT;
BEGIN
    FOR comunidad_pendiente IN
        SELECT c.id FROM comunidad c
        WHERE NOT EXISTS (SELECT 1 FROM asignacion_revisor a WHERE a.comunidad_id = c.id)
        ORDER BY c.id
    LOOP
        SELECT u.id INTO responsable
        FROM usuario u
        LEFT JOIN asignacion_revisor a ON a.usuario_id = u.id
        WHERE u.rol = 'ADMIN_MINISTERIO_SALUD' AND u.activo
        GROUP BY u.id
        ORDER BY count(a.id), u.id
        LIMIT 1;
        IF responsable IS NOT NULL THEN
            INSERT INTO asignacion_revisor(usuario_id, comunidad_id)
            VALUES (responsable, comunidad_pendiente);
        END IF;
        responsable := NULL;
    END LOOP;
END;
$$;
