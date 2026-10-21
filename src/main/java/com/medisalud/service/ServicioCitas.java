package com.medisalud.service;

import com.medisalud.entity.Cita;
import com.medisalud.entity.EstadoCita;
import com.medisalud.entity.Medico;
import com.medisalud.entity.Paciente;
import com.medisalud.exception.CancelacionTardiaException;
import com.medisalud.exception.CitaNoEncontradaException;
import com.medisalud.exception.TransicionInvalidaException;
import com.medisalud.repository.RepositorioCitas;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServicioCitas {

    private final RepositorioCitas repositorioCitas;

    public ServicioCitas(RepositorioCitas repositorioCitas) {
        this.repositorioCitas = repositorioCitas;
    }

    public Cita agendarCita(String codigo, Paciente paciente, Medico medico, LocalDate fecha) {
        Cita cita = new Cita(codigo, paciente, medico, fecha);
        repositorioCitas.guardar(cita);
        return cita;
    }

    public Cita buscarPorCodigo(String codigo) throws CitaNoEncontradaException {
        Cita cita = repositorioCitas.buscarPorCodigo(codigo);
        if (cita == null) {
            throw new CitaNoEncontradaException(codigo);
        }
        return cita;
    }

    public void confirmarCita(String codigo) throws CitaNoEncontradaException, TransicionInvalidaException {
        cambiarEstado(codigo, EstadoCita.CONFIRMADA);
    }

    public void atenderCita(String codigo, String diagnostico)
            throws CitaNoEncontradaException, TransicionInvalidaException {
        Cita cita = cambiarEstado(codigo, EstadoCita.ATENDIDA);
        cita.getPaciente().getHistoriaClinica().agregarConsulta(diagnostico);
    }

    /**
     * Cancela una cita con motivo. Aplica RN1-RN4:
     * RN1: motivo obligatorio (validado en Cita.cancelarConMotivo).
     * RN2: solo PENDIENTE o CONFIRMADA (reutiliza EstadoCita.puedeTransicionarA).
     * RN3: no se cancela si la fecha ya paso (CancelacionTardiaException).
     * RN4: si no existe, lanza CitaNoEncontradaException.
     */
    public void cancelarCita(String codigo, String motivo)
            throws CitaNoEncontradaException, TransicionInvalidaException, CancelacionTardiaException {
        Cita cita = buscarPorCodigo(codigo);

        if (cita.getFecha().isBefore(LocalDate.now())) {
            throw new CancelacionTardiaException(codigo, cita.getFecha());
        }

        if (!cita.getEstado().puedeTransicionarA(EstadoCita.CANCELADA)) {
            throw new TransicionInvalidaException(codigo, cita.getEstado(), EstadoCita.CANCELADA);
        }

        cita.cancelarConMotivo(motivo);
        cita.cambiarEstado(EstadoCita.CANCELADA);
        repositorioCitas.guardar(cita);
    }

    public List<Cita> listarPorMedico(String codigoMedico) {
        return repositorioCitas.listarPorMedico(codigoMedico);
    }

    public List<Cita> listarTodas() {
        return repositorioCitas.listarTodas();
    }

    /**
     * Reporte de cancelaciones por médico: agrupa citas CANCELADA por código de médico,
     * cuenta cuántas hay por cada uno y ordena de mayor a menor cantidad.
     * Usa Stream API (filter + groupingBy + counting), sin bucles manuales de conteo.
     */
    public Map<String, Long> generarReporteCancelacionesPorMedico() {
        return listarTodas().stream()
                .filter(cita -> cita.getEstado() == EstadoCita.CANCELADA)
                .collect(Collectors.groupingBy(
                        cita -> cita.getMedico().getCodigo(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    private Cita cambiarEstado(String codigo, EstadoCita nuevoEstado)
            throws CitaNoEncontradaException, TransicionInvalidaException {
        Cita cita = buscarPorCodigo(codigo);
        if (!cita.getEstado().puedeTransicionarA(nuevoEstado)) {
            throw new TransicionInvalidaException(codigo, cita.getEstado(), nuevoEstado);
        }
        cita.cambiarEstado(nuevoEstado);
        repositorioCitas.guardar(cita);
        return cita;
    }
}
