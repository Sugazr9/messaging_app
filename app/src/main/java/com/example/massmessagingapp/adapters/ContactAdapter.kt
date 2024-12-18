package com.example.massmessagingapp.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.massmessagingapp.R
import com.example.massmessagingapp.models.Contact

class ContactAdapter(
    private val allContacts: List<Contact>,
    private val selectedContacts: MutableSet<Contact>,
    private val screenType: String,
    private val onContactSelected: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var filteredContacts = allContacts.toMutableList() // Filtered list for display

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contactName: TextView = itemView.findViewById(R.id.contact_name)
        private val contactNumber: TextView = itemView.findViewById(R.id.contact_number)
        private var nameFont = ResourcesCompat.getFont(itemView.context, R.font.fredoka_standard)
        private var numberFont = ResourcesCompat.getFont(itemView.context, R.font.fredoka_standard)

        fun bind(contact: Contact, isSelected: Boolean, isInternational: Boolean) {

            // resetting click listener as a bug fix
            itemView.setOnClickListener(null)
            // Set contact name text
            contactName.text = itemView.context.getString(R.string.contact_adapter_name, contact.name, contact.type)
            contactNumber.text = contact.number
            val selectedBackground = if (screenType == "messageDetails") {
                itemView.context.getColor(R.color.colorFailedRed)
            } else {
                itemView.context.getColor(R.color.colorAccent)
            }
            // Apply styles based on selection state
            if (isSelected) {
                itemView.setBackgroundColor(selectedBackground)
                val tfType = if (isInternational) {
                    Typeface.BOLD_ITALIC
                } else {
                    Typeface.BOLD
                }
                contactName.setTypeface(nameFont, tfType)
                contactNumber.setTypeface(numberFont, tfType)
            } else {
                itemView.setBackgroundColor(itemView.context.getColor(android.R.color.transparent))
                val tfType = if (isInternational) {
                    Typeface.ITALIC
                } else {
                    Typeface.NORMAL
                }
                contactName.setTypeface(nameFont, tfType)
                contactNumber.setTypeface(numberFont, tfType)
            }

            if (isInternational) {
                contactName.setTextColor(itemView.context.getColor(R.color.colorAlert))
                contactNumber.setTextColor(itemView.context.getColor(R.color.colorAlert))
            }
            else {
                val typedArray = itemView.context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary))
                val textColorPrimary = typedArray.getColor(0, Color.BLACK) // Fallback to black if not resolved
                typedArray.recycle() // Remember to recycle to avoid memory leaks

                contactName.setTextColor(textColorPrimary)
                contactNumber.setTextColor(itemView.context.getColor(R.color.colorPrimaryDark))
            }

            // Toggle selection on click
            itemView.setOnClickListener {
                onContactSelected(contact)
                notifyDataSetChanged()
            }
}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = filteredContacts[position]
        val isSelected = selectedContacts.contains(contact)
        val isInternational = contact.number.startsWith("+")
        holder.bind(contact, isSelected, isInternational)
    }

    override fun getItemCount(): Int = filteredContacts.size

    // Filter function to update the displayed contacts based on the search query
    fun filter(query: String) {
        filteredContacts = if (query.isEmpty()) {
            allContacts.toMutableList()
        } else {
            allContacts.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged() // Refresh the displayed list
    }
}
