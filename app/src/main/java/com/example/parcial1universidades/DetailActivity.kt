package com.example.parcial1universidades

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.parcial1universidades.databinding.ActivityDetailBinding
import com.example.parcial1universidades.model.University

/**
 * Pantalla 2: no dibuja nada por su cuenta, solo hace de contenedor del
 * UniversityDetailFragment, que es el que muestra la ficha.
 *
 * Recibe la universidad por Intent desde MainActivity y se la pasa al fragment
 * por argumentos.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        // Recuperamos la universidad que mando la pantalla anterior.
        // getSerializableExtra saca lo que se guardo con putExtra usando la MISMA
        // clave, y 'as?' la castea a University (si no puede, devuelve null en
        // vez de romper). En Android nuevo aparece tachado, pero funciona igual.
        val university = intent.getSerializableExtra(EXTRA_UNIVERSITY) as? University

        // Si por algun motivo no llego nada, cerramos en vez de mostrar una
        // ficha vacia.
        if (university == null) {
            finish()
            return
        }

        // Solo montamos el fragment la primera vez. Si la pantalla se recrea (al
        // rotar), el FragmentManager ya lo tiene guardado y lo vuelve a poner
        // solo: si no chequearamos esto, quedarian dos fragments encimados.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    UniversityDetailFragment.newInstance(university)
                )
                .commit()
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        /**
         * Clave del Intent. Va en el companion object para que el que guarda y
         * el que lee usen exactamente el mismo texto.
         */
        const val EXTRA_UNIVERSITY = "extra_university"
    }
}
