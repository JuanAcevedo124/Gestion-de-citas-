package com.medisalud.exception;

public class CancelacionTardiaException extends Exception {

    public CancelacionTardiaException(String codigoCita, java.time.LocalDate fechaCita) {
        super("No se puede cancelar la cita " + codigoCita + " porque su fecha (" + fechaCita
                + ") ya paso. Solo se pueden cancelar citas con fecha futura.");
    }
}
