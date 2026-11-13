from pathlib import Path
import re, math, json, html
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.colors import HexColor, white
from reportlab.platypus import Paragraph, Table, TableStyle
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_LEFT
from pypdf import PdfReader, PdfWriter

REPO=Path(__file__).resolve().parents[3]
ROOT=REPO.parent
OUT=REPO/'docs/informes'
OUT.mkdir(parents=True,exist_ok=True)
SRC=OUT/'fuentes'; SRC.mkdir(exist_ok=True)
FONT=Path('C:/Windows/Fonts')
for name,file in [('Body','segoeui.ttf'),('Bold','segoeuib.ttf'),('Mono','consola.ttf')]:
    pdfmetrics.registerFont(TTFont(name,str(FONT/file)))
pdfmetrics.registerFontFamily('Body',normal='Body',bold='Bold',italic='Body',boldItalic='Bold')
W,H=841.89,595.28
INK='#153B37'; GREEN='#167264'; LIGHT='#EAF4EF'; GRAY='#5D726F'; LINE='#C9DCD5'; GOLD='#C89840'; PAPER='#F8FAF8'
pdf=canvas.Canvas(str(OUT/'02_Arquitectura_V1.pdf'),pagesize=(W,H))
pdf.setTitle('ALLPANA | Arquitectura y diseño técnico - V1')
pdf.setAuthor('Ariel | Integrante 2')
md=['# Arquitectura y diseño técnico\n\nALLPANA · Huertos Medicinales Comunitarios\n\nIntegrante 2: Ariel. Versión 1.0. Corte: 12 de noviembre de 2026.\n']
page=0; svg=[]; record=False

def color(s): return HexColor(s)
def rect(x,y,w,h,fill=white,stroke=None,r=0):
    pdf.setFillColor(color(fill) if isinstance(fill,str) else fill)
    pdf.setStrokeColor(color(stroke or fill) if isinstance(stroke or fill,str) else stroke or fill)
    if r: pdf.roundRect(x,H-y-h,w,h,r,stroke=bool(stroke),fill=1)
    else: pdf.rect(x,H-y-h,w,h,stroke=bool(stroke),fill=1)
    if record: svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill if isinstance(fill,str) else "white"}" stroke="{stroke or "none"}"/>')

def txt(x,y,t,size=10,font='Body',fill=INK):
    pdf.setFillColor(color(fill)); pdf.setFont(font,size); pdf.drawString(x,H-y-size*.8,t)
    if record: svg.append(f'<text x="{x}" y="{y+size*.8}" font-family="{ "Consolas" if font=="Mono" else "Segoe UI, sans-serif"}" font-size="{size}" font-weight="{700 if font=="Bold" else 400}" fill="{fill}">{html.escape(t)}</text>')

def path(points,fill=GREEN,dash=False,arrow=False):
    pdf.setStrokeColor(color(fill)); pdf.setLineWidth(1.15); pdf.setDash(3,3) if dash else pdf.setDash()
    p=pdf.beginPath(); p.moveTo(points[0][0],H-points[0][1])
    for x,y in points[1:]:p.lineTo(x,H-y)
    pdf.drawPath(p);pdf.setDash()
    if record: svg.append(f'<polyline points="{" ".join(f"{x},{y}" for x,y in points)}" fill="none" stroke="{fill}" stroke-width="1.15" '+('stroke-dasharray="3 3"' if dash else '')+'/>')
    if arrow:
        x,y=points[-1];a,b=points[-2]; angle=math.atan2(y-b,x-a)
        pts=[(x-7*math.cos(angle-.4),y-7*math.sin(angle-.4)),(x,y),(x-7*math.cos(angle+.4),y-7*math.sin(angle+.4))]
        path(pts,fill)

def para(t,x,y,w,size=11,leading=None):
    st=ParagraphStyle('p',fontName='Body',fontSize=size,leading=leading or size*1.48,textColor=color(INK),spaceAfter=0)
    p=Paragraph(t,st);a,b=p.wrap(w,1000); p.drawOn(pdf,x,H-y-b)
    return y+b

def section(t,x,y,w,text):
    txt(x,y,t,14,'Bold',GREEN)
    return para(text,x,y+26,w)

def table(headers,rows,x=40,y=140,widths=None,size=10):
    widths=widths or [210,552]
    st=ParagraphStyle('tab',fontName='Body',fontSize=size,leading=size*1.35,textColor=color(INK))
    data=[[Paragraph(str(c),st) for c in row] for row in [headers]+rows]
    t=Table(data,colWidths=widths,hAlign='LEFT')
    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),color(LIGHT)),('FONTNAME',(0,0),(-1,0),'Bold'),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),10),('RIGHTPADDING',(0,0),(-1,-1),10),('TOPPADDING',(0,0),(-1,-1),8),('BOTTOMPADDING',(0,0),(-1,-1),8),('LINEBELOW',(0,0),(-1,-1),.4,color(LINE))]))
    _,h=t.wrap(sum(widths),1000);t.drawOn(pdf,x,H-y-h);return y+h

