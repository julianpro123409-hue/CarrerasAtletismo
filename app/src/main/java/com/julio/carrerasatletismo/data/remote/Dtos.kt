package com.julio.carrerasatletismo.data.remote

import com.google.gson.JsonElement

data class MaratonesResponse(
    val meta: MetaDto?,
    val active: List<MaratonResumenDto>?,
    val archived: List<MaratonResumenDto>?
)

data class MetaDto(
    val totalActive: Int?,
    val totalArchived: Int?
)

data class MaratonResumenDto(
    val id: String?,
    val name: JsonElement?,
    val date: String?,
    val city: String?,
    val country: String?,
    val url: String?
)

data class MaratonDetalleResponse(
    val marathon: MaratonDetalleDto?
)

data class MaratonDetalleDto(
    val id: String?,
    val name: JsonElement?,
    val date: String?,
    val timezone: String?,
    val city: String?,
    val country: String?,
    val weather: ClimaDto?,
    val course: RecorridoDto?,
    val links: LinksDto?
)

data class ClimaDto(
    val avgTempC: Double?,
    val humidity: Double?,
    val windKmh: Double?,
    val precipPct: Double?,
    val conditions: String?
)

data class RecorridoDto(
    val type: String?,
    val elevationGain: Double?,
    val profile: String?,
    val terrain: String?
)

data class LinksDto(
    val page: String?,
    val countdown: String?,
    val api: String?
)

fun JsonElement?.comoTexto(): String {
    if (this == null || isJsonNull) return ""
    if (isJsonPrimitive) return asString
    if (isJsonObject) {
        val obj = asJsonObject
        for (clave in listOf("es", "en", "full", "official", "short", "local")) {
            val valor = obj.get(clave)
            if (valor != null && valor.isJsonPrimitive) return valor.asString
        }
        return obj.entrySet().firstOrNull { it.value.isJsonPrimitive }?.value?.asString ?: ""
    }
    return toString()
}
