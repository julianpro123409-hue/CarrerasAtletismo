package com.julio.carrerasatletismo.di

import android.content.Context
import com.julio.carrerasatletismo.data.local.AppDatabase
import com.julio.carrerasatletismo.data.remote.RetrofitClient
import com.julio.carrerasatletismo.data.remote.RunDidaApi
import com.julio.carrerasatletismo.data.repositorio.CarrerasRepositoryImpl
import com.julio.carrerasatletismo.domain.repositorio.CarrerasRepository

// Inyección de dependencias manual: el contenedor crea UNA sola vez
// la base de datos, la API y el repositorio, y los comparte en toda la app.
interface ContenedorApp {
    val baseDeDatos: AppDatabase
    val api: RunDidaApi
    val repositorio: CarrerasRepository
}

class ContenedorAppImpl(context: Context) : ContenedorApp {

    override val baseDeDatos: AppDatabase by lazy {
        AppDatabase.obtener(context)
    }

    override val api: RunDidaApi by lazy {
        RetrofitClient.api
    }

    override val repositorio: CarrerasRepository by lazy {
        CarrerasRepositoryImpl(baseDeDatos, api)
    }
}
