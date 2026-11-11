-- Carga manual del bloque 02. Ejecutar en una transacción, tras respaldo.
-- Solo completa fichas botánicas totalmente vacías; preserva sus identificadores.
-- Las dos equivalencias sin autoría están enumeradas expresamente en el JSON.
DO $carga_catalogo$
DECLARE
 ficha RECORD;
 actual especie%ROWTYPE;
 coincidencias INTEGER;
 especie_id_nueva BIGINT;
 nuevas INTEGER := 0;
 completadas INTEGER := 0;
 omitidas INTEGER := 0;
BEGIN
 LOCK TABLE especie IN SHARE ROW EXCLUSIVE MODE;
 FOR ficha IN
 SELECT * FROM jsonb_to_recordset($fichas_catalogo$
[
  {
    "nombre_comun": "Guayusa",
    "nombre_cientifico": "Ilex guayusa Loes.",
    "otros_nombres": "Wais (MSP).",
    "familia_botanica": "Aquifoliaceae",
    "regiones": [
      "AMAZONIA"
    ],
    "provincias": "Napo, Orellana, Sucumbíos, Pastaza, Morona Santiago y Zamora Chinchipe en etiquetas QCNE seleccionadas. Lista parcial; no todas acreditan cultivo.",
    "clima": "Árbol asociado por Kew al trópico húmedo; las etiquetas incluyen chacras y bosque. No es una recomendación universal de cultivo.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "Puntos comprobados: Orellana 200 m (752), Sucumbíos 350 m (425), Napo 1000 m (67242), Pastaza 235 m (15471), Zamora Chinchipe 900 m (164788). No forman un rango nacional. El intervalo aportado 200-2000 m no se confirma aquí.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Categoría de trabajo inferida del hábito arbóreo duradero. No se documenta un plazo de primera cosecha.",
    "cualidad": null,
    "contexto_cualidad": "Sin clasificación térmica comprobada en las fuentes revisadas. Su nombre y su uso cultural no determinan caliente o fría.",
    "usos_medicinales": "El MSP registra con hojas prácticas tradicionales relacionadas con espanto y dolores corporales (p. 64). Se conserva el uso atribuido, sin receta ni instrucciones.",
    "saberes": "Wais figura en el repertorio amazónico compilado por el MSP. El registro QCNE 67242 sitúa la planta en chacras de Rucullacta; no se atribuye un testimonio único a todas las nacionalidades.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKGU: Royal Botanic Gardens, Kew. Plants of the World Online. Ilex guayusa Loes. https://powo.science.kew.org/taxon/127854-2\nHGU: INABIO QCNE. Registros de Ilex guayusa. https://bndb.sisbioecuador.bio/bndb/collections/list.php?page=8&taxa=9031&taxontype=2&usethes=1",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Hierbabuena",
    "nombre_cientifico": "Mentha spicata L.",
    "otros_nombres": "Hierba buena; mantener separada de menta.",
    "familia_botanica": "Lamiaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay: localidades del cantón Cuenca en la guía UPS. Loja: ejemplares LOJA de Chuquiribamba y San Lucas. Lista parcial; Imbabura sigue sin verificación específica en esta revisión.",
    "clima": "Kew: bioma templado; planta perenne. Las localidades ecuatorianas se documentan por separado.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "UPS p. 99 ofrece 0-3000 m sin aclarar si es intervalo nacional o de cultivo. LOJA registra 2400 m (31900, 31901) y 2600 m (31744). Se conservan en observaciones, sin establecer límites nacionales.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Se infiere largo por duración perenne, confirmada por Kew. Tiempo hasta cosecha no documentado aquí.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 73: clasificación del nombre agrupado «hierba buena y menta», procedente de Cachiguango (2012); no identificación de un ejemplar concreto.",
    "usos_medicinales": "La guía UPS recoge usos atribuidos para molestias digestivas y dentales (p. 99), sin trasladar preparaciones a esta ficha.",
    "saberes": "El MSP p. 66 registra una función de protección de plantas vecinas. Se presenta como saber de huerto y no como uso medicinal.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKHI: Royal Botanic Gardens, Kew. Plants of the World Online. Mentha spicata L. https://powo.science.kew.org/taxon/451162-1\nUPS: Cazorla Orellana, Pedro Adrián. Elaboración de una guía etnobotánica y fitoquímica de plantas medicinales de las parroquias Nulti, Paccha, Quingeo, Santa Ana, Tarqui, Turi, El Valle del cantón Cuenca Ecuador. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf\nHHI: UNL Herbario Reinaldo Espinosa LOJA. Mentha spicata. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=38&page=6&taxa=Lamiaceae&taxontype=3",
    "foto_fuente": null,
    "nombres_previos": [
      "Mentha spicata"
    ]
  },
  {
    "nombre_comun": "Jengibre",
    "nombre_cientifico": "Zingiber officinale Roscoe",
    "otros_nombres": "Ajirinri, nombre equiparado con jengibre en el glosario del MSP, p. 78. No se asigna una lengua específica sin fuente.",
    "familia_botanica": "Zingiberaceae",
    "regiones": [
      "COSTA"
    ],
    "provincias": "Orellana, Napo y Morona Santiago según etiquetas QCNE; Zamora Chinchipe según LOJA. Lista parcial; Santo Domingo y Esmeraldas no se confirman con los registros seleccionados.",
    "clima": "Kew lo sitúa en bioma tropical estacionalmente seco; las etiquetas ecuatorianas incluyen contextos húmedos y de chacra. Distinguir bioma general y condiciones locales.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "QCNE 233466: 300 m; 52968: 450 m. LOJA 20098: 819 m; 7286: 1020 m. Puntos de colecta, no rango. No se confirma el intervalo nacional 150-1500 m ni se convierte en límite de cultivo.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Inferido del hábito perenne y rizomatoso de Kew. La cosecha de 8-10 meses aportada no se verifica con una referencia agronómica concreta; queda fuera de la ficha confirmada.",
    "cualidad": null,
    "contexto_cualidad": "Sin clasificación caliente/fría comprobada. Ajirinri es un nombre, no una categoría térmica.",
    "usos_medicinales": "El MSP p. 72 lo incluye entre sus categorías antiinflamatorias, descongestionantes y tónicas. La mención externa de p. 63 se conserva sin procedimiento de aplicación.",
    "saberes": "Región del ejemplo MSP: Costa. Esto no excluye registros de la Amazonía. Se corrigen la familia y la descripción botánica: Zingiberaceae y rizoma.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKJE: Royal Botanic Gardens, Kew. Plants of the World Online. Zingiber officinale Roscoe https://powo.science.kew.org/taxon/798372-1\nHJE: INABIO QCNE y UNL LOJA. Zingiber officinale. https://bndb.sisbioecuador.bio/bndb/collections/list.php?taxa=19306&usethes=1",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Llantén mayor",
    "nombre_cientifico": "Plantago major L.",
    "otros_nombres": "Llantén; el calificativo mayor distingue esta ficha de Plantago lanceolata.",
    "familia_botanica": "Plantaginaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay: guía UPS para Cuenca. Loja: registros LOJA 25399 y 25448. Lista parcial de localidades documentadas.",
    "clima": "Kew: bioma templado; puede ser anual o perenne. Contexto ecuatoriano documentado en las localidades citadas.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "UPS p. 105 ofrece 0-3500 m, de alcance no precisado. LOJA: Chuquiribamba 2750 m y Loja 2135 m. No se combinan estos datos con los de Plantago lanceolata.",
    "ciclo": null,
    "observaciones_ciclo": "Kew admite hábito anual o perenne; la guía UPS lo describe perenne. Se deja ciclo sin documentar como opción única hasta precisar el criterio del material a cultivar.",
    "cualidad": "FRESCA",
    "contexto_cualidad": "MSP p. 73 asigna fresca al nombre llantén, sin especie científica en ese cuadro. La equivalencia con este taxón es editorial, no una identificación comunitaria comprobada.",
    "usos_medicinales": "UPS p. 105 recoge usos atribuidos relacionados con heridas y molestias renales. El MSP p. 72 clasifica el nombre llantén como astringente, diurético y laxante, sin separar especies.",
    "saberes": "La ficha distingue expresamente el nombre común de la identificación botánica. La guía UPS también presenta inconsistencias entre tablas; se utiliza su ficha de Plantago major, no la fila que llama llantén a Peperomia.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKLM: Royal Botanic Gardens, Kew. Plants of the World Online. Plantago major L. https://powo.science.kew.org/taxon/321286-2\nUPS: Cazorla Orellana, Pedro Adrián. Elaboración de una guía etnobotánica y fitoquímica de plantas medicinales de las parroquias Nulti, Paccha, Quingeo, Santa Ana, Tarqui, Turi, El Valle del cantón Cuenca Ecuador. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf\nHLM: UNL LOJA. Plantago major. https://bndb.sisbioecuador.bio/bndb/collections/list.php?country=Ecuador&db=38&page=249",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Llantén de hoja estrecha",
    "nombre_cientifico": "Plantago lanceolata L.",
    "otros_nombres": "Llantén; calificativo descriptivo para distinguirlo de Plantago major.",
    "familia_botanica": "Plantaginaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay, Pichincha, Chimborazo y Cotopaxi en registros QCNE seleccionados. Lista parcial.",
    "clima": "Kew lo asocia al bioma templado y lo describe perenne. Los ejemplares ecuatorianos se citan aparte.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "QCNE: Cochapata 2720 m (169573); Lloa 2800 m (26852); Chimborazo 3400 m (214798); Cotopaxi 3127 m (239857). Registros puntuales, no intervalo nacional 1000-3500 m.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Categoría de trabajo inferida de la duración perenne documentada por Kew. No se establece plazo de primera cosecha.",
    "cualidad": "FRESCA",
    "contexto_cualidad": "Clasificación MSP p. 73 para el nombre común llantén. El cuadro no diferencia especies; conservar esa limitación al asociarla editorialmente con esta ficha.",
    "usos_medicinales": "La monografía EMA, sección 4.1, recoge uso tradicional europeo de las hojas relacionado con irritación de boca o faringe y tos seca. No se presenta como testimonio ecuatoriano ni como instrucción de tratamiento.",
    "saberes": "La referencia UTA de la compilación menciona esta especie, pero su PDF no pudo consultarse directamente. Sus usos para cólicos no se trasladan como verificados. El contexto europeo de EMA queda explícito.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKLL: Royal Botanic Gardens, Kew. Plants of the World Online. Plantago lanceolata L. https://powo.science.kew.org/taxon/321285-2\nHLL: INABIO QCNE. Plantago lanceolata. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&reset=1&taxa=Plantago&taxontype=2\nEMA: European Medicines Agency. European Union herbal monograph on Plantago lanceolata L., folium. Revision 1. EMA/HMPC/887979/2022. https://www.ema.europa.eu/en/documents/herbal-monograph/final-european-union-herbal-monograph-plantaginis-lanceolatae-folium-revision-1_en.pdf",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Manzanilla",
    "nombre_cientifico": "Matricaria chamomilla L.",
    "otros_nombres": "Camomila; Matricaria recutita L. es sinónimo científico, no una segunda ficha.",
    "familia_botanica": "Asteraceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay: localidades de la guía UPS para Cuenca. Loja: ejemplar LOJA6080, Taquil. No se certifican todas las provincias aportadas.",
    "clima": "Kew: hierba anual del bioma templado. No se convierte esa referencia general en una recomendación de siembra para todas las provincias.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "UPS p. 107 ofrece 1700-3600 m, sin alcance nacional demostrado. LOJA6080: Taquil, 2300 m. El intervalo recibido 2000-3200 m no se adopta como límite; se conservan las evidencias diferenciadas.",
    "ciclo": "CORTO",
    "observaciones_ciclo": "Duración anual según Kew. MSP p. 48 la menciona entre plantas que pueden alcanzar cosecha en 6-8 semanas; ese ejemplo no expresa la vida total ni una garantía de cosecha local.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 74: clasificación tradicional del nombre manzanilla según Cachiguango (2012).",
    "usos_medicinales": "El MSP p. 72 la incluye entre plantas antiespasmódicas y sedantes. Se registra la atribución documental sin afirmar eficacia ni incluir preparaciones.",
    "saberes": "El MSP p. 66 recoge la relación de manzanilla con abejas y plantas cercanas como saber del huerto. La identificación adoptada no incluye automáticamente otras plantas llamadas manzanilla.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKMA: Royal Botanic Gardens, Kew. Plants of the World Online. Matricaria chamomilla L. https://powo.science.kew.org/taxon/154715-2\nUPS: Cazorla Orellana, Pedro Adrián. Elaboración de una guía etnobotánica y fitoquímica de plantas medicinales de las parroquias Nulti, Paccha, Quingeo, Santa Ana, Tarqui, Turi, El Valle del cantón Cuenca Ecuador. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf\nHMA: UNL LOJA. Matricaria chamomilla. https://bndb.sisbioecuador.bio/bndb/collections/list.php?country=Ecuador&county=Taquil&db=38&page=1&state=Loja",
    "foto_fuente": null,
    "nombres_previos": [
      "Matricaria chamomilla"
    ]
  },
  {
    "nombre_comun": "Marco",
    "nombre_cientifico": "Ambrosia arborescens Mill.",
    "otros_nombres": "Altamisa: nombre regional compartido; no equivalencia universal entre todas las plantas así llamadas.",
    "familia_botanica": "Asteraceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Imbabura, Pichincha, Azuay y Cotopaxi en los ejemplares QCNE seleccionados. Lista parcial.",
    "clima": "Kew indica bioma subtropical. Las etiquetas elegidas incluyen ambientes andinos secos y húmedos; no se asigna un único clima ecuatoriano por provincia.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "QCNE: Peguche 2600 m (143401), Lloa 2800 m (30142), Cutchil 2680 m (77841), Cusubamba 3200 m (61258). No son límites nacionales; superan el máximo de 3000 m aportado.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "MSP p. 48 incluye marco entre ejemplos de cosecha larga. UTN p. 7 describe hábito subarbustivo; largo se usa como categoría de trabajo para ese hábito duradero.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 74: marco aparece entre las plantas calientes; conservar la atribución cultural.",
    "usos_medicinales": "La revisión bibliográfica de Gómez Benalcázar (UTN, 2017, p. 7) recoge antecedentes de usos para molestias reumáticas y fiebre, citando literatura anterior. Son antecedentes secundarios, no validación clínica ni entrevistas originales de esa tesis.",
    "saberes": "El MSP p. 66 recoge protección del hogar y energías pesadas como creencias atribuidas. Se conservan como contexto cultural, no como hechos médicos. Los usos de abono o insecticida no se registran como medicinales.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKMR: Royal Botanic Gardens, Kew. Plants of the World Online. Ambrosia arborescens Mill. https://powo.science.kew.org/taxon/176055-1\nUTN: Gómez Benalcázar, Evelyn Valeria. Sensibilidad microbiana y poder insecticida de los aceites esenciales de Clinopodium nubigenum y Ambrosia arborescens. UTN, 2017. https://repositorio.utn.edu.ec/bitstream/123456789/8457/1/03%20BIOT%20006%20TRABAJO%20DE%20GRADO.pdf\nHMR: INABIO QCNE. Ambrosia arborescens. https://bndb.sisbioecuador.bio/bndb/collections/list.php?page=1&taxa=787&taxontype=2&usethes=1",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Matico",
    "nombre_cientifico": "Piper aduncum L.",
    "otros_nombres": "Matico de monte (Ikiam).",
    "familia_botanica": "Piperaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Napo: estudio de siete comunidades de Archidona y Tena, con material recolectado en Muyuna. No se verifica aquí la lista amplia de Pastaza, Esmeraldas y Pichincha.",
    "clima": "Kew: arbusto o árbol del trópico húmedo. El ejemplo de huerto de la Sierra del MSP se distingue de la evidencia amazónica de Napo.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "Ikiam p. 4 describe localidades de entrevistas entre 359 y 798 m. Es la altitud del área de estudio, no un rango de distribución de Piper aduncum ni la altitud de cada ejemplar.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Inferido del hábito arbustivo o arbóreo duradero descrito por Kew. Se retira semiperenne sin criterio documental; primera cosecha no documentada.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 74 clasifica el nombre matico como caliente. Su cuadro no vincula cada nombre a un taxón; no se atribuye esa clasificación como resultado de las entrevistas de Ikiam.",
    "usos_medicinales": "Monar Lucero (Ikiam, 2023) registra uso tradicional respiratorio, principalmente antibronquítico, en siete comunidades kichwas del Bajo Napo. Los experimentos de la tesis no se presentan como prueba de eficacia clínica.",
    "saberes": "La identificación Piper aduncum y el nombre matico de monte se mantienen vinculados al estudio de Napo. El nombre común puede corresponder a otras especies en otros lugares.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKMT: Royal Botanic Gardens, Kew. Plants of the World Online. Piper aduncum L. https://powo.science.kew.org/taxon/680296-1\nIKIAM: Monar Lucero, Madelyn Andrea. Etnofarmacología de Piper aduncum L. Usos medicinales y actividad biológica. Ikiam, 2023. https://repositorio.ikiam.edu.ec/jspui/handle/RD_IKIAM/903",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Menta",
    "nombre_cientifico": "Mentha × piperita L.",
    "otros_nombres": "Hierba buena negra en la tabla de UPS; no confundir con hierbabuena de esta propuesta.",
    "familia_botanica": "Lamiaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Loja: registro LOJA 6348, Vilcabamba. Azuay: referencia a Mentha piperita en el estudio UPS de Cuenca. Lista parcial; no acredita cultivo en toda la región interandina.",
    "clima": "Kew: bioma templado y hábito perenne. La localidad y condiciones de cada cultivo requieren evaluación propia.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "LOJA 6348 no tiene altitud. UPS p. 47 resume 0-3000 m sin precisar alcance ni justificar aquí límites nacionales. El intervalo aportado 1500-3000 m no se confirma como rango ecuatoriano.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo por duración perenne de Kew. MSP p. 48 usa menta como ejemplo de cosecha corta, 6-8 semanas. Se conserva la diferencia de criterios; largo no representa espera hasta la primera cosecha.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 73 agrupa hierba buena y menta como calientes. Atribución al nombre vernáculo y al contexto andino de la fuente.",
    "usos_medicinales": "MSP p. 73 incluye el nombre menta entre plantas vermífugas. La tabla UPS p. 47 vincula Mentha piperita con usos digestivos y cólicos. No se añade el uso para estrés de la referencia UPS 2026 no verificada.",
    "saberes": "Kew confirma el híbrido Mentha aquatica × Mentha spicata. El nombre científico lleva signo ×. Las funciones de protección del huerto del MSP p. 66 se distinguen de los usos medicinales.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKME: Royal Botanic Gardens, Kew. Plants of the World Online. Mentha × piperita L. https://powo.science.kew.org/taxon/450969-1\nUPS: Cazorla Orellana, Pedro Adrián. Elaboración de una guía etnobotánica y fitoquímica de plantas medicinales de las parroquias Nulti, Paccha, Quingeo, Santa Ana, Tarqui, Turi, El Valle del cantón Cuenca Ecuador. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf\nHME: UNL LOJA. Registro publicado como Mentha piperita L. https://bndb.sisbioecuador.bio/bndb/collections/list.php?country=Ecuador&county=Vilcabamba&db=38&reset=1&state=Loja",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Orégano",
    "nombre_cientifico": "Origanum vulgare L.",
    "otros_nombres": null,
    "familia_botanica": "Lamiaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay: estudio UPS de Cuenca y ejemplar LOJA 51394 del Jardín Botánico de Cuenca. No se certifican Pichincha ni Tungurahua con las referencias consultadas.",
    "clima": "Kew: bioma templado y planta perenne. La guía UPS la registra como introducida en el contexto de Cuenca.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "LOJA 51394: 2539 m, jardín botánico. UPS p. 111 ofrece 1500-2800 m y su tabla p. 47 indica 50-3400 m. La discrepancia no se resuelve eligiendo un intervalo; mínimo y máximo quedan nulos.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Categoría de trabajo por hábito perenne documentado por Kew; no hay plazo de cosecha local verificado en esta revisión.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 74 sitúa orégano entre las plantas calientes, según Cachiguango (2012).",
    "usos_medicinales": "La guía UPS p. 111 recoge usos digestivos tradicionales. MSP p. 72 lo incluye en su categoría de plantas antioxidantes. Se conservan como atribuciones documentales y no como beneficios clínicos demostrados.",
    "saberes": "Origanum vulgare se adopta como identificación editorial respaldada por Kew y por la guía UPS. Su condición introducida no impide considerarla en el catálogo, ni demuestra adaptación a cualquier localidad.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKOR: Royal Botanic Gardens, Kew. Plants of the World Online. Origanum vulgare L. https://powo.science.kew.org/taxon/453395-1\nUPS: Cazorla Orellana, Pedro Adrián. Elaboración de una guía etnobotánica y fitoquímica de plantas medicinales de las parroquias Nulti, Paccha, Quingeo, Santa Ana, Tarqui, Turi, El Valle del cantón Cuenca Ecuador. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf\nHHI: UNL Herbario Reinaldo Espinosa LOJA. Mentha spicata. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=38&page=6&taxa=Lamiaceae&taxontype=3",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Romero",
    "nombre_cientifico": "Salvia rosmarinus Spenn.",
    "otros_nombres": "Rosmarinus officinalis L. (sinónimo científico). Otros nombres locales sin documentar.",
    "familia_botanica": "Lamiaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay: presencia en el conjunto de plantas del estudio UPS de Cuenca. No se verifica aquí toda la lista aportada de Imbabura, Pichincha, Loja y Tungurahua.",
    "clima": "Kew: arbusto perenne del bioma templado, originario del Mediterráneo. Es una referencia botánica general, no validación de tolerancia local a sequía.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "UPS p. 47 presenta 2000-3000 m sin establecer su alcance como rango ecuatoriano. No se adopta el intervalo aportado 1500-3000 m. Se requiere fuente agronómica local o registros de colecta para límites de cultivo.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Kew describe arbusto perenne. MSP p. 48 lo incluye como ejemplo de plantas de cosecha larga; no fija meses ni años para Ecuador.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 74 clasifica el nombre romero como caliente según Cachiguango (2012).",
    "usos_medicinales": "MSP p. 72 registra romero entre sus categorías astringentes y tónicas. Se conserva esa atribución sin recomendaciones de uso ni preparación.",
    "saberes": "Los sinónimos científicos permiten reunir literatura histórica sin crear duplicados. La expresión general fitoterapia popular local no se usa para inventar un testimonio comunitario.",
    "fuente_documental": "MSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF del usuario; véase paginación en la matriz de fuentes.\nKRO: Royal Botanic Gardens, Kew. Plants of the World Online. Salvia rosmarinus Spenn. https://powo.science.kew.org/taxon/457138-1\nUPS: Cazorla Orellana, Pedro Adrián. Elaboración de una guía etnobotánica y fitoquímica de plantas medicinales de las parroquias Nulti, Paccha, Quingeo, Santa Ana, Tarqui, Turi, El Valle del cantón Cuenca Ecuador. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf",
    "foto_fuente": null,
    "nombres_previos": []
  }
]
$fichas_catalogo$::jsonb) AS datos(
 nombre_comun TEXT, nombre_cientifico TEXT, otros_nombres TEXT, familia_botanica TEXT,
 regiones TEXT[], provincias TEXT, clima TEXT, altitud_minima INTEGER, altitud_maxima INTEGER,
 observaciones_altitud TEXT, ciclo TEXT, observaciones_ciclo TEXT, cualidad TEXT,
 contexto_cualidad TEXT, usos_medicinales TEXT, saberes TEXT, fuente_documental TEXT,
 foto_fuente TEXT, nombres_previos TEXT[])
 LOOP
  SELECT COUNT(*) INTO coincidencias FROM especie e
   WHERE lower(btrim(e.nombre_cientifico)) = lower(btrim(ficha.nombre_cientifico))
    OR lower(btrim(e.nombre_cientifico)) IN (SELECT lower(btrim(n)) FROM unnest(ficha.nombres_previos) n);
  IF coincidencias > 1 THEN
   IF current_setting('huertos.carga_catalogo_automatica', true) = 'on' THEN
    RAISE WARNING 'Identificación repetida o equivalencia ambigua: %.', ficha.nombre_cientifico;
    CONTINUE;
   END IF;
   RAISE EXCEPTION 'Identificación repetida o equivalencia ambigua: %.', ficha.nombre_cientifico;
  ELSIF coincidencias = 1 THEN
   SELECT e.* INTO actual FROM especie e
    WHERE lower(btrim(e.nombre_cientifico)) = lower(btrim(ficha.nombre_cientifico))
     OR lower(btrim(e.nombre_cientifico)) IN (SELECT lower(btrim(n)) FROM unnest(ficha.nombres_previos) n);
   IF NULLIF(btrim(actual.fuente_documental),'') IS NOT NULL THEN
    omitidas := omitidas + 1;
    RAISE NOTICE 'Se conserva la ficha documentada existente: %.', actual.nombre_cientifico;
    CONTINUE;
   END IF;
   IF jsonb_strip_nulls(to_jsonb(actual) - ARRAY['id','version','nombre_comun','nombre_cientifico']) <> '{}'::jsonb
      OR EXISTS(SELECT 1 FROM especie_region WHERE especie_id = actual.id) THEN
    IF current_setting('huertos.carga_catalogo_automatica', true) = 'on' THEN
     RAISE WARNING 'La ficha % contiene datos previos sin fuente; revisar manualmente antes de completar.', actual.nombre_cientifico;
     CONTINUE;
    END IF;
    RAISE EXCEPTION 'La ficha % contiene datos previos sin fuente; revisar manualmente antes de completar.', actual.nombre_cientifico;
   END IF;
   UPDATE especie SET
    nombre_comun=ficha.nombre_comun, nombre_cientifico=ficha.nombre_cientifico,
    otros_nombres=ficha.otros_nombres, familia_botanica=ficha.familia_botanica,
    provincias=ficha.provincias, clima=ficha.clima,
    altitud_minima=ficha.altitud_minima, altitud_maxima=ficha.altitud_maxima,
    observaciones_altitud=ficha.observaciones_altitud, ciclo=ficha.ciclo,
    observaciones_ciclo=ficha.observaciones_ciclo, cualidad=ficha.cualidad,
    contexto_cualidad=ficha.contexto_cualidad, usos_medicinales=ficha.usos_medicinales,
    saberes=ficha.saberes, fuente_documental=ficha.fuente_documental,
    version=version+1
   WHERE id=actual.id;
   especie_id_nueva := actual.id;
   completadas := completadas + 1;
  ELSE
   IF EXISTS (SELECT 1 FROM especie e
     WHERE lower(btrim(e.nombre_comun)) = lower(btrim(ficha.nombre_comun))
       AND NULLIF(btrim(e.nombre_cientifico),'') IS NULL) THEN
    IF current_setting('huertos.carga_catalogo_automatica', true) = 'on' THEN
     RAISE WARNING 'Ya existe % sin identificación científica; revisar antes de cargar.', ficha.nombre_comun;
     CONTINUE;
    END IF;
    RAISE EXCEPTION 'Ya existe % sin identificación científica; revisar antes de cargar.', ficha.nombre_comun;
   END IF;
   INSERT INTO especie(nombre_comun,nombre_cientifico,otros_nombres,familia_botanica,
    provincias,clima,altitud_minima,altitud_maxima,observaciones_altitud,ciclo,
    observaciones_ciclo,cualidad,contexto_cualidad,usos_medicinales,saberes,fuente_documental)
   VALUES(ficha.nombre_comun,ficha.nombre_cientifico,ficha.otros_nombres,ficha.familia_botanica,
    ficha.provincias,ficha.clima,ficha.altitud_minima,ficha.altitud_maxima,ficha.observaciones_altitud,ficha.ciclo,
    ficha.observaciones_ciclo,ficha.cualidad,ficha.contexto_cualidad,ficha.usos_medicinales,ficha.saberes,ficha.fuente_documental)
   RETURNING id INTO especie_id_nueva;
   nuevas := nuevas + 1;
  END IF;
  INSERT INTO especie_region(especie_id,region)
   SELECT especie_id_nueva,unnest(ficha.regiones);
 END LOOP;
 RAISE NOTICE 'Bloque 02: % nuevas, % fichas vacías completadas, % documentadas conservadas. Siembras intactas.', nuevas,completadas,omitidas;
END;
$carga_catalogo$;
