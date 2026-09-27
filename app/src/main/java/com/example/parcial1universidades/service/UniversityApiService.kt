package com.example.parcial1universidades.service

import com.example.parcial1universidades.model.University
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoint de la API de Hipolabs que usa la app.
 *
 * Retrofit toma esta interfaz y genera solo la implementacion que arma la URL
 * y hace la llamada HTTP.
 */
interface UniversityApiService {

    /**
     * Trae las universidades de un pais.
     *
     * Es una funcion suspend: se puede pausar mientras espera la respuesta sin
     * bloquear el hilo en el que corre.
     *
     * La API devuelve directamente un array de universidades, no un objeto con
     * la lista adentro. Por eso el tipo es List<University> y no hace falta una
     * clase envoltorio.
     */
    @GET("search")
    suspend fun searchByCountry(@Query("country") country: String): Response<List<University>>
}