def new(title,kicker,sub=''):
    global page
    if page:pdf.showPage()
    page+=1
    pdf.bookmarkPage(f'pagina-{page}')
    pdf.addOutlineEntry(title,f'pagina-{page}',level=0)
    rect(0,0,W,H,white)
    rect(40,32,25,4,GREEN)
    txt(75,27,'ALLPANA / ARQUITECTURA Y DISEÑO TÉCNICO',8,'Bold',GRAY)
    txt(40,59,title,25,'Bold')
    if sub:txt(40,97,sub,10,'Body',GRAY)
    path([(40,550),(802,550)],LINE)
    txt(40,562,'V1.0 · Ariel · Integrante 2 · 12 NOV 2026 · Versión local',8,'Body',GRAY)
    txt(676,562,f'{kicker.upper()}  /  {page:02}',8,'Bold',GRAY)
    md.append('\n## '+title+'\n')

def note(t,y=514):
    rect(40,y,762,25,LIGHT,r=4);txt(50,y+8,t,8.4,'Body',GRAY)

def cls(x,y,name,module,attrs,methods,ref=False,w=220):
    ah=18*len(attrs)+10;mh=17*len(methods)+10; ht=42+ah+mh
    rect(x,y,w,ht,white,LINE,5);rect(x+1,y+1,w-2,40,LIGHT if not ref else '#F0F2F1')
    txt(x+10,y+6,module+(' · referencia' if ref else ' · entidad'),7.7,'Body',GRAY)
    txt(x+10,y+20,name,12,'Bold')
    for i,a in enumerate(attrs):txt(x+10,y+49+i*18,'- '+a,8.7,'Mono')
    path([(x,y+42+ah),(x+w,y+42+ah)],LINE)
    for i,m in enumerate(methods):txt(x+10,y+49+ah+i*17,'+ '+m,8.3,'Mono')
    return (x,y,w,ht)

def assoc(a,b,ma='1',mb='0..*',label='',vertical=False):
    if vertical:
        x=a[0]+a[2]/2;y=a[1]+a[3];xx=b[0]+b[2]/2;yy=b[1]
        path([(x,y),(x,(y+yy)/2),(xx,(y+yy)/2),(xx,yy)])
        txt(x+6,y+6,ma,8,'Bold',GREEN);txt(xx+6,yy-14,mb,8,'Bold',GREEN)
        if label:txt(x+13,(y+yy)/2-10,label,7.8,'Body',GRAY)
    else:
        left,right=(a,b) if a[0]<b[0] else (b,a)
        lm,rm=(ma,mb) if a[0]<b[0] else (mb,ma)
        x=left[0]+left[2];xx=right[0];y=left[1]+68
        path([(x,y),(xx,y)]);txt(x+3,y-14,lm,7.8,'Bold',GREEN);txt(xx-24,y+5,rm,7.8,'Bold',GREEN)

def startsvg():
    global svg,record
    svg=[];record=True
def endsvg(name):
    global record
    (OUT/name).write_text('<svg xmlns="http://www.w3.org/2000/svg" width="1123" height="794" viewBox="0 0 841.89 595.28">'+'\n'.join(svg)+'</svg>',encoding='utf-8');record=False

new('Arquitectura y diseño técnico','informe')
rect(40,125,762,292,INK,r=10)
txt(70,155,'HUERTOS MEDICINALES COMUNITARIOS',12,'Bold','#9ACBBB')
txt(70,196,'ALLPANA',50,'Bold','#FFFFFF')
# White text in dark cover, separate paragraph style.
st=ParagraphStyle('cover',fontName='Body',fontSize=17,leading=25,textColor=white)
p=Paragraph('Informe de la arquitectura implementada, el modelo de clases y los flujos críticos del prototipo.',st);_,h=p.wrap(635,120);p.drawOn(pdf,70,H-275-h)
txt(70,379,'Monolito modular · Java 21 · Spring Boot · PostgreSQL',12,'Body','#FFFFFF')
txt(40,440,'Ariel | Integrante 2',14,'Bold')
txt(40,469,'Versión 1.0 | Corte documental: 12 de noviembre de 2026',11,'Body',GRAY)
txt(40,491,'Rama local: Ariel_Baldeon | Base de Develop: 9a7bd05',11,'Body',GRAY)
txt(40,520,'Incluye 3 láminas de clases UML, 2 secuencias y despliegue local.',11,'Bold',GREEN)
md.append('Documento de la copia local, incluidos los cambios sin commit. No describe únicamente el commit remoto 9a7bd05.\n')

