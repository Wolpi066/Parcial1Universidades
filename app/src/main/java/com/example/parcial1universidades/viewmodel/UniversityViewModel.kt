package com.example.parcial1universidades.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial1universidades.repository.UniversityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel de la pantalla principal (la "VM" de MVVM).
 *
 * Expone un solo LiveData con el estado completo de la pantalla. La Activity lo
 * observa y con un when decide que mostrar. No sabe nada de Retrofit: eso es
 * cosa del repositorio.
 *
 * Al sobrevivir a la rotacion, la busqueda no se pierde si se gira el celular.
 *
 * Recibe el repositorio por constructor (inyeccion de dependencias): quien lo
 * crea decide que repositorio usar, y para testearlo se le podria pasar uno falso.
 */
class UniversityViewModel(private val repository: UniversityRepository) : ViewModel() {

    // El MutableLiveData es privado (solo lo toca el ViewModel) y afuera se
    // expone la version de solo lectura.
    private val _uiState = MutableLiveData<UniversityUiState>(UniversityUiState.Idle)
    val uiState: LiveData<UniversityUiState> get() = _uiState

    // Guardamos la corrutina de la ultima busqueda para poder cancelarla.
    private var searchJob: Job? = null

    // El ultimo pais que se busco bien. Al rotar, la Activity se recrea y el
    // TextWatcher vuelve a avisar el mismo texto; con esto evitamos repetir la
    // llamada cuando esos resultados ya estan en pantalla.
    private var lastSearchedCountry: String? = null

    /**
     * Se llama en cada tecla que escribe el usuario.
     *
     * Cancelamos la busqueda anterior antes de lanzar la nueva y esperamos un
     * ratito antes de salir a la API. Asi escribir "Argentina" dispara una sola
     * llamada y no nueve: las ocho primeras se cancelan solas antes del delay.
     */
    fun onQueryChanged(query: String) {
        searchJob?.cancel()

        val country = query.trim()

        // Con una o dos letras la busqueda no tiene sentido; volvemos al inicio.
        if (country.length < MIN_QUERY_LENGTH) {
            _uiState.value = UniversityUiState.Idle
            return
        }

        // Ya tenemos en pantalla los resultados de ese pais: no los pedimos de nuevo.
        // Esto es lo que evita una llamada de mas cada vez que se gira el celular.
        if (country == lastSearchedCountry && _uiState.value is UniversityUiState.Success) {
            return
        }

        // Dispatchers.IO es el grupo de hilos pensado para esperar red o disco.
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(SEARCH_DELAY_MS)
            _uiState.postValue(UniversityUiState.Loading)
            search(country)
        }
    }

    /** Hace la consulta y traduce lo que vuelve a uno de los estados. */
    private suspend fun search(country: String) {
        // Si no hay red ni salimos a preguntar: cortamos aca.
        if (!repository.isNetworkAvailable()) {
            _uiState.postValue(UniversityUiState.Error(MESSAGE_NO_CONNECTION))
            return
        }

        try {
            val response = repository.searchByCountry(country)

            // isSuccessful es true para cualquier codigo 2xx.
            if (response.isSuccessful) {
                val universities = response.body() ?: emptyList()

                if (universities.isEmpty()) {
                    _uiState.postValue(UniversityUiState.Empty(country))
                } else {
                    _uiState.postValue(UniversityUiState.Success(universities))
                    lastSearchedCountry = country
                }
            } else {
                _uiState.postValue(
                    UniversityUiState.Error("Error del servidor (${response.code()})")
                )
            }
        } catch (e: IOException) {
            // Una IOException casi siempre es que se corto la conexion o vencio
            // el timeout en el medio de la llamada.
            Log.e(TAG, "Fallo la conexion", e)
            _uiState.postValue(UniversityUiState.Error(MESSAGE_NO_CONNECTION))
        } catch (e: Exception) {
            // Cualquier otra cosa suele ser un problema al leer el JSON.
            Log.e(TAG, "Error inesperado", e)
            _uiState.postValue(UniversityUiState.Error("Ocurrió un error inesperado."))
        }
    }

    companion object {
        private const val TAG = "UniversityViewModel"

        /** Cuantas letras hacen falta para que valga la pena buscar. */
        private const val MIN_QUERY_LENGTH = 3

        /** Cuanto esperamos, en milisegundos, a que el usuario deje de escribir. */
        private const val SEARCH_DELAY_MS = 400L

        private const val MESSAGE_NO_CONNECTION =
            "Sin conexión a internet. Fijate la red y probá de nuevo."
    }
}
