package com.example.parcial1universidades.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parcial1universidades.repository.UniversityRepository

/**
 * Fabrica de UniversityViewModel.
 *
 * Hace falta porque UniversityViewModel no tiene constructor vacio: recibe el
 * repositorio. Android no sabe como armarlo solo, entonces le damos esta clase
 * que le explica como.
 *
 * Este es el punto donde se inyectan las dependencias: la Activity arma el
 * repositorio, se lo pasa a la fabrica, y la fabrica se lo pasa al ViewModel.
 */
class UniversityViewModelFactory(
    private val repository: UniversityRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UniversityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UniversityViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
