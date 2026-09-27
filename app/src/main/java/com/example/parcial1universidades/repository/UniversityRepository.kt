package com.example.parcial1universidades.repository

import android.util.Log
import com.example.parcial1universidades.model.University
import com.example.parcial1universidades.service.NetworkChecker
import com.example.parcial1universidades.service.UniversityApiService
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Unica puerta de entrada a los datos de universidades (patron Repository).
 *
 * Concentra la configuracion de Retrofit, la consulta a la API y el chequeo de
 * red. Si mañana hubiera que cambiar de API o agregar una cache, se toca solo
 * este archivo y el ViewModel ni se entera.
 *
 * Recibe el NetworkChecker por constructor en vez de crearlo adentro, asi se lo
 * puede armar con uno falso para probarlo.
 */
class UniversityRepository(private val networkChecker: NetworkChecker) {

    // Un solo Retrofit para toda la vida del repositorio.
    private val apiService: UniversityApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        // GsonConverterFactory es el que convierte el JSON en nuestras data classes.
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(UniversityApiService::class.java)

    /** true si el celular tiene una red con salida a internet. */
    fun isNetworkAvailable(): Boolean = networkChecker.isConnected()

    /**
     * Pide a la API las universidades de un pais.
     *
     * Devuelve el Response completo y no solo la lista, porque el ViewModel
     * necesita el codigo HTTP para distinguir un error del servidor de una
     * busqueda sin resultados.
     */
    suspend fun searchByCountry(country: String): Response<List<University>> {
        Log.d(TAG, "Pidiendo universidades de: $country")

        // La ejecucion se pausa aca hasta que la API responda, sin congelar la UI.
        val response = apiService.searchByCountry(country)

        if (response.isSuccessful) {
            Log.d(TAG, "Llegaron ${response.body()?.size ?: 0} universidades")
        } else {
            Log.e(TAG, "El servidor respondio ${response.code()}")
        }

        return response
    }

    companion object {
        private const val TAG = "UniversityRepository"

        // La API de Hipolabs solo responde por http, no por https.
        private const val BASE_URL = "http://universities.hipolabs.com/"
    }
}
