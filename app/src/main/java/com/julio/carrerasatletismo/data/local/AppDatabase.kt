package com.julio.carrerasatletismo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Pais::class,
        Ciudad::class,
        Categoria::class,
        CarreraEntity::class,
        CorredorEntity::class,
        Inscripcion::class,
        Resultado::class
    ],
    views = [Carrera::class, Corredor::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carrerasDao(): CarrerasDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "carreras.db"
                )
                    .addMigrations(MIGRACION_1_2)
                    .addCallback(object : Callback() {
                        // Instalación nueva: llenar el catálogo de categorías
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            sembrarCategorias(db)
                        }
                    })
                    .build().also { INSTANCIA = it }
            }
    }
}
