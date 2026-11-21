package com.julio.carrerasatletismo.domain

/** Acepta h:mm:ss o hh:mm:ss y lo deja como hh:mm:ss (para poder ordenar). */
fun normalizarTiempo(texto: String): String? {
    val m = Regex("""^\s*(\d{1,2}):([0-5]\d):([0-5]\d)\s*$""").find(texto) ?: return null
    val (h, min, s) = m.destructured
    return "%02d:%s:%s".format(h.toInt(), min, s)
}
