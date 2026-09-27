package com.example.parcial1universidades

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.parcial1universidades.databinding.FragmentUniversityDetailBinding
import com.example.parcial1universidades.model.University

/**
 * Fragment obligatorio del trabajo: muestra la ficha de una universidad con sus
 * dominios oficiales, el codigo de pais, la provincia y el link a su sitio.
 *
 * No sabe de donde sale la universidad ni hace llamadas a la API: la recibe
 * armada en sus argumentos. Asi se podria reusar en cualquier otra pantalla.
 */
class UniversityDetailFragment : Fragment() {

    // El binding se crea en onCreateView y se tira en onDestroyView, porque la
    // vista del fragment puede morir antes que el fragment. Por eso es nullable.
    private var _binding: FragmentUniversityDetailBinding? = null

    // Atajo para no escribir _binding!! en todos lados. Solo valido mientras la
    // vista existe (entre onCreateView y onDestroyView).
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUniversityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sacamos la universidad de los argumentos que le paso DetailActivity.
        val university = arguments?.getSerializable(ARG_UNIVERSITY) as? University ?: return

        showUniversity(university)
    }

    /** Vuelca los datos de la universidad en las vistas. */
    private fun showUniversity(university: University) {
        binding.apply {
            tvDetailName.text = university.name
            tvDetailCountry.text = university.country

            tvDetailCountryCode.text = university.countryCode
                ?: getString(R.string.not_available)

            // La provincia es el dato que la API no siempre completa.
            tvDetailProvince.text = if (university.stateProvince.isNullOrBlank()) {
                getString(R.string.not_available)
            } else {
                university.stateProvince
            }

            // Un dominio por linea, que se lee mejor que todos en fila.
            tvDetailDomains.text = university.domains
                ?.joinToString("\n")
                ?: getString(R.string.not_available)

            val webPage = university.getMainWebPage()
            if (webPage != null) {
                btnOpenWeb.text = webPage
                btnOpenWeb.isEnabled = true
                btnOpenWeb.setOnClickListener { openInBrowser(webPage) }
            } else {
                // Sin pagina web no hay nada que abrir.
                btnOpenWeb.text = getString(R.string.no_web_page)
                btnOpenWeb.isEnabled = false
            }

            btnBack.setOnClickListener { requireActivity().finish() }
        }
    }

    /** Abre el sitio de la universidad en el navegador del celular. */
    private fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Por si el dispositivo no tiene ningun navegador instalado.
            Toast.makeText(requireContext(), R.string.error_no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para no dejar la vista colgada en memoria.
        _binding = null
    }

    companion object {
        private const val ARG_UNIVERSITY = "arg_university"

        /**
         * Forma recomendada de crear un Fragment que necesita datos: nunca se le
         * pasan por constructor, porque al recrearlo el sistema usa el constructor
         * vacio y los perderia. Van en un Bundle de argumentos, que si sobrevive.
         */
        fun newInstance(university: University): UniversityDetailFragment {
            return UniversityDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UNIVERSITY, university)
                }
            }
        }
    }
}
