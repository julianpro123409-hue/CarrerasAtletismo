# Carreras Atletismo

Aplicación Android escrita en **Kotlin** con **Jetpack Compose** para consultar maratones de todo el mundo, registrar corredores, inscribirlos en carreras con su dorsal y tiempo objetivo, y capturar su tiempo final para obtener la clasificación.

Proyecto de la carrera de Desarrollo de Software Multiplataforma, Universidad Tecnológica de Tula-Tepeji (grupo 4DSM-G2).

**Autores:** Jesús Gael Mata Jiménez y Julio Cesar Lugo Rodríguez

## Funciones (primera etapa)

- Descarga de las próximas maratones desde la API pública de [RunDida](https://rundida.com) (Retrofit + Gson).
- Almacenamiento local con Room (SQLite) para usar la app sin conexión.
- Detalle de cada carrera: fecha, ciudad, país, clima promedio y tipo de recorrido.
- Alta y baja de corredores; la categoría (Juvenil, Libre, Máster) se asigna según la edad.
- Inscripción de corredores con dorsal único por carrera y tiempo objetivo.
- Registro del tiempo final y cálculo automático de la posición.

## Arquitectura

```
ui/        → MainActivity, pantallas Compose y CarrerasViewModel
domain/    → modelos de dominio e interfaz CarrerasRepository
data/      → Room (entidades, vistas, DAO, migraciones), Retrofit (API y DTO), mappers
di/        → ContenedorApp: inyección de dependencias manual
```

## Tecnologías

| Herramienta | Versión |
|---|---|
| Kotlin | 2.3.20 |
| Android Gradle Plugin | 9.2.0 |
| Compose BOM | 2024.12.01 |
| Room | 2.7.2 (KSP) |
| Retrofit / Gson | 2.11.0 |
| Lifecycle (ViewModel) | 2.8.7 |
| minSdk / targetSdk | 24 / 36 |

## Cómo ejecutarlo

1. Clonar el repositorio: `git clone https://github.com/jesusgaelmata/CarrerasAtletismo.git`
2. Abrir la carpeta en Android Studio y esperar a que sincronice Gradle.
3. Ejecutar en un emulador o dispositivo con Android 7.0 (API 24) o superior.

Datos de maratones: RunDida (rundida.com).
