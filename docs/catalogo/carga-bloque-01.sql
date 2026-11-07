-- CARGA MANUAL: este archivo está fuera de Flyway y no se ejecuta al iniciar la aplicación.
-- Requiere V5. Para revisar sin guardar: BEGIN; ejecutar este archivo; ROLLBACK;
-- Para aplicar después de revisar: BEGIN; ejecutar este archivo; COMMIT;
-- La unidad DO es atómica; no sobrescribe ni vincula especies a comunidades.
DO $carga_catalogo$
DECLARE
 ficha RECORD;
 coincidencias INTEGER;
 especie_nueva BIGINT;
 creadas INTEGER := 0;
 omitidas INTEGER := 0;
BEGIN
 -- Evita dos cargas simultáneas y ediciones concurrentes durante este lote breve.
 LOCK TABLE especie IN SHARE ROW EXCLUSIVE MODE;
 FOR ficha IN
  SELECT * FROM jsonb_to_recordset($fichas_catalogo$
[
  {
    "nombre_comun": "Achiote",
    "nombre_cientifico": "Bixa orellana L.",
    "otros_nombres": "Manturu, en contexto kichwa amazónico.",
    "familia_botanica": "Bixaceae",
    "regiones": [
      "COSTA"
    ],
    "provincias": "Esmeraldas, Loja, Orellana y Sucumbíos en los registros QCNE seleccionados. Lista parcial.",
    "clima": "Cálido tropical húmedo. Referencia agronómica general: 20–28 °C y precipitación anual de 1500–2000 mm, con suelo bien drenado.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "100–1275 m en la muestra seleccionada: Esmeraldas 100 m; Orellana 250 m; Sucumbíos 250–350 m; Loja 1275 m. Intervalo observado parcial, no límite nacional ni recomendación de cultivo.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo. Arbusto o árbol perenne; la producción de semillas puede comenzar alrededor de los dos años, según el manejo.",
    "cualidad": "FRIA",
    "contexto_cualidad": "Fría en el registro etnográfico de Amaluza, Loja, de 2018, recogido por la Universidad Nacional de Loja. La clasificación se mantiene vinculada a ese contexto.",
    "usos_medicinales": "El MSP recoge prácticas tradicionales con hojas relacionadas con afecciones prostáticas y hemorragias posparto. La fila continúa entre las páginas 63 y 64. Se conserva el antecedente atribuido, sin preparaciones, dosis ni instrucciones.",
    "saberes": "El nombre manturu procede del repertorio kichwa amazónico; la clasificación fría aquí añadida procede de Amaluza, Loja. No se presentan como un solo testimonio. Región de referencia: Costa como ejemplo del MSP; no exclusiva.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nKew POWO. Bixa orellana. Nombre aceptado, Bixaceae, hábito y bioma. https://powo.science.kew.org/taxon/33335-2\nINABIO QCNE. Bixa orellana. Registros 91201, 187773, 187260, 922 y 106634. Localidades y elevaciones de etiquetas. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&page=1&taxa=Bixa&taxontype=2\nFAO Ecocrop Bixa orellana ficha ecológica y ciclo. https://ecocrop.apps.fao.org/ecocrop/srv/en/dataSheet?id=520\nUNL Saberes ancestrales e interculturalidad en salud en el Qhapaq Ñan de Loja tabla 4.1. https://unl.edu.ec/sites/default/files/archivo/2024-11/Saberes%20ancestrales%20e%20interculturalidad%20en%20salud%20en%20el%20Qhapaq%20%C3%91an%20de%20Loja.pdf",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Aguacate",
    "nombre_cientifico": "Persea americana Mill.",
    "otros_nombres": "Palta.",
    "familia_botanica": "Lauraceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Carchi, Imbabura, Pichincha, Tungurahua, Azuay y Loja, en el ámbito descrito por INIAP. No es una lista exhaustiva nacional.",
    "clima": "Temperatura media de 16–20 °C y suelos con buen drenaje en ese ámbito de cultivo.",
    "altitud_minima": 1800,
    "altitud_maxima": 2600,
    "observaciones_altitud": "1800–2600 m para el cultivo descrito en el manual de INIAP; no se interpreta como límite para todas las variedades.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo. Árbol perenne y cultivo multianual.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "Caliente en los registros del Qhapaq Ñan de Loja, con testimonios de Jimbura de 2018 y San Lucas de 2019. Es una clasificación local, no universal.",
    "usos_medicinales": "El MSP registra uso tradicional externo de la hoja relacionado con neuralgias; también menciona aguacate entre las plantas cicatrizantes sin precisar parte ni procedimiento en ese cuadro. Se conserva la atribución, sin prescribir tratamientos.",
    "saberes": "Registro de uso de la hoja en el repertorio tradicional del MSP. Región de referencia: Sierra como ejemplo del MSP; no exclusiva.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nSotomayor A. et al. INIAP. Manual técnico para la producción de plantas injertas de aguacate Persea americana Mill de alta calidad, 2019. Resumen institucional. https://repositorio.iniap.gob.ec/items/5d84be4d-dd40-4722-9099-f7758c95a20d\nUNL Saberes ancestrales e interculturalidad en salud en el Qhapaq Ñan de Loja tabla 4.1. https://unl.edu.ec/sites/default/files/archivo/2024-11/Saberes%20ancestrales%20e%20interculturalidad%20en%20salud%20en%20el%20Qhapaq%20%C3%91an%20de%20Loja.pdf",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Ajo sacha",
    "nombre_cientifico": "Mansoa alliacea (Lam.) A.H.Gentry",
    "otros_nombres": "Ajo de monte; Sacha Aju en runa shimi; Kaip en los registros shuar, achuar y shiwiar consultados.",
    "familia_botanica": "Bignoniaceae",
    "regiones": [
      "AMAZONIA"
    ],
    "provincias": "Napo, Orellana, Pastaza y Sucumbíos; además Manabí en un registro costero. Lista parcial de QCNE.",
    "clima": "Tropical húmedo. El ensayo CIPCA describe bosque lluvioso premontano, 18–24 °C y 3703 mm de precipitación en su caracterización de zona; no son un óptimo universal.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "Muestra QCNE: Manabí 100–150 m, Orellana 190 m, Sucumbíos 248 m, Pastaza 260 m y Napo 440–500 m. Ensayo CIPCA a 595 m. Son registros y un sitio experimental, no límites nacionales.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo. Liana leñosa de permanencia multianual. El seguimiento de crecimiento durante 320 días de un ensayo no representa su duración total.",
    "cualidad": null,
    "contexto_cualidad": "No registrada expresamente en la documentación revisada. No se deduce del olor a ajo ni de la temperatura de una preparación.",
    "usos_medicinales": "CONFENIAE recoge un testimonio atribuido a Imelda Tapuy sobre usos relacionados con dolores y afecciones respiratorias. Se describen hojas, corteza, tallo y raíz como partes aprovechadas, con usos variables. Se conserva el relato sin adoptar afirmaciones de eficacia ni pautas terapéuticas.",
    "saberes": "Fuente de una organización indígena amazónica que también documenta siembra en huertas. Mantener el testimonio y los nombres atribuidos. Región de referencia: Amazonía como ejemplo del MSP; hay también registros costeros.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nKew POWO. Mansoa alliacea. Nombre aceptado, Bignoniaceae, liana y presencia nativa en Ecuador. https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:152672-2\nINABIO QCNE. Mansoa alliacea. Colección Bignoniaceae, registros 90560, 24720, 205473, 160262, 190982 y 51050. Se excluyeron otras especies del género. https://bndb.sisbioecuador.bio/bndb/collections/list.php?db=1&page=6&taxa=Bignoniaceae&taxontype=3\nSharupi T. La Voz de la CONFENIAE número 22, 2021, pp. 45–46. Testimonio atribuido a Imelda Tapuy y nombres por nacionalidad. Las afirmaciones de eficacia no se adoptan. https://hivos.org/assets/2021/10/Revista-CONFENIAE-2021.pdf\nAbril R. et al. Initial growth of Mansoa alliacea Bignoniaceae species of interest in the Amazon region of Ecuador. Cuban Journal of Agricultural Science 50 número 4, 2016, pp. 673–682. Tabla 1, p. 674. https://www.redalyc.org/pdf/6537/653768177001.pdf",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Albahaca",
    "nombre_cientifico": "Ocimum basilicum L.",
    "otros_nombres": "Chakra kiwa; albahaca blanca. Albahaca morada identifica una variante de color citada en un registro de Loja; no es una especie adicional en este bloque.",
    "familia_botanica": "Lamiaceae",
    "regiones": [
      "COSTA"
    ],
    "provincias": "Pichincha como ubicación de la localidad documentada Reserva Endesa, vía Quito a Puerto Quito; confirmar la etiqueta provincial al cargar el dato definitivo. Loja cuenta con registro etnográfico y de comercialización, que no se utiliza como prueba automática de cultivo.",
    "clima": "Cálido a templado cálido; referencia agronómica general de 18–27 °C. Requiere buena luminosidad y suelo bien drenado; el sitio comunitario debe evaluarse por separado.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "650–700 m en el registro K005202704 de Reserva Endesa; intervalo de localidad, no rango nacional.",
    "ciclo": "CORTO",
    "observaciones_ciclo": "Corto para manejo anual. Referencia de cultivo: 80–270 días; primera cosecha aproximada a 70–110 días. Puede variar por cultivar y manejo.",
    "cualidad": "VARIABLE",
    "contexto_cualidad": "Caliente en el cuadro del MSP. Fría para albahaca morada en un testimonio de Saraguro de 2018 recogido por la UNL. Conservar ambas clasificaciones con su contexto.",
    "usos_medicinales": "El MSP recoge usos de hojas y tallos relacionados con gases y trastornos menstruales. El estudio de mercados registra usos ginecológicos y urinarios para O. basilicum, con la hoja como parte vendida. Sin dosis ni instrucciones.",
    "saberes": "El nombre chakra kiwa, la clasificación general andina del MSP y el testimonio de albahaca morada de Saraguro corresponden a contextos diferentes. Región de referencia: Costa como ejemplo del MSP; no exclusiva.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nKew. Registro Ocimum basilicum K005202704. Reserva Forestal Endesa, vía Quito a Puerto Quito, 18 enero 1987, 650–700 m. Pichincha se propone por localidad, no por campo provincial explícito. https://records.data.kew.org/occurrences/f4cf9430-1789-46f9-afb3-4a212047ee7d\nFAO Ecocrop. Ocimum basilicum código 1547. Referencia agronómica general, no zonificación ecuatoriana. https://ecocrop.apps.fao.org/ecocrop/srv/en/cropView?id=1547\nTinitana F. et al. Medicinal plants sold at traditional markets in southern Ecuador, 2016. Tabla 5: Ocimum basilicum FT46TMPA, Iresine diffusa FT0280 e Iresine herbstii FT0486. Estudio de mercados de Loja; no demuestra cultivo ni eficacia clínica. https://pmc.ncbi.nlm.nih.gov/articles/PMC4934001/\nFAO Ecocrop Ocimum basilicum ficha ecológica. https://ecocrop.apps.fao.org/ecocrop/srv/en/dataSheet?id=1547\nUNL Saberes ancestrales e interculturalidad en salud en el Qhapaq Ñan de Loja tabla 4.1. https://unl.edu.ec/sites/default/files/archivo/2024-11/Saberes%20ancestrales%20e%20interculturalidad%20en%20salud%20en%20el%20Qhapaq%20%C3%91an%20de%20Loja.pdf",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Algodón",
    "nombre_cientifico": "Gossypium barbadense L.",
    "otros_nombres": "Uruch, en el registro shuar del Centro Tink.",
    "familia_botanica": "Malvaceae",
    "regiones": [
      "COSTA"
    ],
    "provincias": "Manabí, ensayo de Portoviejo; Morona Santiago, Centro Shuar Tink. Lista parcial.",
    "clima": "El sitio experimental de Portoviejo se describe con 26,4 °C y humedad relativa de 81 %. No es requisito universal de todos los materiales.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "60 m en Portoviejo y 600 m en el registro de Morona Santiago. Dos puntos documentados, no rango óptimo ni límites nacionales.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo como criterio de esta ficha: se representa el arbusto conservado de forma multianual en el huerto. También admite manejo anual; un ensayo de Portoviejo registró maduración entre 154 y 199 días. El tiempo de cosecha se distingue de la duración del ejemplar.",
    "cualidad": null,
    "contexto_cualidad": "No registrada expresamente en las fuentes revisadas.",
    "usos_medicinales": "El MSP recoge prácticas con semilla tierna y raíz relacionadas con molestias del oído y sangrado menstrual. La etiqueta shuar NY registra uso de la raíz relacionado con diarrea. Son antecedentes atribuidos, sin instrucciones de administración.",
    "saberes": "El registro shuar conserva localidad y nombre; su práctica se mantiene separada del registro del MSP. Región de referencia: Costa como ejemplo del MSP; también hay registro amazónico.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nPárraga Navarrete J. et al. INIAP. Respuesta adaptativa de diez accesiones de algodón de colores Gossypium barbadense en las condiciones del valle del río Portoviejo, 2023. Resumen del ensayo de 2022. https://repositorio.iniap.gob.ec/items/deeabc2b-4fc7-41bb-818a-49865f466b7b\nNew York Botanical Garden. Registro biocultural Gossypium barbadense NY 89746. Utitiaj 6, 1985, Centro Shuar Tink, Morona Santiago, 600 m. Nombre uruch y uso de raíz relacionado con diarrea. https://sweetgum.nybg.org/science/biocultural/specimen-details/?irn=713393\nFAO Ecocrop. Gossypium barbadense código 1158. Manejo anual o perenne; su altitud tropical global no se traslada a Ecuador. https://ecocrop.apps.fao.org/ecocrop/srv/en/cropView?id=1158",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Café arábigo",
    "nombre_cientifico": "Coffea arabica L.",
    "otros_nombres": "Café, cafeto, café arábigo. La especie robusta Coffea canephora queda fuera de esta ficha.",
    "familia_botanica": "Rubiaceae",
    "regiones": [
      "COSTA"
    ],
    "provincias": "La síntesis histórica del BCE menciona arábigo en Manabí, El Oro, Loja, Sucumbíos, Guayas y Cañar. Lista parcial histórica, no estadística actual.",
    "clima": "Tropical con estación menos lluviosa o subtropical húmedo; referencia agronómica general de 14–28 °C y precipitación anual de 1400–2300 mm. No equivale a una zonificación exclusiva de Ecuador.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "Referencia regional histórica: hasta unos 600 m en Manabí; 500–1600 m en El Oro y Loja. INIAP describe cultivo arábigo hasta unos 2000 m. No inventar mínimo nacional común.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo. Arbusto o árbol pequeño perenne; puede iniciar producción alrededor de los dos o tres años. La maduración de los frutos y las cosechas anuales no determinan la vida total del cultivo.",
    "cualidad": null,
    "contexto_cualidad": "No registrada con suficiente contexto para esta especie en lo revisado.",
    "usos_medicinales": "El MSP incluye el café entre las plantas estimulantes, sin especificar especie en ese cuadro. La ficha de Coffea arabica de FAO registra la semilla en categorías de uso relacionadas con el sistema nervioso. Se presenta el antecedente documental sin diagnósticos ni instrucciones de consumo.",
    "saberes": "Se propone café arábigo para precisar una sola especie del catálogo común. El nombre genérico café del listado regional del MSP no identifica por sí mismo un ejemplar de la comunidad. Región de referencia: Costa como ejemplo del MSP. Propuesta restringida a arábigo.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nBanco Central del Ecuador. Apuntes de Economía número 40, sección 2.3.2, p. 19. Síntesis histórica de arábigo y robusta con referencias MAG e INIAP; no es estadística vigente. https://contenido.bce.fin.ec/documentos/PublicacionesNotas/Catalogo/Apuntes/ae40.pdf\nINIAP. Variedades mejoradas de café arábigo una contribución para el desarrollo de la caficultura en el Ecuador, 2004. Resumen institucional. https://repositorio.iniap.gob.ec/items/01305c66-6687-4256-8779-a50bc9f30706\nKew POWO. Coffea arabica. Nombre aceptado y distribución introducida en Ecuador. https://powo.science.kew.org/taxon/747038-1\nFAO Ecocrop Coffea arabica ficha ecológica. https://ecocrop.apps.fao.org/ecocrop/srv/en/dataSheet?id=749",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Canela amazónica",
    "nombre_cientifico": "Mespilodaphne quixos (Lam.) Rohwer",
    "otros_nombres": "Ishpingo; sinónimo científico frecuente: Ocotea quixos (Lam.) Kosterm. Esta ficha corresponde a canela amazónica, no a Cinnamomum verum.",
    "familia_botanica": "Lauraceae",
    "regiones": [
      "AMAZONIA"
    ],
    "provincias": "Napo, Pichincha y Sucumbíos en el listado ARCSA bajo Ocotea quixos. Lista de 2015, no exhaustiva actual.",
    "clima": "Valles orientales tropicales y subtropicales húmedos en la descripción histórica; bioma tropical húmedo en Kew.",
    "altitud_minima": 0,
    "altitud_maxima": 1000,
    "observaciones_altitud": "0–1000 m como distribución reportada para Ocotea quixos; no intervalo óptimo de cultivo.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo. Árbol perenne de crecimiento lento y cultivo de permanencia multianual.",
    "cualidad": null,
    "contexto_cualidad": "No registrada para esta especie con el contexto necesario. El MSP clasifica el nombre genérico canela como caliente, sin identificar la especie; ese dato no se transfiere automáticamente al ishpingo.",
    "usos_medicinales": "Naranjo y colaboradores describen usos tradicionales ecuatorianos relacionados con digestión, apetito, diarrea y empleo local como desinfectante o anestésico. Atribuciones históricas, sin eficacia clínica ni instrucciones; no transferir a otras canelas.",
    "saberes": "Especie propuesta para representar la canela amazónica en este catálogo de trabajo. El listado del MSP no permite confirmar por sí solo que toda mención de canela corresponda al mismo taxón. Región de referencia: Amazonía como ejemplo del MSP; el nombre común no prueba una especie concreta.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nKew POWO. Mespilodaphne quixos. Nombre aceptado; Ocotea quixos como sinónimo. Lauraceae, árbol y bioma tropical húmedo. https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:77198017-1/general-information\nARCSA. Listado de plantas medicinales del Ecuador, archivo de 24 julio 2015. Filas Baccharis latifolia y Ocotea quixos. Referencia histórica de altitud y provincias; no implica autorización sanitaria. https://controlsanitario.gob.ec/wp-content/uploads/downloads/2015/08/listado-de-plantas-medicinales-del-ecuador-24-07-2015.pdf\nNaranjo P., Kijjoa A., Giesbrecht A. M. y Gottlieb O. R. Ocotea quixos American cinnamon. Journal of Ethnopharmacology 4, 1981, pp. 233–236. Historia, botánica y usos tradicionales, pp. 233–234. https://dspace-test.ucuenca.edu.ec/bitstream/123456789/43733/1/Ocotea-quixos-american.pdf",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Ceibo costeño",
    "nombre_cientifico": "Ceiba trischistandra (A.Gray) Bakh.",
    "otros_nombres": "Ceibo. Costeño es una aclaración de catálogo para distinguir la especie de otras ceibas.",
    "familia_botanica": "Malvaceae",
    "regiones": [
      "COSTA"
    ],
    "provincias": "Loja, reserva Laipuna; Manabí, comuna Agua Blanca. Lista parcial de las fuentes usadas.",
    "clima": "Bosque seco premontano en Laipuna y ecosistema seco tropical en Agua Blanca. Descripciones locales; no se impone un bioma global.",
    "altitud_minima": null,
    "altitud_maxima": null,
    "observaciones_altitud": "400–1400 m en la etiqueta Homeier 1316, QCNE 229889, Laipuna. Intervalo de localidad, no límite nacional.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo. Árbol perenne; la pérdida estacional de hojas no significa que sea una planta anual.",
    "cualidad": null,
    "contexto_cualidad": "No registrada expresamente para esta especie en lo revisado.",
    "usos_medicinales": "El estudio de biodiversidad de la comuna Agua Blanca, Manabí, registra usos tradicionales de hojas, flores y corteza relacionados con tos, fiebre y dolor de cabeza. El dato fue cotejado en el documento completo, tablas de las páginas 43 y 64. Se conserva como uso reportado, sin afirmar eficacia ni indicar preparación.",
    "saberes": "Registro de usos de Agua Blanca, Manabí. Se propone esta especie costeña como entrada concreta del catálogo; la mención genérica ceibo del MSP no identifica automáticamente todos los ejemplares locales. Región de referencia: Costa como ejemplo del MSP.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nKew POWO. Ceiba trischistandra. Nombre aceptado, Malvaceae y distribución nativa occidental de Ecuador. https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:51087-2/general-information\nINABIO QCNE 229889. Homeier 1316, 2004. Ceiba trischistandra, reserva Laipuna, Loja; etiqueta 400–1400 m, bosque seco premontano. Intervalo de localidad. https://pruebas.sisbioecuador.bio/bndb/collections/individual/index.php?occid=29129\nChele González E. G. UNESUM, Usos de interés turísticos de la biodiversidad de la comuna Agua Blanca, 2025, pp. 43 y 64. https://repositorio.unesum.edu.ec/handle/53000/8059",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Chilca",
    "nombre_cientifico": "Baccharis latifolia (Ruiz & Pav.) Pers.",
    "otros_nombres": "Chillka.",
    "familia_botanica": "Asteraceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay, Bolívar, Cañar, Carchi, Chimborazo, Cotopaxi, Imbabura, Loja, Napo, Pichincha, Sucumbíos y Tungurahua. Lista ARCSA; no implica adaptación uniforme por provincia.",
    "clima": "Ambientes andinos templados y valles interandinos. Como dato local, una recolección en Ibarra, Imbabura, a 2210 m describe temperatura media anual de 15,4 °C; no se convierte en un óptimo nacional.",
    "altitud_minima": 1000,
    "altitud_maxima": 4000,
    "observaciones_altitud": "1000–4000 m como distribución reportada, no intervalo óptimo de cultivo.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo. Arbusto de permanencia multianual, con capacidad de rebrote.",
    "cualidad": "CALIENTE",
    "contexto_cualidad": "Caliente en el cuadro de medicina andina del MSP.",
    "usos_medicinales": "El MSP registra empleo cultural en limpias. El estudio del Valle del Chota documenta usos tradicionales relacionados con dolencias articulares e inflamaciones, con tallos, flores y hojas. Sin instrucciones ni afirmaciones de eficacia.",
    "saberes": "El registro del Chota corresponde a saberes afroecuatorianos; se mantiene separado del cuadro andino y de la función de cerco vivo. Región de referencia: Sierra como ejemplo del MSP; no exclusiva.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nARCSA. Listado de plantas medicinales del Ecuador, archivo de 24 julio 2015. Filas Baccharis latifolia y Ocotea quixos. Referencia histórica de altitud y provincias; no implica autorización sanitaria. https://controlsanitario.gob.ec/wp-content/uploads/downloads/2015/08/listado-de-plantas-medicinales-del-ecuador-24-07-2015.pdf\nKew POWO. Baccharis latifolia. Nombre aceptado, Asteraceae y especie nativa de Ecuador. https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:183399-1\nHernández Montenegro J. M. Estudio sobre domesticación y valorización de especies vegetales silvestres afroecuatorianas en el territorio ancestral del Valle del Chota. UPEC, 2026. Resumen institucional del trabajo de campo. https://repositorio.upec.edu.ec/items/7e5824b5-3124-4f74-bb02-47b60216103e\nUTN Análisis y obtención de colorante natural a partir de Baccharis latifolia p 91. https://repositorio.utn.edu.ec/bitstream/123456789/3529/1/04%20IT%20007%20TESIS.pdf",
    "foto_fuente": null
  },
  {
    "nombre_comun": "Escancel",
    "nombre_cientifico": "Iresine diffusa Humb. & Bonpl. ex Willd.",
    "otros_nombres": "Escancel. Las formas herbstii y lindenii se conservarán cuando se identifique el material; no se añaden como plantas extra a este bloque.",
    "familia_botanica": "Amaranthaceae",
    "regiones": [
      "SIERRA"
    ],
    "provincias": "Azuay, Girón, Santa Carmen: presencia puntual en muestra 237. La venta en Loja se registra como comercio, no como presencia silvestre o cultivo probado.",
    "clima": "Tropical a subtropical, con variación entre localidades y formas; Kew la asocia de forma general al bioma tropical estacionalmente seco. No se fija un intervalo térmico único que las fuentes no establecen.",
    "altitud_minima": 0,
    "altitud_maxima": 3500,
    "observaciones_altitud": "Rango ecuatoriano publicado de 0–3500 m para Iresine diffusa; registro puntual del Herbario Azuay a 2700 m en Girón. El rango no define por sí solo el sitio óptimo de cultivo ni se atribuye automáticamente a cada forma.",
    "ciclo": "LARGO",
    "observaciones_ciclo": "Largo como criterio de trabajo para el material perenne descrito en la ficha ecuatoriana. La duración puede variar con la forma y el manejo; se conservará esa nota al seleccionar el ejemplar.",
    "cualidad": "FRESCA",
    "contexto_cualidad": "Fresca para el nombre escancel en el MSP. Esa referencia carece de identificación de forma botánica; no se transfiere a plantas de otro género llamadas escancel.",
    "usos_medicinales": "El estudio de mercados registra para I. diffusa FT0280 usos en categorías digestivas, respiratorias y urinarias, entre otras; rama como parte comercializada. I. herbstii FT0486 aparece en fila distinta. Mantener los registros separados, sin fusionar usos ni indicar tratamientos.",
    "saberes": "El nombre escancel también se aplica a otras plantas. Esta ficha se limita a Iresine diffusa; no incorpora los datos de Aerva sanguinolenta ni mezcla los registros de sus formas. Región de referencia: Sierra como ejemplo del MSP.",
    "fuente_documental": "MSP. Lineamientos para implementar huertos medicinales, 2017. PDF entregado por el usuario. Se revisaron directamente p. 20, pp. 61–64 y pp. 72–74 impresas.\nTinitana F. et al. Medicinal plants sold at traditional markets in southern Ecuador, 2016. Tabla 5: Ocimum basilicum FT46TMPA, Iresine diffusa FT0280 e Iresine herbstii FT0486. Estudio de mercados de Loja; no demuestra cultivo ni eficacia clínica. https://pmc.ncbi.nlm.nih.gov/articles/PMC4934001/\nKew MPNS. Iresine diffusa. Nombre aceptado, Amaranthaceae y formas diffusa, lindenii y herbstii. Correspondencias con nombres de fuentes medicinales. https://mpns.science.kew.org/mpns-portal/plantDetail?dbs=wcsCmp&plantId=2866580\nUniversidad del Azuay. Herbario Azuay muestra 237. Iresine diffusa, colección de 1998 en Girón, Santa Carmen, a 2700 m. Registro puntual. https://herbario.uazuay.edu.ec/muestras/iresine-diffusa-237\nQuintana C. Plantas silvestres de los valles secos cercanos a Quito. Herbario QCA PUCE, 2013. Entrada Iresine diffusa, texto p. 63. Consulta del texto indexado; el archivo excedió el tamaño admitido. Rango y hábito pendientes de cotejo integral. https://repositorio.puce.edu.ec/bitstreams/1ab46d3d-db41-4fd1-b6f7-638bcd34d057/download\nKew POWO. Iresine diffusa. Nombre aceptado, hábito subarbustivo o arbustivo y bioma tropical estacionalmente seco. No define clima óptimo ecuatoriano. https://powo.science.kew.org/taxon/urn:lsid:ipni.org:names:129900-2/general-information",
    "foto_fuente": null
  }
]
$fichas_catalogo$::jsonb) AS datos(
 nombre_comun TEXT, nombre_cientifico TEXT, otros_nombres TEXT, familia_botanica TEXT,
 regiones TEXT[], provincias TEXT, clima TEXT, altitud_minima INTEGER, altitud_maxima INTEGER,
 observaciones_altitud TEXT, ciclo TEXT, observaciones_ciclo TEXT, cualidad TEXT,
 contexto_cualidad TEXT, usos_medicinales TEXT, saberes TEXT, fuente_documental TEXT, foto_fuente TEXT)
 LOOP
  SELECT COUNT(*) INTO coincidencias FROM especie e
   WHERE lower(btrim(e.nombre_cientifico)) = lower(btrim(ficha.nombre_cientifico));
  IF coincidencias > 1 THEN
   RAISE EXCEPTION 'Identificación repetida en el catálogo: %. Revisa las fichas existentes.', ficha.nombre_cientifico;
  ELSIF coincidencias = 1 THEN
   omitidas := omitidas + 1;
   RAISE NOTICE 'Se conserva sin cambios la ficha existente de %.', ficha.nombre_cientifico;
   CONTINUE;
  END IF;
  IF EXISTS (SELECT 1 FROM especie e
    WHERE lower(btrim(e.nombre_comun)) = lower(btrim(ficha.nombre_comun))
      AND NULLIF(btrim(e.nombre_cientifico), '') IS NULL) THEN
   RAISE EXCEPTION 'Ya existe % sin identificación científica. Revísala antes de cargar.', ficha.nombre_comun;
  END IF;
  INSERT INTO especie(nombre_comun, nombre_cientifico, otros_nombres, familia_botanica,
    provincias, clima, altitud_minima, altitud_maxima, observaciones_altitud, ciclo,
    observaciones_ciclo, cualidad, contexto_cualidad, usos_medicinales, saberes, fuente_documental, foto_fuente)
  VALUES(ficha.nombre_comun, ficha.nombre_cientifico, ficha.otros_nombres, ficha.familia_botanica,
    ficha.provincias, ficha.clima, ficha.altitud_minima, ficha.altitud_maxima, ficha.observaciones_altitud, ficha.ciclo,
    ficha.observaciones_ciclo, ficha.cualidad, ficha.contexto_cualidad, ficha.usos_medicinales, ficha.saberes,
    ficha.fuente_documental, ficha.foto_fuente)
  RETURNING id INTO especie_nueva;
  INSERT INTO especie_region(especie_id, region)
   SELECT especie_nueva, r FROM unnest(ficha.regiones) AS r;
  creadas := creadas + 1;
 END LOOP;
 RAISE NOTICE 'Bloque 01: % fichas nuevas y % existentes conservadas. No se modifica ninguna siembra.', creadas, omitidas;
END;
$carga_catalogo$;
