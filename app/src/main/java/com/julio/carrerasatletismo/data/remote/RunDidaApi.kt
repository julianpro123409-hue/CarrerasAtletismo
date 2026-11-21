package com.julio.carrerasatletismo.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface RunDidaApi {

    @GET("api/marathons.json")
    suspend fun obtenerMaratones(): MaratonesResponse

    @GET("api/marathons/{id}.json")
    suspend fun obtenerMaraton(@Path("id") id: String): MaratonDetalleResponse
}
