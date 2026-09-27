package com.example.parcial1universidades.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parcial1universidades.databinding.ItemUniversityBinding
import com.example.parcial1universidades.model.University

/**
 * Conecta la lista de universidades con el RecyclerView: agarra cada University
 * y la pega en una fila de item_university.xml.
 *
 * @param universityList lista a mostrar. Arranca vacia y se llena cuando el
 *                       ViewModel publica resultados.
 * @param onItemClick que hacer cuando tocan una fila. Lo decide la Activity, el
 *                    adapter solo avisa.
 */
class UniversityAdapter(
    private var universityList: List<University>,
    private val onItemClick: (University) -> Unit
) : RecyclerView.Adapter<UniversityAdapter.UniversityViewHolder>() {

    /**
     * Sostiene las vistas de UNA fila. Guardamos el binding de item_university.xml
     * en vez de cada vista por separado, asi el RecyclerView recicla la fila sin
     * volver a buscarlas. binding.root es la MaterialCardView de afuera.
     */
    class UniversityViewHolder(val binding: ItemUniversityBinding) :
        RecyclerView.ViewHolder(binding.root)

    /** Infla item_university.xml y lo mete en un ViewHolder. Se llama pocas veces. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversityViewHolder {
        val binding = ItemUniversityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UniversityViewHolder(binding)
    }

    override fun getItemCount() = universityList.size

    /** Llena la fila de esa posicion con los datos. Se llama muchas veces. */
    override fun onBindViewHolder(holder: UniversityViewHolder, position: Int) {
        val university = universityList[position]

        holder.binding.apply {
            tvUniversityName.text = university.name
            tvUniversityLocation.text = university.getLocation()

            // El codigo de pais casi siempre viene, pero por las dudas.
            tvCountryCode.text = university.countryCode ?: "--"

            root.setOnClickListener { onItemClick(university) }
        }
    }

    /** Reemplaza la lista y redibuja, cuando el ViewModel publica resultados nuevos. */
    fun updateList(newList: List<University>) {
        universityList = newList
        notifyDataSetChanged()
    }
}
