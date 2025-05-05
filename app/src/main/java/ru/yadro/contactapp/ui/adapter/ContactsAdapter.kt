package ru.yadro.contactapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.yadro.contactapp.Contact
import ru.yadro.contactapp.R

class ContactsAdapter(private val contacts: List<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.contactName)
        val phone: TextView = itemView.findViewById(R.id.contactPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = contacts[position].name
        holder.phone.text = contacts[position].phone
    }

    override fun getItemCount() = contacts.size


}