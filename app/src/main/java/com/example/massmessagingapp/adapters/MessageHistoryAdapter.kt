package com.example.massmessagingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.massmessagingapp.R
import com.example.massmessagingapp.models.Message
import java.text.SimpleDateFormat
import java.util.Locale

class MessageHistoryAdapter(
    private val messageHistory: List<Message>,
    private val onMessageClick: (Message) -> Unit,    // Callback for clicking a message
) : RecyclerView.Adapter<MessageHistoryAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contents: TextView = itemView.findViewById(R.id.tv_message_content)
        private val timestamp: TextView = itemView.findViewById(R.id.tv_message_timestamp)
        private val contactsCount: TextView = itemView.findViewById(R.id.tv_message_contacts_count)

        fun bind(message: Message) {

            contents.text = message.content
            // Format for date and time (e.g., "Nov 4, 2024")
            val datetimeFormat = SimpleDateFormat("MMM d, yyyy\nhh:mm a", Locale.getDefault())
            timestamp.text = datetimeFormat.format(message.timestamp)
            val contactCnt = message.recipients.size
            contactsCount.text = itemView.context.resources.getQuantityString(R.plurals.message_item_contact_num, contactCnt, contactCnt)

            // Handle regular click to select the group
            itemView.setOnClickListener {
                onMessageClick(message)
            }

            /*// Handle long click to show the options dialog (Edit/Delete)
            itemView.setOnLongClickListener {
                showGroupEditPopup(group, itemView)
                true  // Return true to indicate the long-click was handled
            }*/
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageHistory[position]
        holder.bind(message)  // Pass selected state
    }

    override fun getItemCount(): Int {
        return messageHistory.size
    }

    /*private fun showGroupEditPopup(group: Group, anchorView: View) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(anchorView.context)
        val popupView = inflater.inflate(R.layout.group_edit_dialog, null)

        // Initialize the PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each button in the custom layout
        popupView.findViewById<ImageButton>(R.id.btn_edit).setOnClickListener {
            onGroupAction(group, "edit")
            popupWindow.dismiss() // Close the popup after clicking
        }

        popupView.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
            onGroupAction(group, "delete")
            popupWindow.dismiss() // Close the popup after clicking
        }

        // Show the PopupWindow below the anchor view
        popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    fun updateSelectedMessage(group: Group?) {
        selectedGroup = group
    }*/
}