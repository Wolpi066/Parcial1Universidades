package com.example.parcial1universidades.viewmodel

import com.example.parcial1universidades.model.University

/**
 * Los estados por los que puede pasar la pantalla principal.
 *
 * Al ser una sealed class, solo pueden heredarla las clases de este archivo.
 * Por eso el compilador sabe que son todos los casos posibles y obliga a
 * cubrirlos en el when de la Activity: si mañana se agrega uno nuevo, el
 * codigo no compila hasta manejarlo.
 */
sealed class UniversityUiState {

    /** Estado inicial, antes de que el usuario escriba algo. */
    object Idle : UniversityUiState()

    /** Se esta esperando la respuesta de la API. */
    object Loading : UniversityUiState()

    /** La busqueda encontro universidades. */
    data class Success(val universities: List<University>) : UniversityUiState()

    /** La busqueda anduvo bien pero ese pais no tiene resultados. */
    data class Empty(val country: String) : UniversityUiState()

    /** Algo salio mal: sin conexion, error del servidor o problema al leer el JSON. */
    data class Error(val message: String) : UniversityUiState()
}