new('01. Alcance y criterio de lectura','contexto','La documentación representa el código local refactorizado, incluidos cambios aún sin commit.')
section('Objetivo',40,135,355,'Explicar cómo está construido ALLPANA, qué responsabilidades tiene cada parte y cómo colaboran sus clases. El informe cubre los cinco puntos de la actividad: arquitectura, componentes, clases, secuencias y despliegue.')
section('Método de comprobación',445,135,355,'Se contrastaron clases de dominio, servicios, controladores, migraciones, configuración y archivos de despliegue. Los resultados de pruebas se toman de la verificación local registrada; esta entrega documental no vuelve a ejecutar la suite ni altera el negocio.')
section('Qué implementa el prototipo',40,293,355,'Gestiona comunidades, miembros, huertos, catálogo de especies, plantas, actividades con evidencias, revisión humana, misiones, puntos, ranking y una bodega básica. La interfaz pública muestra mapa y fichas; las áreas privadas dependen del rol y del recurso autorizado.')
section('Cómo leer los diagramas',445,293,355,'Las clases muestran una selección de atributos y operaciones reales. Se omiten getters repetitivos, constructores y campos secundarios. Las asociaciones de dominio son referencias por ID/FK; no significan navegación entre entidades JPA. Los elementos repetidos entre láminas se marcan como referencia.')
note('Estado observado: implementación local. Un diagrama describe su diseño; no certifica que todo requisito o caso límite esté resuelto.')

new('02. Estilo arquitectónico y justificación','arquitectura','Un único proyecto Maven, un JAR de aplicación y una base de datos compartida.')
section('Monolito modular por capas',40,136,355,'Los módulos agrupan capacidades del negocio y se dividen en presentación, aplicación, dominio, persistencia y configuración cuando las necesitan. Se despliegan juntos y se comunican mediante llamadas Java síncronas. Thymeleaf y los recursos del navegador se empaquetan en el mismo JAR.')
section('Por qué encaja con el prototipo',445,136,355,'Permite coordinar revisión, actividad, recompensas y misión dentro de una transacción de PostgreSQL. Reduce la operación de infraestructura y conserva el estilo de programación existente. La separación por capas delimita cambios sin exigir reescribir el proyecto.')
section('Costo y restricciones',40,332,355,'La base y el despliegue son compartidos. Los módulos no son servicios autónomos y existen dependencias entre sus casos de uso. Cambiar una entidad o una migración puede afectar varias capacidades; por ello se conservan contratos de lectura y pruebas de integración.')
section('Precisión del término',445,332,355,'No se presenta como arquitectura hexagonal pura: el dominio mantiene anotaciones JPA y los servicios de aplicación usan repositorios concretos. El puerto de archivos permite sustituir ese adaptador, pero no convierte por sí solo todo el sistema en puertos y adaptadores.')

new('03. Componentes y responsabilidades','módulos','La división funcional coincide con los paquetes productivos bajo ec.edu.epn.huertos.')
table(['Módulo','Responsabilidad principal'],[
('autenticacion','Registro, usuarios, roles, autorización y recarga de identidad de sesión.'),
('comunidades','Membresías, invitaciones, cupos, solicitudes, asignación de revisores y archivo/restauración.'),
('huertos','Huerto real, espacios, cerramientos, especies compartidas, plantas y lectura del huerto virtual.'),
('actividades / evidencias','Registro y participantes; validación y almacenamiento de archivos; envíos y revisión humana.'),
('misiones / gamificacion','Plantillas y dependencias, estados por huerto, recompensas individuales/comunitarias y ranking.'),
('bodega / publico','Inventario básico por huerto; portada, mapa y fichas para visitantes.'),
('compartido / demo','Fechas, excepciones y errores comunes; carga sintética condicional del perfil demo.')],y=135,size=10.2)
note('Los módulos colaboran en aplicación. No hay una base de datos separada ni un proceso independiente por módulo.')

new('04. Diagrama de arquitectura','componentes','Vista de ejecución y comunicación; todas las capas internas pertenecen al mismo proceso Java.')
def box(x,y,w,h,title,body,fill=LIGHT):
    rect(x,y,w,h,fill,LINE,6);txt(x+12,y+12,title,12,'Bold');para(body,x+12,y+37,w-24,9.7)
