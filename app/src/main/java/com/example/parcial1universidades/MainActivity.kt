package com.example.parcial1universidades

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parcial1universidades.adapter.UniversityAdapter
import com.example.parcial1universidades.databinding.ActivityMainBinding
import com.example.parcial1universidades.model.University
import com.example.parcial1universidades.repository.UniversityRepository
import com.example.parcial1universidades.service.NetworkChecker
import com.example.parcial1universidades.viewmodel.UniversityUiState
import com.example.parcial1universidades.viewmodel.UniversityViewModel
import com.example.parcial1universidades.viewmodel.UniversityViewModelFactory

/**
 * Pantalla 1: buscador por pais + lista de universidades (la "V" de MVVM).
 *
 * Solo muestra y escucha lo que escribe el usuario. La busqueda contra la API
 * la maneja el ViewModel; esta clase observa un unico estado y dibuja.
 */
class MainActivity : AppCompatActivity() {

    // View Binding: acceso a las vistas de activity_main.xml sin findViewById.
    private lateinit var binding: ActivityMainBinding

    // Lo guardamos como propiedad para poder refrescarlo desde el observer.
    private lateinit var adapter: UniversityAdapter

    // El delegate crea el ViewModel, o recupera el que ya existia si la pantalla
    // se volvio a crear (por ejemplo al rotar).
    //
    // Aca es donde se arman las dependencias y se inyectan: la Activity construye
    // el repositorio con lo que necesita y se lo pasa al ViewModel a traves de la
    // fabrica. El ViewModel no se crea nada por su cuenta.
    private val viewModel: UniversityViewModel by viewModels {
        UniversityViewModelFactory(
            repository = UniversityRepository(NetworkChecker(applicationContext))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    /** Deja la lista lista para usar: orientacion vertical y adapter enganchado. */
    private fun setupRecyclerView() {
        adapter = UniversityAdapter(emptyList()) { university ->
            // Tocaron una universidad: abrimos el detalle mandandola dentro del
            // Intent. Viaja porque University es Serializable.
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_UNIVERSITY, university)
            startActivity(intent)
        }

        binding.rvUniversities.layoutManager = LinearLayoutManager(this)
        binding.rvUniversities.adapter = adapter
    }

    /**
     * La busqueda es en tiempo real: no hay boton, el TextWatcher avisa al
     * ViewModel cada vez que cambia el texto y el ViewModel decide cuando salir
     * realmente a la API.
     */
    private fun setupListeners() {
        binding.etCountry.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No lo necesitamos, pero la interfaz obliga a implementarlo.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Idem.
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onQueryChanged(s.toString())
            }
        })
    }

    /**
     * Un solo observer para toda la pantalla. El when sobre la sealed class
     * obliga a contemplar los cinco estados posibles.
     */
    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UniversityUiState.Idle ->
                    showMessage(getString(R.string.message_start))

                is UniversityUiState.Loading ->
                    showLoading()

                is UniversityUiState.Success ->
                    showResults(state.universities)

                is UniversityUiState.Empty ->
                    showMessage(getString(R.string.message_empty, state.country))

                is UniversityUiState.Error ->
                    showMessage(state.message)
            }
        }
    }

    /** Muestra la lista y esconde el resto. */
    private fun showResults(universities: List<University>) {
        adapter.updateList(universities)
        binding.rvUniversities.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.tvMessage.visibility = View.GONE
    }

    /** Prende el indicador de carga y esconde el resto. */
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvUniversities.visibility = View.GONE
        binding.tvMessage.visibility = View.GONE
    }

    /** Mismo TextView para el mensaje inicial, el de vacio y el de error. */
    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.rvUniversities.visibility = View.GONE
    }

    /**
     * Como la app va de punta a punta de la pantalla (edge to edge), agregamos
     * padding para que el contenido no quede debajo de las barras del sistema.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
