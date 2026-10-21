# MediSalud - Sistema de Gestión de Citas Médicas

Proyecto final del curso de Java con la funcionalidad adicional de **Cancelación de citas con motivo**.

## Estructura del proyecto

```
src/main/java/com/medisalud/
  entity/          EstadoCita, HistoriaClinica, Persona, Paciente, Medico, Cita,
                   Factura, FacturaConRecargoNocturno, FacturaConDescuentoAfiliado,
                   ConstructorHistoriaClinica
  exception/       PacienteNoEncontradoException, MedicoNoEncontradoException,
                   CitaNoEncontradaException, TransicionInvalidaException,
                   CancelacionTardiaException
  repository/      RepositorioPacientes, RepositorioMedicos, RepositorioCitas,
                   RepositorioPacientesMemoria, RepositorioMedicosMemoria,
                   RepositorioCitasMemoria, GestorClinica
  service/         EstrategiaCosto, EstrategiaCostoConsultaGeneral,
                   EstrategiaCostoConsultaEspecialista, ServicioPacientes,
                   ServicioMedicos, ServicioCitas, ServicioFacturacion,
                   FachadaAgendamiento
  notificacion/    ObservadorCita, ObservadorCitaNotificacion, HiloNotificaciones
  util/            ContadorCodigos
  persistencia/    archivo (AlmacenPacientesArchivo, AlmacenCitasSerializado)
                   jdbc (ConexionBD, PacienteDAO, MedicoDAO, CitaDAO)
  controlador/     ControladorMediSalud
  vista/           VistaConsola
  Principal.java
sql/
  medisalud.sql            Script de creación de tablas y datos de ejemplo
  002-motivo-cancelacion.sql  Migración: agrega columna motivo_cancelacion
docker-compose.yml         Servidor MySQL con Docker
lib/                       mysql-connector-j-8.3.0.jar (descargar por separado)
```

## Cómo compilar y ejecutar

### 1. Levantar MySQL con Docker Compose

```bash
docker compose up -d
```

Esperar a que el contenedor esté `running (healthy)`.

### 2. Crear la base de datos y las tablas

```bash
# Primera instalación (crea todo incluyendo motivo_cancelacion)
Get-Content sql/medisalud.sql | docker compose exec -T mysql mysql -uroot -proot

# Si ya tenías la base anterior, aplicar la migración:
Get-Content sql/002-motivo-cancelacion.sql | docker compose exec -T mysql mysql -uroot -proot
```

### 3. Descargar el conector MySQL

Descargar `mysql-connector-j-8.3.0.jar` de Maven Central y guardarlo en `lib/`.

### 4. Compilar

```powershell
javac -d out $(Get-ChildItem -Recurse -Filter *.java src/main/java).FullName
```

### 5. Ejecutar

```powershell
java -cp "out;lib/mysql-connector-j-8.3.0.jar" com.medisalud.Principal
```

## Funcionalidades

1. Registrar paciente (con Builder para historia clínica opcional)
2. Registrar médico
3. Agendar cita (Facade, State para transiciones, Observer para notificaciones)
4. Consultar historia clínica
5. Facturar consulta (Strategy para costo, Decorator para recargo/descuento)
6. Generar reporte (citas por médico, total facturado, cancelaciones por médico, pacientes por edad con Stream API)
7. **Cancelar cita con motivo** (nuevo)
8. Salir

## Patrones aplicados

- MVC (Modelo-Vista-Controlador)
- Inversión de dependencias (interfaces de repositorio)
- Builder (ConstructorHistoriaClinica)
- Singleton (GestorClinica)
- Observer (ObservadorCita / ObservadorCitaNotificacion)
- State (EstadoCita.puedeTransicionarA)
- Strategy (EstrategiaCosto)
- Decorator (FacturaConRecargoNocturno, FacturaConDescuentoAfiliado)
- Facade (FachadaAgendamiento)
- Concurrencia (HiloNotificaciones, AtomicLong en ContadorCodigos)
- Stream API (listarMayoresDeEdad, totalFacturadoPorMedico, generarReporteCancelacionesPorMedico)
