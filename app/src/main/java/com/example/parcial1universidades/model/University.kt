package com.example.parcial1universidades.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Una universidad devuelta por la API de Hipolabs.
 *
 * La API no completa lo mismo para todas: hay universidades sin provincia y
 * alguna sin codigo de pais, por eso esos campos son nulables.
 *
 * Implementa Serializable para poder viajar dentro del Intent hacia
 * DetailActivity y despues dentro del Bundle hacia UniversityDetailFragment.
 */
data class University(

    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String,

    // Codigo ISO de dos letras: AR, ES, US.
    @SerializedName("alpha_two_code")
    val countryCode: String?,

    // Ojo: en el JSON el campo se llama "state-province", con guion en el medio.
    // Un guion no es valido en un nombre de Kotlin, asi que la anotacion es
    // obligatoria aca (sin ella Gson no encuentra el campo y queda siempre null).
    @SerializedName("state-province")
    val stateProvince: String?,

    @SerializedName("domains")
    val domains: List<String>?,

    @SerializedName("web_pages")
    val webPages: List<String>?
) : Serializable {

    /** La primera pagina web, que es la que abre el boton del detalle. */
    fun getMainWebPage(): String? = webPages?.firstOrNull()

    /**
     * "Buenos Aires, Argentina" si hay provincia, o solo "Argentina" si no.
     * Se usa en la fila de la lista, donde no entra todo por separado.
     */
    fun getLocation(): String {
        if (stateProvince.isNullOrBlank()) return country
        return "$stateProvince, $country"
    }
}
