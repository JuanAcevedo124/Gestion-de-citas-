# ENTREGA.md

## Lista de archivos creados y modificados

| Archivo | Acción | Por qué |
|---------|--------|---------|
| sql/002-motivo-cancelacion.sql | Creado | Migración que agrega columna `motivo_cancelacion VARCHAR(200) NULL` a la tabla `citas` sin borrar datos (R1) |
| sql/medisalud.sql | Modificado | Agregada columna `motivo_cancelacion` al `CREATE TABLE` para instalaciones nuevas (R1) |
| src/main/java/com/medisalud/entity/Cita.java | Modificado | Agregado campo `motivoCancelacion`, getter `getMotivoCancelacion()`, setter package-private `setMotivoCancelacion()`, y método `cancelarConMotivo(String)` que valida RN1 (R3) |
| src/main/java/com/medisalud/exception/CancelacionTardiaException.java | Creado | Excepción propia para RN3: cita con fecha pasada no se puede cancelar (R3) |
| src/main/java/com/medisalud/service/ServicioCitas.java | Modificado | Reemplazado `cancelarCita(String)` por `cancelarCita(String, String)` aplicando RN1-RN4; agregado `listarTodas()` y `generarReporteCancelacionesPorMedico()` con Stream API (R3, R4) |
| src/main/java/com/medisalud/persistencia/jdbc/CitaDAO.java | Modificado | `guardar()` persiste motivo en INSERT y ON DUPLICATE KEY UPDATE; `mapearCita()` lee y restaura el motivo sin disparar notificaciones (R2) |
| src/main/java/com/medisalud/controlador/ControladorMediSalud.java | Modificado | Agregado `cancelarCita(String, String)` y `generarReporteCancelacionesPorMedico()` que delegan al servicio (R4) |
| src/main/java/com/medisalud/vista/VistaConsola.java | Modificado | Agregada opción 7 "Cancelar cita", Salir pasado a opción 8; reporte con columna Motivo y sección Cancelaciones por médico (R4) |
| README.md | Creado | Instrucciones de compilación y ejecución |
| ENTREGA.md | Creado | Este documento |

## Decisión sobre cancelarCita(String)

El método `cancelarCita(String codigo)` anterior fue **eliminado** y reemplazado por `cancelarCita(String codigo, String motivo)`. Motivo: la nueva funcionalidad requiere obligatoriamente un motivo (RN1), por lo que no tiene sentido mantener una versión sin motivo que dejaría citas canceladas sin justificación, violando el requisito de negocio. El nuevo método es la única entrada para cancelar citas.

## Dónde se aplicó cada concepto

| Concepto | Archivo y método |
|----------|------------------|
| **State** | `CitaDAO.mapearCita` usa `EstadoCita.valueOf(...)`; `ServicioCitas.cancelarCita` usa `cita.getEstado().puedeTransicionarA(EstadoCita.CANCELADA)` para validar RN2 (reutiliza `EstadoCita.puedeTransicionarA`) |
| **Observer** | `Cita.cambiarEstado` sigue notificando a observadores; `ObservadorCitaNotificacion` encola en `HiloNotificaciones`; al cancelar desde el menú se imprime la notificación en segundo plano (RN5) |
| **Excepción propia** | `CancelacionTardiaException` en `exception/CancelacionTardiaException.java`, lanzada por `ServicioCitas.cancelarCita` cuando la fecha ya pasó (RN3) |
| **PreparedStatement** | `CitaDAO.guardar` usa `setString` para parámetros (código, paciente, médico, fecha, estado, motivo) tanto en INSERT como en UPDATE (R2) |
| **Stream API** | `ServicioCitas.generarReporteCancelacionesPorMedico` usa `filter` + `Collectors.groupingBy` + `Collectors.counting` + `sorted` para contar cancelaciones por médico de mayor a menor (R4) |

## Resultado de los 12 casos de prueba

| Caso | Descripción | Resultado |
|------|-------------|-----------|
| 1 | T01, motivo "Paciente viaja" → CANCELADA con motivo, notificación en segundo plano | ✅ |
| 2 | T02, motivo "Medico en congreso" → CANCELADA con motivo | ✅ |
| 3 | T03 (ATENDIDA), motivo "Error" → TransicionInvalidaException, no cambia | ✅ |
| 4 | T01 otra vez (ya CANCELADA), motivo "Otro" → TransicionInvalidaException, no sobrescribe | ✅ |
| 5 | TPASADA (fecha ayer), motivo "Tarde" → CancelacionTardiaException con código y fecha | ✅ |
| 6 | ZZZ (inexistente), motivo "X" → CitaNoEncontradaException, menú sigue | ✅ |
| 7 | T05, motivo vacío → IllegalArgumentException "motivo obligatorio", sigue PENDIENTE | ✅ |
| 8 | T05, motivo 201 caracteres → IllegalArgumentException "máximo 200", sigue PENDIENTE | ✅ |
| 9 | Cerrar (opción 8), reabrir, reporte M009 → T01 y T02 siguen CANCELADA con motivos (persistió) | ✅ |
| 10 | Reporte M010 → C004 muestra "Sin motivo registrado", sin NullPointerException | ✅ |
| 11 | Opción 6 → sección cancelaciones por médico, conteo correcto ordenado desc | ✅ |
| 12 | Opción 8 → hilo se detiene ordenadamente, programa termina | ✅ |

## Riesgo o mejora

Un riesgo es que si dos usuarios cancelan la misma cita concurrentemente, el último `guardar` sobrescribe el motivo del primero sin detectar conflicto. Una mejora sería agregar una columna de versión o timestamp en la tabla `citas` para detectar actualizaciones perdidas (optimistic locking) antes de persistir la cancelación.
