-- Captura el tipo y la dirección exacta de la comunidad sin cambiar sus relaciones.
ALTER TABLE comunidad
    ADD COLUMN tipo_comunidad VARCHAR(20) NOT NULL DEFAULT 'BARRIO'
        CHECK (tipo_comunidad IN ('PERSONA', 'FAMILIA', 'CONDOMINIO', 'BARRIO')),
    ADD COLUMN direccion_exacta VARCHAR(250) NOT NULL DEFAULT '';

-- Las cuentas nuevas y las invitaciones usan nombres de usuario sin distinguir mayúsculas.
CREATE UNIQUE INDEX uq_usuario_nombre_usuario_ci ON usuario (lower(nombre_usuario));

-- Se amplían los códigos del diseño manteniendo las opciones anteriores como equivalencias.
ALTER TABLE huerto ALTER COLUMN diseno TYPE VARCHAR(40);
ALTER TABLE huerto DROP CONSTRAINT IF EXISTS huerto_diseno_check;
UPDATE huerto SET diseno = CASE diseno
    WHEN 'VERTICAL' THEN 'VERTICAL_PAREDES'
    WHEN 'COLGANTE' THEN 'COLGANTE_TECHOS'
    WHEN 'PISO' THEN 'PISO_MACETAS'
    ELSE diseno END;
ALTER TABLE huerto ADD CONSTRAINT huerto_diseno_check CHECK (diseno IN (
    'TIERRA_SURCOS', 'TIERRA_CAMADAS', 'TIERRA_CAMELLONES', 'TIERRA_TERRAZAS',
    'VERTICAL_PAREDES', 'COLGANTE_TECHOS', 'PISO_MACETAS', 'PISO_BOTELLAS_PATIO',
    'PISO_LLANTAS_PATIO', 'PISO_TERRAZA', 'PISO_GRADAS', 'PISO_BALCONES', 'PISO_FILOS_VENTANA'
));

-- Se conservan los códigos generales de registros previos; los formularios nuevos ofrecen
-- únicamente las opciones detalladas. Los valores nuevos quedan restringidos por este CHECK.
ALTER TABLE huerto ALTER COLUMN cerramiento TYPE VARCHAR(40);
ALTER TABLE huerto DROP CONSTRAINT IF EXISTS huerto_cerramiento_check;
ALTER TABLE huerto ADD CONSTRAINT huerto_cerramiento_check CHECK (cerramiento IN (
    'VIVO', 'NATURAL', 'RECICLADO', 'METALICO', 'CONVENCIONAL',
    'VIVO_HIGUERILLA', 'VIVO_FLORIPONDIO', 'VIVO_AGAVE', 'VIVO_LAVANDA',
    'VIVO_NARANJOS', 'VIVO_CAPULIES', 'VIVO_TOCTE', 'VIVO_GUABO',
    'NATURAL_CANA_GUADUA', 'NATURAL_BAHAREQUE', 'NATURAL_TAPIAL',
    'RECICLADO_TABLAS', 'RECICLADO_BOTELLAS', 'RECICLADO_NEUMATICOS',
    'METALICO_ALAMBRE_PUAS', 'CONVENCIONAL_PIEDRA', 'CONVENCIONAL_LADRILLO', 'CONVENCIONAL_BLOQUE'
));
