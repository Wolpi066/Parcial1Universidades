package com.example.parcial1universidades.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Le pregunta al sistema si hay una red disponible.
 *
 * Sirve para cortar la busqueda antes de empezar cuando el celular esta en modo
 * avion o sin datos: asi el usuario ve el aviso al toque en vez de esperar a que
 * Retrofit se canse solo.
 *
 * Necesita el permiso ACCESS_NETWORK_STATE, declarado en el manifest.
 */
class NetworkChecker(private val context: Context) {

    /** true si hay alguna red activa que dice tener salida a internet. */
    fun isConnected(): Boolean {
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // activeNetwork es null directamente cuando no hay ninguna red prendida.
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