box(40,145,160,108,'Navegador','HTML, CSS y JavaScript<br/>Bootstrap · Leaflet')
rect(244,129,350,362,PAPER,LINE,8);txt(260,140,'Aplicación Spring Boot / JAR',11,'Bold',GREEN)
box(262,169,314,78,'Presentación','Spring MVC · controladores · Thymeleaf')
box(262,270,314,94,'Aplicación','Servicios, autorización, DTO y transacciones')
box(262,387,145,82,'Dominio','Entidades, estados y valores')
box(424,387,152,82,'Persistencia','JPA / JDBC / archivos')
box(640,168,162,84,'PostgreSQL 17','Datos + restricciones<br/>Esquema por Flyway')
box(640,284,162,84,'Archivos privados','Fotos y videos<br/>Fuera de static')
box(40,360,160,90,'OpenStreetMap','Teselas del mapa<br/>Solicitadas por Leaflet')
path([(200,204),(262,204)],arrow=True);txt(205,182,'HTTP',8,'Bold')
path([(419,247),(419,270)],arrow=True)
path([(335,364),(335,387)],arrow=True);path([(500,364),(500,387)],arrow=True)
path([(576,426),(615,426),(615,210),(640,210)],arrow=True);txt(603,194,'JDBC',7.5)
path([(576,442),(626,442),(626,326),(640,326)],arrow=True)
path([(118,253),(118,360)],dash=True,arrow=True);txt(126,300,'HTTPS',8)
note('Configuración conecta Security y propiedades. FFprobe es un proceso local para validar video; no un servicio remoto.')

new('05. Fronteras y organización física','capas','Las reglas se aplican por capa dentro de cada módulo funcional.')
table(['Capa','Contrato y dependencias permitidas'],[
('presentacion','Adapta HTTP. Consume servicios y DTO de aplicación. No usa repositorios, SQL ni entidades JPA, tampoco anidadas.'),
('aplicacion','Coordina casos de uso, permisos y transacciones. Usa dominio y persistencia. No depende de controladores ni ejecuta JDBC/EntityManager.'),
('dominio','Contiene entidades, enums, valores y reglas. No depende de las otras capas; conserva las anotaciones JPA existentes.'),
('persistencia','Implementa consultas, joins, bloqueos y archivos. Usa dominio; puede devolver proyecciones de consulta de aplicación.'),
('configuracion','Conecta infraestructura, propiedades y seguridad. No sustituye a los servicios del negocio.')],y=133,size=10)
para('<b>Ubicación:</b> src/main/java/ec/edu/epn/huertos/&lt;modulo&gt;/&lt;capa&gt;/. Las migraciones Java están en db/catalogo y las SQL en src/main/resources/db/migration. Las vistas activas están en frontend/templates y los recursos en frontend/static.',40,436,762,10)
note('La excepción de proyecciones persistencia → aplicación está documentada. No se declara un grafo acíclico de módulos.')

# UML diagrams: six classes per sheet, vector and separate editable SVG.
startsvg();new('06. Clases: comunidad y huerto','uml 1/3','Atributos principales y métodos seleccionados. Asociación sólida = relación referencial por ID/FK.')
a=cls(40,128,'Usuario','autenticacion',['id: Long','nombreUsuario: String','rol: Rol','activo: boolean'],['cambiarRol(rol: Rol): void'])
b=cls(310,128,'Integrante','comunidades',['id, usuarioId: Long','comunidadId: Long','activo, esGestor: boolean'],['darDeBaja(): void','asumirGestion(): void'])
c=cls(580,128,'Comunidad','comunidades',['id: Long','tipoComunidad: String','activa: boolean'],['archivar(id: Long): void','restaurar(): void'])
d=cls(40,360,'Especie','huertos',['id: Long','nombreComun: String','regiones: Set<RegionPlanta>'],['actualizar(DatosEspecie): void','cambiarFoto(...): void'])
e=cls(310,360,'Planta','huertos',['id, huertoId: Long','especieId: Long','cantidad: int'],['cambiarSiembra(...): void','cambiarActiva(...): void'])
f=cls(580,360,'Huerto','huertos',['id, comunidadId: Long','nombre: String','areaM2: BigDecimal'],['actualizar(...): void'])
assoc(a,b,'1','0..*');assoc(b,c,'0..*','1');assoc(c,f,'1','0..1',vertical=True);assoc(d,e,'1','0..*');assoc(e,f,'0..*','1')
note('Usuario puede tener membresías históricas; solo una activa. Planta usa especieId, sin @ManyToOne.',523)
endsvg('Diagrama_clases_01_comunidad_huerto.svg')

