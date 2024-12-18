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

class FailedContactsAdapter(private val failedContacts: List<Pair<Contact, String>>, private val selectedContacts: MutableSet<Contact>,
    private val retryEnabled: Boolean) : RecyclerView.Adapter<FailedContactsAdapter.FailedContactViewHolder>() {

    inner class FailedContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val contactName: TextView = view.findViewById(R.id.contact_name)
        private val errorMessage: TextView = view.findViewById(R.id.contact_number)
        private var nameFont = ResourcesCompat.getFont(itemView.context, R.font.fredoka_standard)
        private var errorFont = ResourcesCompat.getFont(itemView.context, R.font.fredoka_standard)

        fun bind(contact: Contact, errorMsg: String, isSelected: Boolean) {

            // resetting click listener as a bug fix
            itemView.setOnClickListener(null)
            // Set contact name text and error text
            contactName.text = contact.name
            val condErrorMsg = errorMsg.split("SMS Error: ").filter {it.isNotEmpty()}[0]
            errorMessage.text = condErrorMsg
            errorMessage.maxWidth = 400

            // Apply styles based on selection state
            if (isSelected) {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.colorPrimary))
                contactName.setTypeface(nameFont, Typeface.BOLD)
                errorMessage.setTypeface(errorFont, Typeface.BOLD)
            } else {
                itemView.setBackgroundColor(itemView.context.getColor(android.R.color.transparent))
                contactName.setTypeface(nameFont, Typeface.NORMAL)
                errorMessage.setTypeface(errorFont, Typeface.NORMAL)
            }

            val typedArray =
                itemView.context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary))
            val textColorPrimary =
                typedArray.getColor(0, Color.BLACK) // Fallback to black if not resolved
            typedArray.recycle() // Remember to recycle to avoid memory leaks

            contactName.setTextColor(textColorPrimary)
            errorMessage.setTextColor(itemView.context.getColor(R.color.colorAlert))

            // Toggle selection on click
            itemView.setOnClickListener {
                if (!retryEnabled) {
                    return@setOnClickListener
                }
                if (selectedContacts.contains(contact)) {
                    selectedContacts.remove(contact) // Deselect if selected
                } else {
                    selectedContacts.add(contact) // Select if not selected
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FailedContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return FailedContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: FailedContactViewHolder, position: Int) {
        val (contact, error) = failedContacts[position]
        val isSelected = selectedContacts.contains(contact)
        holder.bind(contact, error, isSelected)
    }

    override fun getItemCount(): Int = failedContacts.size
}
