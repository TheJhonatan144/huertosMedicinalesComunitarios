# Fuentes del informe

- `../02_Arquitectura_V1.pdf`: informe principal, 18 páginas con marcadores de navegación.
- `../Diagrama_clases.pdf`: tres láminas vectoriales extraídas del informe.
- `../Diagrama_clases_*.svg`: láminas editables en Inkscape u otro editor vectorial.
- `../Diagrama_clases-*.png`: las mismas láminas a 180 dpi para presentaciones.
- `Informe_contenido.md`: transcripción editable del contenido textual del PDF; los diagramas se editan en SVG o en el generador.
- `crear_informe_arquitectura.py`: generador reproducible con ReportLab y pypdf. Localiza el repositorio desde su ubicación en docs/informes/fuentes. Requiere las fuentes Segoe UI y Consolas de Windows.
- `verificacion.json`: identificación de versión y clases representadas.

Para regenerar desde la raíz del repositorio, con Python que tenga instalados reportlab y pypdf:

```powershell
python docs/informes/fuentes/crear_informe_arquitectura.py
```

El generador vuelve a escribir ambos PDF, los SVG, la transcripción y el resumen de verificación. Los PNG se producen por separado con Poppler:

```powershell
pdftoppm -r 180 -png docs/informes/Diagrama_clases.pdf docs/informes/Diagrama_clases
```

La versión documentada corresponde al estado local previo a su publicación en Ariel_Baldeon, sobre la base de Develop 9a7bd05. Las menciones «sin commit ni push» en el informe y la evidencia describen ese corte histórico. Los resultados de pruebas citados proceden de la verificación local anterior; no se reejecutaron al elaborar el informe. La elaboración documental no modificó el código de la aplicación.
