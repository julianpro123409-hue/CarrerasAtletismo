package com.julio.carrerasatletismo.data.mapper

import com.julio.carrerasatletismo.data.local.Categoria as CategoriaEntity
import com.julio.carrerasatletismo.data.local.Carrera as CarreraVista
import com.julio.carrerasatletismo.data.local.Corredor as CorredorVista
import com.julio.carrerasatletismo.data.local.CorredorConCarreras as CorredorConCarrerasLocal
import com.julio.carrerasatletismo.data.local.InscritoEnCarrera as InscritoLocal
import com.julio.carrerasatletismo.domain.modelo.Carrera
import com.julio.carrerasatletismo.domain.modelo.Categoria
import com.julio.carrerasatletismo.domain.modelo.Corredor
import com.julio.carrerasatletismo.domain.modelo.CorredorConCarreras
import com.julio.carrerasatletismo.domain.modelo.InscritoEnCarrera

// Mappers: convierten los objetos de Room (capa data) a modelos de dominio.

fun CarreraVista.toDomain() = Carrera(
    idCarrera = idCarrera,
    nombre = nombre,
    fecha = fecha,
    ciudad = ciudad,
    pais = pais,
    url = url,
    tipoRecorrido = tipoRecorrido,
    desnivelM = desnivelM,
    terreno = terreno,
    temperaturaC = temperaturaC,
    humedad = humedad,
    condiciones = condiciones
)

fun CorredorVista.toDomain() = Corredor(
    idCorredor = idCorredor,
    nombre = nombre,
    edad = edad,
    categoria = categoria,
    idCategoria = idCategoria
)

fun CategoriaEntity.toDomain() = Categoria(
    idCategoria = idCategoria,
    nombre = nombre,
    edadMin = edadMin,
    edadMax = edadMax
)

fun CorredorConCarrerasLocal.toDomain() = CorredorConCarreras(
    corredor = corredor.toDomain(),
    carreras = carreras.map { it.toDomain() }
)

fun InscritoLocal.toDomain() = InscritoEnCarrera(
    idCorredor = idCorredor,
    nombre = nombre,
    categoria = categoria,
    dorsal = dorsal,
    tiempoObjetivo = tiempoObjetivo,
    tiempoReal = tiempoReal,
    posicion = posicion
)
