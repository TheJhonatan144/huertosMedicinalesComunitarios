-- Mantiene el límite de integrantes activos (incluido el gestor) alineado con el tipo de comunidad.
CREATE OR REPLACE FUNCTION validar_cupo_comunidad() RETURNS trigger AS $$
DECLARE
    activos INTEGER;
    capacidad INTEGER;
BEGIN
    IF NEW.activo THEN
        PERFORM 1 FROM comunidad WHERE id = NEW.comunidad_id FOR UPDATE;
        SELECT CASE tipo_comunidad
                 WHEN 'PERSONA' THEN 1
                 WHEN 'FAMILIA' THEN 10
                 WHEN 'CONDOMINIO' THEN 20
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
