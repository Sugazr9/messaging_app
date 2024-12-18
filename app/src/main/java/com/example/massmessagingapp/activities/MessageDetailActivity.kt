package com.example.massmessagingapp.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.massmessagingapp.R
import com.example.massmessagingapp.adapters.ContactAdapter
import com.example.massmessagingapp.models.Contact
import com.example.massmessagingapp.models.Message
import java.text.SimpleDateFormat
import java.util.Locale

class MessageDetailActivity : Activity() {

    private lateinit var backButton: Button
    private lateinit var messageID: TextView
    private lateinit var messageContent: TextView
    private lateinit var messageTime: TextView
    private lateinit var messageStatus: TextView
    private lateinit var contactsRecyclerView: RecyclerView

    private val colorSuccess by lazy {
        resources.getColor(R.color.colorConfirmation, theme)
    }

    private val colorFailure by lazy {
        resources.getColor(R.color.colorAlert, theme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)

        // Initialize views
        backButton = findViewById(R.id.btn_back)
        messageID = findViewById(R.id.tv_message_id)
        messageContent = findViewById(R.id.tv_message_content)
        messageTime = findViewById(R.id.tv_message_time)
        messageStatus = findViewById(R.id.tv_message_status)
        contactsRecyclerView = findViewById(R.id.recycler_view_contacts)

        // Retrieve message and retrieve relevant info from Intent
        val message = intent.getSerializableExtra("message", Message::class.java)
        val datetimeFormat = SimpleDateFormat("MMM d, yyyy\nhh:mm a", Locale.getDefault())
        messageTime.text = datetimeFormat.format(message!!.timestamp)
        messageContent.text = message.content
        messageID.text = message.id
        val status: String
        if (message.status) {
            messageStatus.setTextColor(colorSuccess)
            status = "Successful"
        } else {
            messageStatus.setTextColor(colorFailure)
            status = "Failed"
        }
        messageStatus.text = this.getString(R.string.message_detail_status, status)

        // Back Button Click
        backButton.setOnClickListener {
            finish()
        }

        // Display list of contacts in RecyclerView
        val failedContacts = message.failedRecipients.toMutableSet()
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        contactsRecyclerView.adapter = ContactAdapter(message.recipients, failedContacts, "messageDetails") { _: Contact -> }
    }
}