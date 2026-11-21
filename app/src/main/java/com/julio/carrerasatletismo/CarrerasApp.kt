package com.julio.carrerasatletismo

import android.app.Application
import com.julio.carrerasatletismo.di.ContenedorApp
import com.julio.carrerasatletismo.di.ContenedorAppImpl

// Clase Application: crea el contenedor de dependencias al iniciar la app.
// Se registra en AndroidManifest.xml con android:name=".CarrerasApp"
class CarrerasApp : Application() {

    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorAppImpl(this)
    }
}