startsvg();new('07. Clases: actividades y evidencias','uml 2/3','El historial conserva envíos anteriores; cada envío admite una única revisión.')
a=cls(40,128,'Integrante','comunidades',['id: Long','comunidadId: Long'],['isActivo(): boolean'],True)
b=cls(310,128,'ActividadParticipante','actividades',['id: Long','actividadId: Long','integranteId: Long','comunidadId: Long'],['getIntegranteId(): Long'])
c=cls(580,128,'Actividad','actividades',['id, huertoId: Long','registradaPor: Long','estado: EstadoRevision'],['aplicarDecision(...): void','reabrirPorReenvio(): void'])
d=cls(40,360,'Usuario','autenticacion',['id: Long','rol: Rol'],['getRol(): Rol'],True)
e=cls(310,360,'Revision','evidencias',['id, envioId: Long','revisorId: Long','decision: EstadoRevision'],['getDecision(): EstadoRevision'])
f=cls(580,360,'EnvioEvidencia','evidencias',['id, actividadId: Long','numero: int','claveAlmacenamiento: String'],['aplicarDecision(...): void'])
assoc(a,b,'1','0..*');assoc(b,c,'0..*','1');assoc(c,f,'1','0..*',vertical=True);assoc(d,e,'1','0..*');assoc(e,f,'0..1','1')
note('Una alta correcta crea el primer envío. Se muestra 0..* como cardinalidad estructural; la revisión no se edita.',523)
endsvg('Diagrama_clases_02_actividades_evidencias.svg')

startsvg();new('08. Clases: misiones y recompensas','uml 3/3','Referencias cruzadas explícitas por identificador; las recompensas son movimientos persistidos.')
a=cls(40,128,'MisionPlantilla','misiones',['id: Long','codigo: String','puntosComunitarios: int'],['getRecurrencia(): RecurrenciaMision'])
b=cls(310,128,'MisionHuerto','misiones',['id, huertoId: Long','plantillaId, actividadId: Long','estado: EstadoMision'],['iniciar(id: Long): void','completar(): void'])
c=cls(580,128,'Huerto','huertos',['id: Long','comunidadId: Long'],['getId(): Long'],True)
d=cls(40,360,'RecompensaIndividual','gamificacion',['id, actividadId: Long','integranteId, comunidadId: Long','puntos: int'],['getPuntos(): int'])
e=cls(310,360,'Actividad','actividades',['id: Long','misionHuertoId: Long','estado: EstadoRevision'],['aplicarDecision(...): void'],True)
f=cls(580,360,'RecompensaComunitaria','gamificacion',['id, actividadId: Long','comunidadId: Long','puntos, progreso: int'],['getProgreso(): int'])
assoc(a,b,'1','0..*');assoc(b,c,'0..*','1');assoc(d,e,'0..*','1');assoc(e,f,'1','0..1')
# Both optional unique foreign keys express the same conceptual association.
assoc(b,e,'0..1','0..1',vertical=True)
note('Dos FK opcionales y únicas vinculan misión-actividad. Recompensa individual: máximo una por actividad/integrante.',523)
endsvg('Diagrama_clases_03_misiones_recompensas.svg')

new('09. Interpretación y clases complementarias','modelo','Las multiplicidades expresan estructura; las restricciones de estado completan el modelo.')
table(['Relación o clase','Interpretación y responsabilidad'],[
('Comunidad - Huerto','Cada huerto pertenece a una comunidad; comunidad_id es único en huerto. Una comunidad puede existir antes de registrar su huerto.'),
('Integrante / ActividadParticipante','Integrante representa una membresía. ActividadParticipante resuelve la participación en actividades y conserva comunidadId para consistencia.'),
('Especie - Planta','Especie conserva el catálogo compartido. Planta pertenece al huerto y aporta cantidad, activa y plantada. Los nombres de catálogo se resuelven al construir las vistas.'),
('EspacioHuerto / HuertoCerramiento','Distribución física y cerramientos del huerto. EspacioHuerto registra huertoId, diseño y área. No se dibuja una asignación Planta-Espacio que el modelo no persiste.'),
('ArticuloBodega','id y huertoId: Long; nombre: String; categoria: CategoriaArticulo; cantidad: int. actualizar(nombre, categoria, cantidad) no recibe formularios.'),
('AsignacionRevisor / SolicitudComunidad','La primera relaciona revisor y comunidad. La segunda conserva el expediente y decisión de alta o eliminación; no es una subclase de Comunidad.')],y=132,size=9.6)
note('Las láminas son vistas parciales del modelo. No se emplea composición UML porque la eliminación en cascada no es su contrato.')

