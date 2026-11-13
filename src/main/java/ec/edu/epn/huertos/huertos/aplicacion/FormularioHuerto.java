package ec.edu.epn.huertos.huertos.aplicacion;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class FormularioHuerto {

    @NotBlank(message = "Indica el nombre de la comunidad.")
    @Size(max = 120)
    private String nombreComunidad;
    @NotBlank(message = "Indica a quién representa la comunidad.")
    @Pattern(regexp = "PERSONA|FAMILIA|CONDOMINIO|BARRIO", message = "Selecciona un tipo de comunidad válido.")
    private String tipoComunidad;
    @NotBlank(message = "Escribe una descripción de la comunidad.")
    @Size(max = 2000)
    private String descripcionComunidad;
    @NotBlank(message = "Indica la provincia de la comunidad.")
    @Size(max = 60)
    private String provinciaComunidad;
    @NotBlank(message = "Indica el cantón de la comunidad.")
    @Size(max = 60)
    private String cantonComunidad;
    @Size(max = 250)
    private String direccionComunidad;
    @DecimalMin(value = "-5.1", message = "La latitud está fuera de Ecuador.")
    @DecimalMax(value = "1.8", message = "La latitud está fuera de Ecuador.")
    @NotNull(message = "Indica la latitud de la comunidad.")
    private Double latitudComunidad;
    @DecimalMin(value = "-92.5", message = "La longitud está fuera de Ecuador.")
    @DecimalMax(value = "-75.0", message = "La longitud está fuera de Ecuador.")
    @NotNull(message = "Indica la longitud de la comunidad.")
    private Double longitudComunidad;

    @NotBlank(message = "Indica el nombre del huerto.")
    @Size(max = 120, message = "El nombre admite hasta 120 caracteres.")
    private String nombre;

    @NotBlank(message = "Escribe una descripción del huerto.")
    @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres.")
    private String descripcion;

    @NotBlank(message = "Indica la provincia del huerto.")
    @Size(max = 60)
    private String provincia;

    @NotBlank(message = "Indica la parroquia.")
    @Size(max = 100)
    private String parroquia;

    @NotBlank(message = "Indica la dirección o referencia.")
    @Size(max = 250)
    private String direccion;

    @DecimalMin(value = "0.01", message = "El área debe ser mayor que cero.")
    @DecimalMax(value = "1000000", message = "El área supera el máximo permitido.")
    @NotNull(message = "Indica el área del huerto.")
    private java.math.BigDecimal areaM2;

    @NotBlank(message = "Selecciona el diseño del huerto.")
    @Pattern(regexp = "TIERRA_SURCOS|TIERRA_CAMADAS|TIERRA_CAMELLONES|TIERRA_TERRAZAS|VERTICAL_PAREDES|COLGANTE_TECHOS|PISO_MACETAS|PISO_BOTELLAS_PATIO|PISO_LLANTAS_PATIO|PISO_TERRAZA|PISO_GRADAS|PISO_BALCONES|PISO_FILOS_VENTANA", message = "Selecciona un diseño válido.")
    private String diseno;

    @NotEmpty(message = "Selecciona al menos un espacio para el huerto.")
    private List<String> disenos = new ArrayList<>();

    @NotBlank(message = "Selecciona el tipo de suelo.")
    private String tipoSuelo;

    @NotBlank(message = "Indica la fuente de agua.")
    @Size(max = 120)
    private String fuenteAgua;

    @NotBlank(message = "Selecciona el abono natural utilizado.")
    @Pattern(regexp = "BARBECHO|RESTOS_VEGETALES|HOJA|COMPOST", message = "Selecciona un abono válido.")
    private String abonoNatural;

    @Size(max = 5, message = "Selecciona hasta cinco tipos de cerca.")
    private List<String> gruposCerramiento = new ArrayList<>();

    // Solo conserva el valor anterior al modelo de varios grupos mientras se resuelven solicitudes pendientes.
    private String cerramiento;

    @DecimalMin(value = "-5.1", message = "La latitud está fuera de Ecuador.")
    @DecimalMax(value = "1.8", message = "La latitud está fuera de Ecuador.")
    @NotNull(message = "Indica la latitud del huerto.")
    private Double latitud;

    @DecimalMin(value = "-92.5", message = "La longitud está fuera de Ecuador.")
    @DecimalMax(value = "-75.0", message = "La longitud está fuera de Ecuador.")
    @NotNull(message = "Indica la longitud del huerto.")
    private Double longitud;

    public static FormularioHuerto de(HuertoVista h, ec.edu.epn.huertos.comunidades.aplicacion.ComunidadVista c) {
        return de(h, c, List.of(h.getDiseno()), List.of());
    }

    public static FormularioHuerto de(HuertoVista h, ec.edu.epn.huertos.comunidades.aplicacion.ComunidadVista c, List<String> disenosGuardados) {
        return de(h, c, disenosGuardados, List.of());
    }

    public static FormularioHuerto de(HuertoVista h, ec.edu.epn.huertos.comunidades.aplicacion.ComunidadVista c,
                                      List<String> disenosGuardados, List<String> gruposCerramiento) {
        FormularioHuerto f = new FormularioHuerto();
        f.nombreComunidad = c.getNombre(); f.descripcionComunidad = c.getDescripcion();
        f.tipoComunidad = c.getTipoComunidad(); f.provinciaComunidad = c.getProvincia();
        f.cantonComunidad = c.getCanton(); f.direccionComunidad = c.getDireccionExacta();
        f.latitudComunidad = c.getLatitud(); f.longitudComunidad = c.getLongitud();
        f.nombre = h.getNombre();
        f.descripcion = h.getDescripcion();
        f.provincia = h.getProvincia(); f.parroquia = h.getParroquia(); f.direccion = h.getDireccion();
        f.areaM2 = h.getAreaM2(); f.diseno = h.getDiseno(); f.tipoSuelo = h.getTipoSuelo();
        f.setDisenos(disenosGuardados == null || disenosGuardados.isEmpty() ? List.of(h.getDiseno()) : disenosGuardados);
        f.fuenteAgua = h.getFuenteAgua(); f.abonoNatural = h.getAbonoNatural(); f.cerramiento = h.getCerramiento();
        f.setGruposCerramiento(gruposCerramiento);
        f.latitud = h.getLatitud(); f.longitud = h.getLongitud();
        return f;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreComunidad() { return nombreComunidad; }
    public void setNombreComunidad(String v) { nombreComunidad = v; }
    public String getTipoComunidad() { return tipoComunidad; }
    public void setTipoComunidad(String v) { tipoComunidad = v; }
    public String getDescripcionComunidad() { return descripcionComunidad; }
    public void setDescripcionComunidad(String v) { descripcionComunidad = v; }
    public String getProvinciaComunidad() { return provinciaComunidad; }
    public void setProvinciaComunidad(String v) { provinciaComunidad = v; }
    public String getCantonComunidad() { return cantonComunidad; }
    public void setCantonComunidad(String v) { cantonComunidad = v; }
    public String getDireccionComunidad() { return direccionComunidad; }
    public void setDireccionComunidad(String v) { direccionComunidad = v; }
    public Double getLatitudComunidad() { return latitudComunidad; }
    public void setLatitudComunidad(Double v) { latitudComunidad = v; }
    public Double getLongitudComunidad() { return longitudComunidad; }
    public void setLongitudComunidad(Double v) { longitudComunidad = v; }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getProvincia() { return provincia; } public void setProvincia(String v) { provincia = v; }
    public String getParroquia() { return parroquia; } public void setParroquia(String v) { parroquia = v; }
    public String getDireccion() { return direccion; } public void setDireccion(String v) { direccion = v; }
    public java.math.BigDecimal getAreaM2() { return areaM2; } public void setAreaM2(java.math.BigDecimal v) { areaM2 = v; }
    public String getDiseno() { return diseno; }
    public void setDiseno(String v) {
        diseno = v;
        if ((disenos == null || disenos.isEmpty()) && v != null && !v.isBlank()) disenos = new ArrayList<>(List.of(v));
    }
    public List<String> getDisenos() { return disenos; }
    public void setDisenos(List<String> v) {
        disenos = v == null ? new ArrayList<>() : new ArrayList<>(v);
        diseno = disenos.isEmpty() ? "" : disenos.getFirst();
    }
    public String getTipoSuelo() { return tipoSuelo; } public void setTipoSuelo(String v) { tipoSuelo = v; }
    public String getFuenteAgua() { return fuenteAgua; } public void setFuenteAgua(String v) { fuenteAgua = v; }
    public String getAbonoNatural() { return abonoNatural; } public void setAbonoNatural(String v) { abonoNatural = v; }
    public String getCerramiento() { return cerramiento; } public void setCerramiento(String v) { cerramiento = v; }
    public List<String> getGruposCerramiento() { return gruposCerramiento; }
    public void setGruposCerramiento(List<String> v) { gruposCerramiento = v == null ? new ArrayList<>() : new ArrayList<>(v); }
    public Double getLatitud() { return latitud; } public void setLatitud(Double v) { latitud = v; }
    public Double getLongitud() { return longitud; } public void setLongitud(Double v) { longitud = v; }
}
