# MediSalud: sistema de gestión de citas médicas

Aplicación de consola en Java para administrar pacientes, médicos, citas, historias clínicas y facturación, con MySQL en Docker. Proyecto final del curso de Java.

## Funcionalidades

- Registro de pacientes y médicos.
- Agendamiento de citas con control de estados.
- Cancelación de citas con motivo.
- Consulta de historia clínica.
- Facturación con recargo nocturno o descuento para afiliados.
- Reportes de citas, facturación y cancelaciones por médico, y de pacientes por edad.

## Tecnologías

Java, MySQL, JDBC, Docker Compose, Stream API y concurrencia.

## Arquitectura y patrones

| Patrón | Implementación |
|---|---|
| MVC | Paquetes `entity`, `vista` y `controlador` |
| Inversión de dependencias | Interfaces de repositorio |
| Builder | `ConstructorHistoriaClinica` |
| Singleton | `GestorClinica` |
| Observer | `ObservadorCita`, `ObservadorCitaNotificacion` |
| State | Transiciones validadas en `EstadoCita` |
| Strategy | `EstrategiaCosto` |
| Decorator | `FacturaConRecargoNocturno`, `FacturaConDescuentoAfiliado` |
| Facade | `FachadaAgendamiento` |
| Concurrencia | `HiloNotificaciones` y `AtomicLong` en `ContadorCodigos` |

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

Las credenciales `root` / `root` son solo para el contenedor local de desarrollo.

## Autor

Juan José Acevedo Morales, desarrollador de software junior.
[Portafolio](https://juanacevedo124.github.io/Portafolio/) | [GitHub](https://github.com/JuanAcevedo124) | [LinkedIn](https://www.linkedin.com/in/juan-acevedo-b24759450/)
