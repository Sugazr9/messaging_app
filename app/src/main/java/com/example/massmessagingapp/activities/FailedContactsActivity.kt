package com.example.massmessagingapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.massmessagingapp.R
import com.example.massmessagingapp.adapters.FailedContactsAdapter
import com.example.massmessagingapp.models.Contact

class FailedContactsActivity : Activity() {

    private lateinit var failedContactsError: List<Pair<Contact, String>>
    private val selectedFailedContacts: MutableSet<Contact> = mutableSetOf()
    private lateinit var messageContent: String
    private var isRetryEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failed_contacts)

        // Get data from intent
        val failedContacts = intent.getSerializableExtra("failedContacts") as? ArrayList<Contact>
        val errorMessages = intent.getStringArrayListExtra("errorMessages")
        messageContent = intent.getStringExtra("messageContent") ?: ""
        isRetryEnabled = intent.getBooleanExtra("isRetryEnabled", true)

        if (failedContacts != null && errorMessages != null) {
            // Assuming both lists are the same size, iterate by index
            failedContactsError = failedContacts.mapIndexed { index: Int, contact: Contact ->
                contact to errorMessages[index]
            }

            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_failed_contacts)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = FailedContactsAdapter(failedContactsError, selectedFailedContacts,
                isRetryEnabled)
        }

        // Set up buttons
        val retryButton = findViewById<Button>(R.id.btn_retry)
        val closeButton = findViewById<Button>(R.id.btn_close)

        // Conditional question of retrying
        val retryQuestion = findViewById<TextView>(R.id.ques_failed_groups)
        if (!isRetryEnabled) {
            retryQuestion.text = ""
        }

        retryButton.isEnabled = isRetryEnabled // Disable retry button if retry has already been attempted
        retryButton.setOnClickListener {
            // Send failed contacts back to MainActivity for retry
            val resultIntent = Intent()
            if (selectedFailedContacts.isEmpty()) {
                resultIntent.putExtra("retryContacts", ArrayList(failedContacts!!))
            } else {
                resultIntent.putExtra("retryContacts", ArrayList(selectedFailedContacts))
            }
            resultIntent.putExtra("messageContent", messageContent)
            setResult(RESULT_OK, resultIntent)
            finish() // Close the activity after sending data back
        }

        closeButton.setOnClickListener {
            finish() // Just close the activity
        }
    }
}
