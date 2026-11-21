package com.julio.carrerasatletismo.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

fun sembrarCategorias(db: SupportSQLiteDatabase) {
    db.execSQL(
        """INSERT OR IGNORE INTO categoria (nombre, edadMin, edadMax) VALUES
           ('Juvenil', 0, 17), ('Libre', 18, 39), ('Máster', 40, 999)"""
    )
}

/**
 * Versión 1 -> 2:
 *  - Normaliza país y ciudad en sus propias tablas.
 *  - Mueve la categoría del corredor a la tabla CATEGORIA.
 *  - Dorsal único por carrera (renumera duplicados si los hubiera).
 *  - Agrega la tabla RESULTADO.
 * Conserva todas las carreras, corredores e inscripciones existentes.
 */
val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Copias temporales sin llaves foráneas
        db.execSQL("CREATE TABLE tmp_carrera AS SELECT * FROM carrera")
        db.execSQL("CREATE TABLE tmp_corredor AS SELECT * FROM corredor")
        db.execSQL("CREATE TABLE tmp_inscripcion AS SELECT * FROM inscripcion")

        // 2. Borrar tablas viejas (hijas primero)
        db.execSQL("DROP TABLE inscripcion")
        db.execSQL("DROP TABLE corredor")
        db.execSQL("DROP TABLE carrera")

        // 3. Crear el esquema nuevo
        db.execSQL("CREATE TABLE IF NOT EXISTS `pais` (`idPais` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pais_nombre` ON `pais` (`nombre`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `ciudad` (`idCiudad` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `idPais` INTEGER, FOREIGN KEY(`idPais`) REFERENCES `pais`(`idPais`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ciudad_idPais` ON `ciudad` (`idPais`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ciudad_nombre_idPais` ON `ciudad` (`nombre`, `idPais`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `categoria` (`idCategoria` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `edadMin` INTEGER NOT NULL, `edadMax` INTEGER NOT NULL)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categoria_nombre` ON `categoria` (`nombre`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `carrera` (`idCarrera` TEXT NOT NULL, `nombre` TEXT NOT NULL, `fecha` TEXT NOT NULL, `idCiudad` INTEGER, `url` TEXT NOT NULL, `tipoRecorrido` TEXT, `desnivelM` INTEGER, `terreno` TEXT, `temperaturaC` REAL, `humedad` INTEGER, `condiciones` TEXT, PRIMARY KEY(`idCarrera`), FOREIGN KEY(`idCiudad`) REFERENCES `ciudad`(`idCiudad`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_carrera_idCiudad` ON `carrera` (`idCiudad`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `corredor` (`idCorredor` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nombre` TEXT NOT NULL, `edad` INTEGER NOT NULL, `idCategoria` INTEGER NOT NULL, FOREIGN KEY(`idCategoria`) REFERENCES `categoria`(`idCategoria`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_corredor_idCategoria` ON `corredor` (`idCategoria`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `inscripcion` (`idCorredor` INTEGER NOT NULL, `idCarrera` TEXT NOT NULL, `dorsal` INTEGER NOT NULL, `tiempoObjetivo` TEXT NOT NULL, PRIMARY KEY(`idCorredor`, `idCarrera`), FOREIGN KEY(`idCorredor`) REFERENCES `corredor`(`idCorredor`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`idCarrera`) REFERENCES `carrera`(`idCarrera`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_inscripcion_idCarrera` ON `inscripcion` (`idCarrera`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_inscripcion_idCarrera_dorsal` ON `inscripcion` (`idCarrera`, `dorsal`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `resultado` (`idCorredor` INTEGER NOT NULL, `idCarrera` TEXT NOT NULL, `tiempoReal` TEXT NOT NULL, PRIMARY KEY(`idCorredor`, `idCarrera`), FOREIGN KEY(`idCorredor`, `idCarrera`) REFERENCES `inscripcion`(`idCorredor`, `idCarrera`) ON UPDATE NO ACTION ON DELETE CASCADE )")

        db.execSQL("CREATE VIEW `carrera_vista` AS $CARRERA_VISTA_SQL")
        db.execSQL("CREATE VIEW `corredor_vista` AS $CORREDOR_VISTA_SQL")

        // 4. Llenar catálogos
        sembrarCategorias(db)
        db.execSQL(
            """INSERT INTO pais (nombre)
               SELECT DISTINCT TRIM(pais) FROM tmp_carrera WHERE TRIM(pais) <> ''"""
        )
        db.execSQL(
            """INSERT INTO ciudad (nombre, idPais)
               SELECT DISTINCT TRIM(t.ciudad), p.idPais
               FROM tmp_carrera t LEFT JOIN pais p ON p.nombre = TRIM(t.pais)
               WHERE TRIM(t.ciudad) <> '' OR TRIM(t.pais) <> ''"""
        )

        // 5. Copiar datos
        db.execSQL(
            """INSERT INTO carrera (idCarrera, nombre, fecha, idCiudad, url, tipoRecorrido,
                                    desnivelM, terreno, temperaturaC, humedad, condiciones)
               SELECT t.idCarrera, t.nombre, t.fecha,
                      (SELECT ci.idCiudad FROM ciudad ci LEFT JOIN pais p ON p.idPais = ci.idPais
                       WHERE ci.nombre = TRIM(t.ciudad) AND COALESCE(p.nombre, '') = TRIM(t.pais)
                       LIMIT 1),
                      t.url, t.tipoRecorrido, t.desnivelM, t.terreno, t.temperaturaC,
                      t.humedad, t.condiciones
               FROM tmp_carrera t"""
        )
        db.execSQL(
            """INSERT INTO corredor (idCorredor, nombre, edad, idCategoria)
               SELECT t.idCorredor, t.nombre, t.edad,
                      COALESCE(
                          (SELECT idCategoria FROM categoria WHERE nombre = t.categoria),
                          (SELECT idCategoria FROM categoria
                           WHERE t.edad BETWEEN edadMin AND edadMax ORDER BY edadMin LIMIT 1),
                          (SELECT MIN(idCategoria) FROM categoria))
               FROM tmp_corredor t"""
        )
        // Si dos corredores tenían el mismo dorsal en una carrera, el segundo recibe uno nuevo
        db.execSQL(
            """INSERT INTO inscripcion (idCorredor, idCarrera, dorsal, tiempoObjetivo)
               SELECT t.idCorredor, t.idCarrera,
                      CASE WHEN t.rowid = (SELECT MIN(t2.rowid) FROM tmp_inscripcion t2
                                           WHERE t2.idCarrera = t.idCarrera AND t2.dorsal = t.dorsal)
                           THEN t.dorsal
                           ELSE (SELECT MAX(t3.dorsal) FROM tmp_inscripcion t3
                                 WHERE t3.idCarrera = t.idCarrera) + t.rowid
                      END,
                      t.tiempoObjetivo
               FROM tmp_inscripcion t
               WHERE t.idCorredor IN (SELECT idCorredor FROM corredor)
                 AND t.idCarrera IN (SELECT idCarrera FROM carrera)"""
        )

        // 6. Limpiar
        db.execSQL("DROP TABLE tmp_carrera")
        db.execSQL("DROP TABLE tmp_corredor")
        db.execSQL("DROP TABLE tmp_inscripcion")
    }
}
