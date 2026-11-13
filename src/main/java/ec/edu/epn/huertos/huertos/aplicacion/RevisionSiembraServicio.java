package ec.edu.epn.huertos.huertos.aplicacion;

import ec.edu.epn.huertos.autenticacion.aplicacion.AutorizacionServicio;
import ec.edu.epn.huertos.autenticacion.dominio.Rol;
import ec.edu.epn.huertos.compartido.dominio.ConflictoException;
import ec.edu.epn.huertos.compartido.dominio.RecursoNoEncontradoException;
import ec.edu.epn.huertos.compartido.dominio.ReglaNegocioException;
import ec.edu.epn.huertos.comunidades.aplicacion.ComunidadServicio;
import ec.edu.epn.huertos.evidencias.dominio.EstadoRevision;
import ec.edu.epn.huertos.huertos.dominio.Planta;
import ec.edu.epn.huertos.huertos.persistencia.EspecieRepository;
import ec.edu.epn.huertos.huertos.persistencia.HuertoRepository;
import ec.edu.epn.huertos.huertos.persistencia.PlantaRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RevisionSiembraServicio {
    private final AutorizacionServicio autorizacion;
    private final ComunidadServicio comunidades;
    private final PlantaRepository plantas;
    private final HuertoRepository huertos;
    private final EspecieRepository especies;

    public RevisionSiembraServicio(
            AutorizacionServicio autorizacion,
            ComunidadServicio comunidades,
            PlantaRepository plantas,
            HuertoRepository huertos,
            EspecieRepository especies) {
        this.autorizacion = autorizacion;
        this.comunidades = comunidades;
        this.plantas = plantas;
        this.huertos = huertos;
        this.especies = especies;
    }

    public record Item(Long id, String planta, String comunidad, int cantidad, String sembradaEn) {}

    @Transactional(readOnly = true)
    public List<Item> pendientes(Long revisorId) {
        autorizacion.exigirRol(revisorId, Rol.ADMIN_MINISTERIO_SALUD);
        return plantas.siembrasPendientes().stream()
                .filter(
                        p -> {
                            var huerto = huertos.findById(p.getHuertoId()).orElseThrow();
                            return comunidades.revisorAsignado(revisorId, huerto.getComunidadId());
                        })
                .map(
                        p -> {
                            var huerto = huertos.findById(p.getHuertoId()).orElseThrow();
                            return new Item(
                                    p.getId(),
                                    especies.findById(p.getEspecieId())
                                            .map(e -> e.getNombreComun())
                                            .orElse(p.getNombreComun()),
                                    comunidades.comunidad(huerto.getComunidadId()).getNombre(),
                                    p.getCantidad(),
                                    p.getPlantadaEn() == null
                                            ? ""
                                            : DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                                    .withZone(ZoneId.systemDefault())
                                                    .format(p.getPlantadaEn()));
                        })
                .toList();
    }

    @Transactional
    public void decidir(
            Long revisorId, Long plantaId, EstadoRevision decision, String observaciones) {
        autorizacion.exigirRol(revisorId, Rol.ADMIN_MINISTERIO_SALUD);
        if (decision != EstadoRevision.APROBADA && decision != EstadoRevision.RECHAZADA)
            throw new ReglaNegocioException("Selecciona aprobar o rechazar la siembra.");
        Planta planta =
                plantas.bloquearPorId(plantaId)
                        .orElseThrow(
                                () -> new RecursoNoEncontradoException("La planta no existe."));
        var huerto =
                huertos.findById(planta.getHuertoId())
                        .orElseThrow(
                                () -> new RecursoNoEncontradoException("El huerto no existe."));
        if (!comunidades.revisorAsignado(revisorId, huerto.getComunidadId()))
            throw new AccessDeniedException("La comunidad no está asignada a este revisor.");
        if (decision == EstadoRevision.RECHAZADA
                && (observaciones == null || observaciones.isBlank()))
            throw new ReglaNegocioException("Escribe el motivo del rechazo.");
        if (planta.getEstadoRevision() != EstadoRevision.PENDIENTE)
            throw new ConflictoException("Esta siembra ya fue revisada.");
        planta.aplicarRevision(decision, observaciones);
    }
}