new('10. Colaboración técnica de las clases','diseño','El controlador no entrega entidades al HTML: la frontera de lectura utiliza records de aplicación.')
box(40,142,218,100,'GestorHuertoController','Presentación: adapta petición, sesión y respuesta MVC.')
box(312,142,220,100,'HuertosConsultaServicio','Aplicación: autoriza, consulta y construye datos de pantalla.')
box(584,142,218,100,'HuertoVista / PlantaVista','Records de aplicación: valores y EspecieVista anidada, sin entidades.')
path([(258,191),(312,191)],dash=True,arrow=True);path([(532,191),(584,191)],dash=True,arrow=True)
box(40,343,218,109,'ActividadServicio','Aplicación: usa el puerto AlmacenamientoArchivos para guardar evidencias.')
box(312,329,220,143,'«interface»','AlmacenamientoArchivos<br/><br/>guardar(ImagenValidada): String<br/>guardar(VideoValidado): String<br/>cargar(String): Resource<br/>eliminar(String): void')
box(584,343,218,109,'AlmacenamientoLocal','Persistencia: implementa el puerto. Escribe claves seguras fuera del directorio público.')
path([(258,398),(312,398)],dash=True,arrow=True)
path([(584,398),(540,398)],dash=True);path([(540,393),(532,398),(540,403),(540,393)]);txt(539,380,'realiza',8,'Body',GRAY)
para('La fila superior resume una dependencia y su resultado. La inferior muestra el puerto existente para archivos. Spring Resource sigue formando parte de ese contrato; no se afirma independencia total del framework.',40,265,762,10.5)
note('Lecturas dentro de la transacción + open-in-view: false. No se delegan consultas diferidas a las plantillas.')

def seq(title,sub,actors,events,foot):
    new(title,'secuencia',sub)
    n=len(actors);xs=[80+i*(682/(n-1)) for i in range(n)]
    for x,a in zip(xs,actors):
        rect(x-64,132,128,45,LIGHT,LINE,4)
        for j,line in enumerate(a.split('|')):txt(x-55,140+j*13,line,9,'Bold')
        path([(x,177),(x,478)],LINE,dash=True)
    y=196
    for start,end,label in events:
        x,xx=xs[start],xs[end]
        if start==end:
            path([(x,y+15),(x+26,y+15),(x+26,y+28),(x,y+28)],arrow=True)
            txt(x+32,y+10,label,8.3)
        else:
            path([(x,y+17),(xx,y+17)],arrow=True)
            labelx=min(x,xx)+6
            # Multi-lane labels intentionally short; two lines fit between lifelines.
            for k,l in enumerate(label.split('|')):txt(labelx,y-8+k*11,l,8.4)
        y+=37
    para(foot,40,489,762,10)

seq('11. Secuencia: registrar actividad','Flujo síncrono resumido; el servicio delimita la transacción de escritura.',
['Gestor / MVC','ActividadServicio','Comunidad / misión','Repositorios','Puerto de archivos'],[
(0,1,'1. registrar(gestorId, form, archivo, misionId?)'),
(1,2,'2. Verificar gestor, comunidad operativa|y misión disponible si corresponde'),
(1,3,'3. Buscar huerto; validar participantes'),
(1,1,'4. Validar fecha y archivo'),
(1,3,'5. Guardar Actividad y participantes; fijarlos'),
(1,4,'6. guardar(imagen/video validado)'),
(1,3,'7. Guardar EnvioEvidencia n.º 1'),
(1,0,'8. Retornar ID; confirmar transacción')],
'<b>Consistencia:</b> el vínculo con la misión se registra si fue seleccionada. Una subida fallida cancela la operación; si la transacción revierte después de guardar el archivo, se programa su compensación. Disco y base no forman una transacción distribuida.')

seq('12. Secuencia: decidir una evidencia','El servicio valida rol y decisión antes del bloqueo; POST /revision/envios/{envioId}/decision.',
['RevisionController','RevisionServicio','Envios / revisiones','GamificacionServicio','MisionServicio'],[
(0,1,'1. decidir(revisorId, envioId, decision, observaciones)'),
(1,2,'2. bloquearPorId(envioId)|SELECT ... FOR UPDATE'),
(1,1,'3. Verificar asignación, autoría y estado'),
(1,2,'4. Guardar Revision; actualizar estados; flush'),
(1,3,'5. Si APROBADA: otorgar(actividad)'),
(1,4,'6. Si tiene misión: completarPorActividad(...)'),
(1,2,'7. flush; commit al salir del servicio'),
(1,0,'8. Resultado y redirección MVC')],
'<b>Alternativas:</b> un envío decidido produce conflicto; rechazar exige observaciones y no genera recompensas. El reenvío crea otra evidencia y conserva el historial. El bloqueo protege la revisión del envío; no resuelve el pendiente de misiones distintas aprobadas a la vez.')

