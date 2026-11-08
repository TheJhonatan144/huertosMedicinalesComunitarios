-- Ejecutar con psql, desde cualquier directorio. Solo simula; finaliza con ROLLBACK.
\set ON_ERROR_STOP on
BEGIN;
\ir carga-bloque-01.sql

SELECT nombre_comun, nombre_cientifico, ciclo, cualidad,
       altitud_minima, altitud_maxima, observaciones_altitud, contexto_cualidad
FROM especie
WHERE nombre_cientifico IN (
 'Bixa orellana L.', 'Persea americana Mill.', 'Mansoa alliacea (Lam.) A.H.Gentry',
 'Ocimum basilicum L.', 'Gossypium barbadense L.', 'Coffea arabica L.',
 'Mespilodaphne quixos (Lam.) Rohwer', 'Ceiba trischistandra (A.Gray) Bakh.',
 'Baccharis latifolia (Ruiz & Pav.) Pers.', 'Iresine diffusa Humb. & Bonpl. ex Willd.'
)
ORDER BY nombre_comun;

ROLLBACK;
