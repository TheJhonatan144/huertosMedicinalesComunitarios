package ec.edu.epn.huertos.comunidades;

import ec.edu.epn.huertos.huertos.FormularioHuerto;

/** Alta comunitaria que incluye los datos completos del primer huerto. */
public class FormularioAltaComunidad extends FormularioHuerto {
    /** Provincia, cantón y dirección del huerto también ubican la comunidad en el alta inicial. */
    public String getCanton() { return getCantonComunidad(); }
    public void setCanton(String v) { setCantonComunidad(v); }

    @Override
    public void setProvincia(String v) {
        super.setProvincia(v);
        setProvinciaComunidad(v);
    }

    @Override
    public void setDireccion(String v) {
        super.setDireccion(v);
        setDireccionComunidad(v);
    }

    @Override
    public void setLatitud(Double v) {
        super.setLatitud(v);
        setLatitudComunidad(v);
    }

    @Override
    public void setLongitud(Double v) {
        super.setLongitud(v);
        setLongitudComunidad(v);
    }
}
