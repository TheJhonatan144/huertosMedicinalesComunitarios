-- Carga manual del bloque 03. Ejecutar en una transacción, tras respaldo.
-- Solo completa fichas botánicas totalmente vacías; preserva sus identificadores.
-- No se reemplazan nombres científicos previos mediante equivalencias automáticas.
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
    "nombre_comun": "Orquídea ecuatoriana",
    "nombre_cientifico": "Epidendrum quitensium Rchb.f.",
    "otros_nombres": "Flor de Cristo; maygua (UPS, tabla 13). Son nombres compartidos por otras orquídeas.",
    "familia_botanica": "Orchidaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay, Napo, Pichincha y Tungurahua según la distribución recopilada en UPS 2020, tabla 13; ejemplar estudiado de Quito, Pichincha.",
    "clima": "Epífita del bioma tropical húmedo según Kew. La altitud, el sustrato y el microclima deben evaluarse para cada huerto.",
    "altitud_minima": 1600,
    "altitud_maxima": 2700,
    "observaciones_altitud": "UPS 2020, tabla 13, reproduce 1600–2700 m como distribución altitudinal, citando Tropicos (2020). No es un intervalo agronómico validado.",
    "ciclo": null,
    "observaciones_ciclo": "No se confirma aquí una duración ni un plazo de cosecha; el hábito epífito no basta para asignar corto/largo.",
    "cualidad": null,
    "contexto_cualidad": "Sin cualidad térmica comprobada para esta especie. No se traslada la cualidad de otras orquídeas.",
    "usos_medicinales": "La tabla 13 de la tesis UPS 2020 recoge un uso tradicional atribuido para afecciones nerviosas. Ese antecedente bibliográfico no demuestra eficacia clínica; el ensayo antioxidante de la tesis tampoco la establece.",
    "saberes": "Especie aceptada por Kew y nativa de Ecuador. El ejemplar estudiado por UPS se recolectó en Quito en 2019. La elección representa una propuesta botánica para el catálogo; no acredita que una comunidad ya la cultive.",
    "fuente_documental": "KO: Royal Botanic Gardens, Kew. Plants of the World Online. Epidendrum quitensium Rchb.f. https://powo.science.kew.org/taxon/632607-1\nORQ: Molina Sandoval, Lisbeth Judith. Evaluación de la actividad antioxidante de los extractos etanólicos de cinco especies de orquídeas de los géneros Maxillaria, Catasetum y Epidendrum, presentes en el Ecuador. UPS, 2020. https://dspace.ups.edu.ec/bitstream/123456789/18989/1/UPS-TTQ075.pdf Páginas: 43, tabla 13 (página 55 del PDF)\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Tomillo",
    "nombre_cientifico": "Thymus vulgaris L.",
    "otros_nombres": "Tomillo común.",
    "familia_botanica": "Lamiaceae",
    "regiones": [
      "COSTA",
      "SIERRA"
    ],
    "provincias": "Azuay (QCNE 225312), Chimborazo (145117), Imbabura (66558), Cotopaxi (61002) y Tungurahua (61269). Lista parcial de colectas; se excluyen puestos de mercado.",
    "clima": "Subarbusto de bioma templado según Kew. Costa figura como ejemplo del MSP; las colectas citadas son andinas y no acreditan adaptación a toda la Costa.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "Colectas QCNE: 2564 m (225312), 2800 m (145117), 2645 m (66558), 2400 m (61002) y 2600 m (61269). Son puntos, no límites nacionales de cultivo.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Categoría de trabajo inferida del hábito subarbustivo duradero; no equivale al tiempo hasta la primera cosecha.",
    "cualidad": null,
    "contexto_cualidad": "Sin clasificación térmica comprobada en los documentos revisados.",
    "usos_medicinales": "El MSP p. 72 incluye al tomillo en las categorías tradicionales antiespasmódica y tónica. Se conserva la atribución documental sin indicación terapéutica.",
    "saberes": "El MSP p. 20 lo menciona como ejemplo para huertos de la Costa; los registros de herbario amplían el contexto geográfico, sin convertirlo en una distribución exclusiva.",
    "fuente_documental": "KT: Royal Botanic Gardens, Kew. Plants of the World Online. Thymus vulgaris L. https://powo.science.kew.org/taxon/461765-1\nHT: INABIO QCNE / UNL LOJA. Ejemplares consultados de Thymus. Filtrar por especie y Ecuador; el listado puede incluir otros taxones o países. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&reset=1&taxa=Thymus&taxontype=2\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Toronjil",
    "nombre_cientifico": "Melissa officinalis L.",
    "otros_nombres": "Melisa.",
    "familia_botanica": "Lamiaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay: localidades del cantón Cuenca documentadas por UPS. Loja: Chuquiribamba (LOJA 31711) y Loja (31899). Lista parcial.",
    "clima": "Hierba perenne del bioma templado según Kew. Las localidades ecuatorianas citadas son andinas.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "UPS p. 129 ofrece 1200–2800 m; su resumen p. 47 ofrece 2500–3000 m. LOJA 31711: 2750 m; 31899: 2200 m. No se fusionan intervalos de alcance incierto.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Inferido de la duración perenne descrita por Kew; no se establece calendario de cosecha.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 74: «toronjil» en la columna caliente, clasificación tradicional recopilada de Cachiguango (2012). Es una atribución al nombre común, no una propiedad biomédica.",
    "usos_medicinales": "La guía UPS p. 129 recoge usos tradicionales calmantes y digestivos. Se registran como antecedentes etnobotánicos, sin preparaciones ni dosis.",
    "saberes": "El MSP p. 66 recoge que atrae abejas en el huerto. Es un saber de manejo, separado de sus usos medicinales atribuidos.",
    "fuente_documental": "KM: Royal Botanic Gardens, Kew. Plants of the World Online. Melissa officinalis L. https://powo.science.kew.org/taxon/450084-1\nHM: INABIO QCNE / UNL LOJA. Ejemplares consultados de Melissa. Filtrar por especie y Ecuador; el listado puede incluir otros taxones o países. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=38&reset=1&taxa=Melissa&taxontype=2\nUPS: Cazorla Orellana, Pedro Adrián. Guía etnobotánica y fitoquímica de plantas medicinales de siete parroquias del cantón Cuenca. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf Páginas: 44, 47, 89, 112, 129, 134\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Taraxaco",
    "nombre_cientifico": "Taraxacum sect. Taraxacum F.H.Wigg.",
    "otros_nombres": "Diente de león; tañig (UPS); Tani (MSP). Taraxacum officinale F.H.Wigg.: nombre histórico tratado como sinónimo de la sección por Kew.",
    "familia_botanica": "Asteraceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay: localidades de Cuenca en UPS. Pichincha: Lloa y Pifo (QCNE 24603, 24833). Cotopaxi: Sigchos, Zumbahua y Aláquez (60586, 60939, 60935). Lista parcial bajo T. officinale.",
    "clima": "Conjunto de hierbas perennes de bioma templado según Kew. No se confirma una microspecie ecuatoriana para todas las localidades.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "UPS p. 89: 2000–4500 m bajo T. officinale, sin alcance nacional aclarado. QCNE: 2800, 2580, 2800, 3600 y 2400 m respectivamente. Los puntos no delimitan aptitud de cultivo.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Inferido de la duración perenne del conjunto descrito por Kew; distinto de un ciclo de recolección.",
    "cualidad": "FRESCA",
    "contexto_cualidad": "MSP p. 74 sitúa «Tani–Diente de león» en la columna fresca. UPS p. 44 relaciona diente de león, taraxaco y tañig. La atribución vernacular no resuelve la microspecie.",
    "usos_medicinales": "El MSP p. 72 incluye al diente de león en categorías diurética y laxante. Registro de uso tradicional atribuido; no prescripción.",
    "saberes": "La equivalencia de nombres populares permite relacionar los documentos, conservando explícitamente la incertidumbre de identificación del conjunto Taraxacum.",
    "fuente_documental": "KD: Royal Botanic Gardens, Kew. Plants of the World Online. Taraxacum sect. Taraxacum F.H.Wigg. https://powo.science.kew.org/taxon/254151-1\nKDS: Royal Botanic Gardens, Kew. Plants of the World Online. Taraxacum officinale F.H.Wigg. (sinónimo en POWO) https://powo.science.kew.org/taxon/1003018-2\nHD: INABIO QCNE / UNL LOJA. Ejemplares consultados de Taraxacum. Filtrar por especie y Ecuador; el listado puede incluir otros taxones o países. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&reset=1&taxa=Taraxacum&taxontype=2\nUPS: Cazorla Orellana, Pedro Adrián. Guía etnobotánica y fitoquímica de plantas medicinales de siete parroquias del cantón Cuenca. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf Páginas: 44, 47, 89, 112, 129, 134\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Ortiga menor",
    "nombre_cientifico": "Urtica urens L.",
    "otros_nombres": "Ortiga; tsini (nombre agrupado del MSP, no exclusivo de esta especie).",
    "familia_botanica": "Urticaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Loja: Acacana (QCNE 237717). Imbabura: Peguche (24338). Azuay: Jadán y San Gerardo (217235, 167906). Lista parcial.",
    "clima": "Hierba anual del bioma templado según Kew.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "QCNE 237717: 2701 m; 24338: 2000–2500 m en una etiqueta; 217235: 2720 m; 167906: 2820 m. UPS p. 112 ofrece 1000–3000 m sin aclarar alcance. No son límites nacionales.",
    "ciclo": "CORTO",
    "observaciones_ciclo": "Inferido de la duración anual confirmada por Kew; no se asigna un plazo de cosecha.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 74 clasifica «Tsini (ortiga)» como caliente. Se conserva como atribución al nombre común; el documento no distingue Urtica urens de todas las demás ortigas.",
    "usos_medicinales": "La guía UPS p. 112 identifica Urtica urens y recoge usos tradicionales para resfríos y nervios. Se conserva la atribución sin reproducir tratamientos.",
    "saberes": "El nombre «ortiga» puede corresponder a Urtica dioica y otros taxones; esta ficha se limita a U. urens y necesita identificación del ejemplar local.",
    "fuente_documental": "KU: Royal Botanic Gardens, Kew. Plants of the World Online. Urtica urens L. https://powo.science.kew.org/taxon/857987-1\nHU: INABIO QCNE / UNL LOJA. Ejemplares consultados de Urtica. Filtrar por especie y Ecuador; el listado puede incluir otros taxones o países. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&reset=1&taxa=Urtica&taxontype=2\nUPS: Cazorla Orellana, Pedro Adrián. Guía etnobotánica y fitoquímica de plantas medicinales de siete parroquias del cantón Cuenca. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf Páginas: 44, 47, 89, 112, 129, 134\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Violeta",
    "nombre_cientifico": "Viola odorata L.",
    "otros_nombres": "Violeta de olor. No confundir con pensamiento (Viola tricolor).",
    "familia_botanica": "Violaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay: localidades del cantón Cuenca documentadas por UPS p. 134. Pichincha: Quito (LOJA 20288). Lista parcial.",
    "clima": "Hierba perenne del bioma templado según Kew.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "UPS p. 134 ofrece 2500–3000 m sin precisar alcance nacional; LOJA 20288 registra 2800 m en Quito. No se interpreta como rango de cultivo comprobado.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Inferido de la duración perenne descrita por Kew; sin calendario de cosecha.",
    "cualidad": "FRESCA",
    "contexto_cualidad": "MSP p. 74 ubica «violeta» en la columna fresca. Clasificación tradicional del nombre popular; la identidad V. odorata se apoya en UPS.",
    "usos_medicinales": "UPS p. 134 recoge usos tradicionales atribuidos para tos y bronquitis. Se conservan como registro etnobotánico y no como recomendación de tratamiento.",
    "saberes": "La foto y el nombre científico corresponden a Viola odorata; no se transfieren propiedades de otras violetas ornamentales.",
    "fuente_documental": "KV: Royal Botanic Gardens, Kew. Plants of the World Online. Viola odorata L. https://powo.science.kew.org/taxon/868828-1\nHV: INABIO QCNE / UNL LOJA. Ejemplares consultados de Viola. Filtrar por especie y Ecuador; el listado puede incluir otros taxones o países. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=38&reset=1&taxa=Viola&taxontype=2\nUPS: Cazorla Orellana, Pedro Adrián. Guía etnobotánica y fitoquímica de plantas medicinales de siete parroquias del cantón Cuenca. UPS, 2022. https://dspace.ups.edu.ec/bitstream/123456789/23717/1/UPS-CT010179.pdf Páginas: 44, 47, 89, 112, 129, 134\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Paico",
    "nombre_cientifico": "Dysphania ambrosioides (L.) Mosyakin & Clemants",
    "otros_nombres": "Paiku (MSP); Chenopodium ambrosioides L. (sinónimo).",
    "familia_botanica": "Amaranthaceae",
    "regiones": [
      "SIERRA",
      "AMAZONIA",
      "COSTA"
    ],
    "provincias": "Loja (QCNE 237743), Bolívar (161797), Pichincha (125633), Chimborazo (181618) y Guayas (247651), bajo Chenopodium ambrosioides. Pastaza: comunidad de Santa Clara en el estudio UEA. Lista parcial.",
    "clima": "Hierba perenne del bioma templado según Kew, con registros ecuatorianos a diferentes alturas. Kew la lista como introducida en Ecuador; no se asume nativa por ser americana.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "QCNE: Acacana 2701 m (237743), Chillanes 2100–2200 m (161797), Tabacundo 1975 m (125633), Guamote 3250 m (181618), Guayas 6–40 m (247651). Puntos y tramos de etiqueta, no rango nacional.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Inferido de la duración perenne descrita por Kew; no se equipara a cosecha ni al manejo anual local.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "MSP p. 74: «Paiku (paico)» en la columna caliente. Atribución cultural al nombre común, con el sinónimo científico resuelto por Kew.",
    "usos_medicinales": "La tabla 2 del estudio UEA 2019 (p. 239), bajo Chenopodium ambrosioides, recoge usos para cólicos y resfríos; la tabla cita a De la Torre et al. (2008). Se registra la atribución sin dosis ni procedimientos.",
    "saberes": "QCNE 237743 lo registra en huertos caseros de Acacana. La sinonimia explica que documentos antiguos lo ubiquen en Chenopodium; no son dos plantas diferentes del catálogo.",
    "fuente_documental": "KP: Royal Botanic Gardens, Kew. Plants of the World Online. Dysphania ambrosioides (L.) Mosyakin & Clemants https://powo.science.kew.org/taxon/1198060-2\nHP: INABIO QCNE / UNL LOJA. Ejemplares consultados de Chenopodium. Filtrar por especie y Ecuador; el listado puede incluir otros taxones o países. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&reset=1&taxa=Chenopodium&taxontype=2\nUEA: Andrade-Yucailla et al. Uso de plantas medicinales en comunidades indígenas asentadas en un bosque siempreverde piemontano del cantón Santa Clara, Amazonía Ecuatoriana. RACYT 8(2), 235–243, 2019. https://doi.org/10.59410/RACYT-v08n02ep14-0123 Páginas: 239, tabla 2; la tabla cita a De la Torre et al. (2008).\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Yuca",
    "nombre_cientifico": "Manihot esculenta Crantz",
    "otros_nombres": "Lumu (MSP). Variedad local no determinada.",
    "familia_botanica": "Euphorbiaceae",
    "regiones": [
      "AMAZONIA"
    ],
    "provincias": "Sucumbíos: Shushufindi y Sinangüe (QCNE 204902, 214247, 214318). Morona Santiago: Bomboiza (50239). Napo: Jatuncocha y Jatun Sacha (1627, 78616), según etiquetas históricas. Lista parcial.",
    "clima": "Arbusto o árbol con órganos subterráneos de reserva, del bioma tropical estacionalmente seco según Kew. También se documenta como cultivo en chacras amazónicas húmedas.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "QCNE 204902: 230 m; 50239: 800 m; 1627: 200 m; 214247 y 214318: 700–800 m. Son localidades de registro; no se determina un rango agronómico ni una variedad de yuca.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Categoría de trabajo por hábito leñoso duradero. No describe el plazo de cosecha de raíces, que depende de la variedad y del manejo y no se confirma aquí.",
    "cualidad": null,
    "contexto_cualidad": "Sin cualidad caliente/fría comprobada para esta especie en las fuentes revisadas.",
    "usos_medicinales": "El MSP p. 63 incluye LUMU (yuca) en su repertorio de usos tradicionales y atribuye usos relacionados con fiebre. Se registra la mención cultural, sin reproducir sus procedimientos ni recomendar consumo.",
    "saberes": "El MSP p. 20 presenta la yuca como planta alimenticia de la chakra familiar amazónica. Se incluye porque se cultiva en ese huerto y aparece en el repertorio documental; su función alimentaria se distingue del uso medicinal atribuido.",
    "fuente_documental": "KY: Royal Botanic Gardens, Kew. Plants of the World Online. Manihot esculenta Crantz https://powo.science.kew.org/taxon/351790-1\nHY: INABIO QCNE / UNL LOJA. Ejemplares consultados de Manihot. Filtrar por especie y Ecuador; el listado puede incluir otros taxones o países. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&reset=1&taxa=Manihot&taxontype=2\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
    "foto_fuente": null,
    "nombres_previos": []
  },
  {
    "nombre_comun": "Uña de gato",
    "nombre_cientifico": "Uncaria tomentosa (Willd. ex Schult.) DC.",
    "otros_nombres": "Nombre común compartido con otras especies; no identifica por sí solo Uncaria tomentosa.",
    "familia_botanica": "Rubiaceae",
    "regiones": [
      "AMAZONIA"
    ],
    "provincias": "Sucumbíos: Shushufindi y Cuyabeno (QCNE 109343–109345, 179798). Zamora Chinchipe: Yawi, Nangaritza (194866). Orellana: El Edén (205257). Pastaza: uso recogido en estudio UEA de Santa Clara. Lista parcial.",
    "clima": "Liana del bioma tropical húmedo según Kew. El cultivo exige valorar soporte y espacio; la presencia regional no garantiza aptitud en cualquier huerto.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "QCNE 109343–109345 y 179798: 250 m; 194866: 970 m. Se conservan como puntos de colecta; no se establece mínimo/máximo nacional de cultivo.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Inferido del hábito de liana leñosa duradera; no se confirma un tiempo de cosecha.",
    "cualidad": null,
    "contexto_cualidad": "Sin cualidad térmica comprobada para esta especie. No se asigna por el nombre común ni por su región.",
    "usos_medicinales": "La tabla 2 del estudio UEA 2019 (p. 239) recoge para Uncaria tomentosa usos tradicionales relacionados con gastritis y cólicos estomacales; la tabla cita a De la Torre et al. (2008). Es un registro atribuido, sin prescripción.",
    "saberes": "El MSP p. 20 la propone entre los ejemplos amazónicos. La ficha y la fotografía se restringen a U. tomentosa; no se mezclan con U. guianensis u otras plantas llamadas uña de gato.",
    "fuente_documental": "KN: Royal Botanic Gardens, Kew. Plants of the World Online. Uncaria tomentosa (Willd. ex Schult.) DC. https://powo.science.kew.org/taxon/768322-1\nHN: INABIO QCNE / UNL LOJA. Ejemplares consultados de Uncaria. Filtrar por especie y Ecuador; el listado puede incluir otros taxones o países. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&reset=1&taxa=Uncaria&taxontype=2\nUEA: Andrade-Yucailla et al. Uso de plantas medicinales en comunidades indígenas asentadas en un bosque siempreverde piemontano del cantón Santa Clara, Amazonía Ecuatoriana. RACYT 8(2), 235–243, 2019. https://doi.org/10.59410/RACYT-v08n02ep14-0123 Páginas: 239, tabla 2; la tabla cita a De la Torre et al. (2008).\nMSP: MSP Ecuador. Implementación de huertos medicinales en los establecimientos de salud del primer nivel de atención. Lineamientos, 2017. PDF aportado por el usuario. Páginas: 20, 48, 63, 66, 72–74 (paginación impresa)",
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
 RAISE NOTICE 'Bloque 03: % nuevas, % fichas vacías completadas, % documentadas conservadas. Siembras intactas.', nuevas,completadas,omitidas;
END;
$carga_catalogo$;