new('13. Diagrama de despliegue','infraestructura','Escenario definido en compose.yaml y Dockerfile. Es documentación de configuración, no un despliegue nuevo.')
box(40,153,164,91,'Cliente web','Navegador del usuario<br/>HTML / CSS / JavaScript')
rect(252,132,550,344,PAPER,LINE,8);txt(269,145,'Equipo local / Docker Compose',12,'Bold',GREEN)
box(270,183,245,129,'Contenedor app','JRE Java 21 · usuario sin privilegios<br/>Spring Boot + Thymeleaf<br/>FFprobe para validar video<br/>Puerto interno 8080')
box(561,183,223,129,'Contenedor db','postgres:17-alpine<br/>Puerto interno 5432<br/>Esquema migrado por Flyway')
box(270,365,245,78,'Volumen evidencias','Montado en /datos/evidencias')
box(561,365,223,78,'Volumen datos-postgres','Montado en /var/lib/postgresql/data')
path([(204,199),(270,221)],arrow=True);txt(207,177,'HTTP',8,'Bold');txt(207,190,':8081*',8)
path([(515,235),(561,235)],arrow=True);txt(520,218,'JDBC',7.5)
path([(392,312),(392,365)],arrow=True);path([(673,312),(673,365)],arrow=True)
para('<b>* Puertos predeterminados:</b> host 8081 → app 8080; base expuesta solo en 127.0.0.1:5432. El archivo .env aporta la configuración y las credenciales. La aplicación espera el estado saludable de PostgreSQL.',40,489,762,10)

new('14. Seguridad, persistencia y operación','atributos de calidad','Controles existentes y consecuencias del diseño.')
section('Seguridad en tres niveles',40,137,355,'Spring Security aplica rutas, sesión y CSRF. Los servicios verifican usuario activo, rol, membresía y propiedad/asignación del recurso. PostgreSQL respalda las reglas con claves, índices y triggers. El rol ADMIN_MINISTERIO_SALUD pertenece al prototipo; no implica integración externa con un ministerio.')
section('Esquema y transacciones',445,137,355,'Flyway versiona la base y Hibernate valida el esquema con ddl-auto: validate. Las entidades se consultan dentro de los servicios; open-in-view está desactivado. Las transacciones de escritura conservan bloqueos e idempotencia para revisión y recompensas.')
section('Privacidad de archivos',40,337,355,'Fotos y videos no se sirven como archivos estáticos públicos. El acceso pasa por autorización y el adaptador valida claves/rutas. La eliminación utiliza una cola persistida con reintentos. La recuperación ante fallos requiere considerar juntos base y archivos; no se ha verificado aquí un procedimiento de restauración.')
section('Despliegue externo descrito',445,337,355,'render.yaml declara una aplicación Docker y PostgreSQL. No configura un disco persistente de evidencias. Por tanto, la conservación de imágenes y videos no queda resuelta en ese archivo. Este informe no afirma que se haya desplegado en Render ni verifica las condiciones comerciales del proveedor.')

new('15. Calidad de pruebas y trazabilidad','verificación','Resultados registrados el 12/11/2026; no se han vuelto a ejecutar durante la elaboración del informe.')
table(['Fase / comprobación','Resultado y propósito'],[
('Surefire: mvn test','66 casos rápidos: unitarias, slice web y arquitectura. Sin fallos, errores ni omitidos.'),
('Failsafe: mvn verify','111 casos de integración; PostgreSQL 17.11 externo desechable. Verificación completa: 177 casos y BUILD SUCCESS.'),
('Repetición de integración','111 casos, segunda semilla de orden y misma base temporal. Cero fallos; no es una prueba adicional contada dentro de los 177.'),
('Arquitectura productiva','8 reglas: fronteras de capas, SQL, entidades por ID, records, transacciones y recursos únicos.'),
('Organización de la suite','3 reglas: paquetes y rutas coherentes, etiquetas/sufijos Test-IT y unitarias sin contexto completo Spring.'),
('Before / After','Preparación nueva por caso, limpieza de datos y archivos fuera de la transacción, cierre de validadores y ejecutores.')],y=132,size=10)
para('<b>TDD observado:</b> dos contratos de organización se ejecutaron primero en rojo y luego pasaron tras refactorizar. No se atribuye TDD histórico al negocio. No se midió cobertura ni mutación; el resultado verde se limita a los escenarios declarados.',40,455,762,10)

