package com.example.massmessagingapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.massmessagingapp.R
import com.example.massmessagingapp.adapters.ContactAdapter
import com.example.massmessagingapp.models.Contact
import com.example.massmessagingapp.models.Group

class UpdateGroupActivity : Activity() {

    private lateinit var groupNameEditText: EditText
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var saveChangesButton: Button
    private lateinit var backButton: Button
    private lateinit var searchEditText: EditText

    private val allContacts = mutableListOf<Contact>()
    private val selectedContacts = mutableSetOf<Contact>()

    private var group: Group? = null
    private var groupPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_update)

        // Initialize views
        groupNameEditText = findViewById(R.id.et_group_name)
        contactsRecyclerView = findViewById(R.id.recycler_view_contacts)
        saveChangesButton = findViewById(R.id.btn_save_group)
        backButton = findViewById(R.id.btn_back)
        searchEditText = findViewById(R.id.et_search)

        // Retrieve group data from intent (if editing)
        group = intent.getSerializableExtra("group", Group::class.java)
        groupPosition = intent.getIntExtra("position", -1)

        // If editing, set the existing group name and contacts
        if (group != null) {
            groupNameEditText.setText(group!!.name)
            selectedContacts.addAll(group!!.contacts)
        }

        // Fetch contacts and mark selected ones
        fetchContacts()

        // Set up RecyclerView
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        val contactAdapter = ContactAdapter(allContacts, selectedContacts, "updateGroup") { contact ->
            if (selectedContacts.contains(contact)) {
                selectedContacts.remove(contact) // Deselect if selected
            } else {
                selectedContacts.add(contact) // Select if not selected
            }
            }
        contactsRecyclerView.adapter = contactAdapter

        // Save Changes Button Click
        saveChangesButton.setOnClickListener {
            saveGroupChanges()
        }

        // Back Button Click
        backButton.setOnClickListener {
            finish()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                contactAdapter.filter(s.toString()) // Filter the contacts based on the search query
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchContacts() {
        val uniqueContacts = mutableMapOf<String, Int>() // To track unique contacts by "name + number" key

        // Fetch all contacts and populate contactList
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
        )
        while (cursor?.moveToNext() == true) {
            val name = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            )
            var number = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            )
            val numberType = cursor.getInt(
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
            )
            // Map the phone type to a readable label
            val strType = when (numberType) {
                ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Mobile"
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
                ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> "Other"
                else -> "Unknown"
            }

            // Normalize the phone number by removing spaces, dashes, and parentheses
            number = number.replace("[\\s\\-()+]+".toRegex(), "")
            if (number.length > 10 && number[0] != '+') {
                number = "+$number"
            }
            if (number.length == 12 && number.take(2) == "+1") {
                number = number.drop(2)
            }


            val uniqueKey = "$name$number"
            if (uniqueContacts.contains(uniqueKey)) {
                uniqueContacts[uniqueKey] = uniqueContacts[uniqueKey]!! + 1
                continue
            }

            uniqueContacts[uniqueKey] = 1
            allContacts.add(Contact(name, number, strType))
        }
        allContacts.add(Contact("Arvind Gouttumukkala", "8056072699", "Mobile"))
        allContacts.sortBy { it.name }
        cursor?.close()
    }

    private fun saveGroupChanges() {
        val newGroupName = groupNameEditText.text.toString().trim()
        if (newGroupName.isEmpty()) {
            Toast.makeText(this, "Group name cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedContacts.isEmpty()) {
            Toast.makeText(this, "Please select at least one contact.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a new group object (either updated or brand new)
        val changedGroup = Group(newGroupName, selectedContacts.toList())

        // Prepare result intent
        val resultIntent = Intent()


        // If creating a new group, position remains -1
        if (groupPosition != -1) {
            resultIntent.putExtra("position", groupPosition)
        }
        resultIntent.putExtra("newGroup", changedGroup)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