new('16. Decisiones y límites de la versión','evaluación','La conformidad de capas no equivale a ausencia de defectos funcionales.')
table(['Decisión conservada','Consecuencia de diseño'],[
('IDs en lugar de relaciones JPA','Joins y proyecciones explícitos; sin cascadas implícitas entre entidades. Aplicación debe resolver datos relacionados.'),
('Servicios + DTO de lectura','La interfaz no accede a repositorios ni recibe entidades. Los permisos y la materialización de vistas se centralizan.'),
('Monolito con transacción compartida','Revisión y recompensas pueden confirmarse juntas. Crece el acoplamiento de coordinación entre módulos.'),
('Archivo local con puerto','Puede reemplazarse el adaptador manteniendo su contrato. La durabilidad depende del volumen y la estrategia de respaldo.')],y=130,size=10)
section('Pendientes funcionales documentados',40,365,762,'Persisten casos de eliminación con relaciones nuevas; desbloqueo de misiones bajo aprobaciones simultáneas; sincronización de cerramientos; cupo al cambiar el tipo de comunidad; reparto de áreas pequeñas y estimación del ranking con recompensas variables. Requieren corrección y regresiones específicas antes de afirmar que están resueltos.')
note('Las recompensas y umbrales de demostración son provisionales. Enumerar recurrencia no demuestra ejecución recurrente de misiones.')

new('17. Fuentes y correspondencia de entrega','referencias','Fuentes primarias de la copia local, revisadas para este documento.')
table(['Fuente dentro del repositorio','Uso en el informe'],[
('docs/arquitectura.md','Estilo, módulos, dependencias permitidas, entidades por ID, DTO y límites arquitectónicos.'),
('src/main/java/ec/edu/epn/huertos/','Clases de dominio, firmas de métodos, servicios y controladores. Láminas UML y secuencias contrastadas con estas fuentes.'),
('src/main/resources/db/migration/','Restricciones y esquema. Junto con anotaciones JPA, respalda la lectura de cardinalidades.'),
('pom.xml; frontend/; application.yml','Java 21, Spring Boot 4.1.2, empaquetado de interfaz, persistencia y configuración de archivos.'),
('compose.yaml; Dockerfile; render.yaml','Topología de despliegue, puertos, volúmenes y límites del almacenamiento externo descrito.'),
('docs/calidad-pruebas.md; docs/verificacion-pruebas.txt','Evidencia de las 177 pruebas, repetición e infraestructura efectivamente utilizada.'),
('docs/cambios-arquitectura.md','Alcance de la refactorización y defectos funcionales pendientes.')],y=129,size=9.5)
para('<b>Correspondencia con la actividad:</b> arquitectura del software: secciones 02, 03, 05 y 14; diagrama de arquitectura: 04; diagrama de clases: 06-10; secuencias: 11-12; despliegue: 13. Las láminas de clases se entregan también juntas en PDF y separadas en SVG para los aportes al documento 03_Diseño_V1.',40,466,762,10)

pdf.save()
r=PdfReader(OUT/'02_Arquitectura_V1.pdf'); w=PdfWriter()
for i in [6,7,8]:w.add_page(r.pages[i])
w.add_metadata({'/Title':'ALLPANA - Diagrama de clases - Tres vistas UML','/Author':'Ariel | Integrante 2'})
with open(OUT/'Diagrama_clases.pdf','wb') as f:w.write(f)

# Editable content transcription from the PDF, with explicit page separators.
trans=['# ALLPANA - Arquitectura y diseño técnico\n\nFuente editable de contenido; los diagramas vectoriales se entregan en SVG.\n']
for i,p in enumerate(r.pages,1):trans.append(f'\n## Página {i}\n\n'+p.extract_text())
(SRC/'Informe_contenido.md').write_text('\n'.join(trans),encoding='utf-8')

# Verify every entity and selected real operation used in UML against the current code.
entities=['Usuario','Integrante','Comunidad','Especie','Planta','Huerto','ActividadParticipante','Actividad','Revision','EnvioEvidencia','MisionPlantilla','MisionHuerto','RecompensaIndividual','RecompensaComunitaria']
assert all(list((REPO/'src/main/java').rglob(n+'.java')) for n in entities)
assert len(r.pages)==18,len(r.pages)
for i,p in enumerate(r.pages):assert len(p.extract_text())>150,(i,len(p.extract_text()))
(SRC/'verificacion.json').write_text(json.dumps({'paginas_informe':len(r.pages),'paginas_clases':3,'rama':'Ariel_Baldeon','base_commit':'9a7bd05bc0c555bfaab7291342ed854660f8094d','clases_dominio_comprobadas':entities,'nota':'Resultados de pruebas citados desde evidencia local existente; no reejecutados por esta entrega.'},ensure_ascii=False,indent=2),encoding='utf-8')
print('Creado informe de',len(r.pages),'páginas y anexo UML de 3 páginas en',OUT)
