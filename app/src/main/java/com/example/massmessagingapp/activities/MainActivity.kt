package com.example.massmessagingapp.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.massmessagingapp.R
import com.example.massmessagingapp.adapters.GroupAdapter
import com.example.massmessagingapp.adapters.MessageHistoryAdapter
import com.example.massmessagingapp.helpers.SMSHandler
import com.example.massmessagingapp.models.Contact
import com.example.massmessagingapp.models.Group
import com.example.massmessagingapp.models.Message
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Runnable
import java.util.Date
import java.util.UUID

class MainActivity : Activity() {

    private lateinit var groupViewIcon: ImageButton
    private lateinit var historyViewIcon: ImageButton
    private var groupAdapter: GroupAdapter? = null
    private val messageHistoryList: MutableList<Message> = mutableListOf()
    private val groupList: MutableList<Group> = mutableListOf()

    private val colorIconActivated by lazy {
        resources.getColor(R.color.colorAccent, theme)
    }
    private val colorIconDeactivated by lazy {
        resources.getColor(R.color.colorMinor, theme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure permissions are granted
        checkAndRequestPermissions()
        setContentView(R.layout.activity_main)

        // Load groups and message history (retrieve or initialize)
        loadGroups()
        loadMessageHistory()

        // Activation and deactivation colors
        val colorButtonDeactivated = resources.getColor(R.color.colorNeutral, theme)
        val colorButtonActivated = resources.getColor(R.color.colorPrimary, theme)
        val colorIconActivated = resources.getColor(R.color.colorAccent, theme)
        val colorIconDeactivated = resources.getColor(R.color.colorMinor, theme)

        // Initialize top toolbar
        groupViewIcon = findViewById(R.id.btn_group_view)
        historyViewIcon = findViewById(R.id.btn_history_view)

        // Group Screen Button Click
        groupViewIcon.setOnClickListener {
            // Load or refresh the group screen
            loadGroupContent()
            groupViewIcon.setBackgroundColor(colorButtonActivated)
            historyViewIcon.setBackgroundColor(colorButtonDeactivated)
            groupViewIcon.setColorFilter(colorIconActivated)
            historyViewIcon.setColorFilter(colorIconDeactivated)
        }

        // Message History Button Click
        historyViewIcon.setOnClickListener {
            // Navigate to the history screen
            loadMessageHistoryContent()
            historyViewIcon.setBackgroundColor(colorButtonActivated)
            groupViewIcon.setBackgroundColor(colorButtonDeactivated)
            groupViewIcon.setColorFilter(colorIconDeactivated)
            historyViewIcon.setColorFilter(colorIconActivated)
        }

        loadGroupContent() // Load group content on initialization

    }

    // Handle Permissions
    private fun checkAndRequestPermissions() {
        // List of permissions to check and request
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS
        )

        // Check if any of the permissions are not granted
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        // If there are any missing permissions, request them
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if all permissions are granted
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // All permissions granted, you can proceed
            } else {
                // Handle case where some permissions are denied
                Toast.makeText(this, "Permissions are required for this app to function", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // load the group screen
    private fun loadGroupContent() {
        // Initial cleanup and setup
        val container = findViewById<FrameLayout>(R.id.content_container)
        container.removeAllViews() // Clear previous views
        var selectedGroup: Group? = null // Tracking selected group
        layoutInflater.inflate(R.layout.layout_group_content, container)


        // Initialize views
        val groupRecyclerView = container.findViewById<RecyclerView>(R.id.recycler_view_groups)
        val addGroupButton = container.findViewById<Button>(R.id.btn_add_group)
        val messageEditText = container.findViewById<EditText>(R.id.et_message)
        val sendButton = container.findViewById<ImageButton>(R.id.btn_send)

        fun sendMessageCheck() {
            // Activation and deactivation colors
            val colorButtonDeactivated = resources.getColor(R.color.colorNeutral, theme)
            val colorButtonActivated = resources.getColor(R.color.colorPrimary, theme)
            val colorIconActivated = resources.getColor(R.color.colorAlert, theme)
            val colorIconDeactivated = resources.getColor(R.color.colorMinor, theme)

            if (messageEditText.text.toString() != "" && selectedGroup != null) {
                sendButton.backgroundTintList = ColorStateList.valueOf(colorButtonActivated)
                sendButton.setColorFilter(colorIconActivated)
            } else {
                sendButton.backgroundTintList = ColorStateList.valueOf(colorButtonDeactivated)
                sendButton.setColorFilter(colorIconDeactivated)
            }
        }

        // Set up the RecyclerView
        groupRecyclerView.layoutManager = LinearLayoutManager(this)
        groupAdapter = GroupAdapter(groupList, { currSelectedGroup ->
            selectedGroup = onGroupSelected(currSelectedGroup, selectedGroup) // Handle group selection
            sendMessageCheck()
        }, { currSelectedGroup, action ->
            handleGroupAction(currSelectedGroup, action)  // Handle long-click actions (Edit/Delete)
        })
        groupRecyclerView.adapter = groupAdapter

        // Checking changes in message text
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendMessageCheck()
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        // Add Group Button Click
        addGroupButton.setOnClickListener {
            val intent = Intent(this, UpdateGroupActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MOD_GROUP)
        }

        // Send Button Click
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (selectedGroup != null && message.isNotBlank()) {
                val randomContacts = selectedGroup!!.contacts.shuffled()
                sendMessages(randomContacts, message, true)
                messageEditText.text.clear()
            } else {
                Toast.makeText(this, "Please select a group and enter a message.",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }

    // load the message history content
    private fun loadMessageHistoryContent() {
        // Initial cleanup and setup
        val container = findViewById<FrameLayout>(R.id.content_container)
        container.removeAllViews() // Clear previous views
        layoutInflater.inflate(R.layout.layout_message_history_content, container)

        // Initialize RecyclerView for message history
        val historyRecyclerView = container.findViewById<RecyclerView>(R.id.recycler_view_message_history)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up adapter for RecyclerView
        messageHistoryList.sortBy { it.timestamp }
        messageHistoryList.reverse()
        val messageHistoryAdapter = MessageHistoryAdapter(messageHistoryList) { message ->
            val intent = Intent(this, MessageDetailActivity::class.java).apply {
                putExtra("message", message)
            }
            startActivity(intent)
        }
        historyRecyclerView.adapter = messageHistoryAdapter
    }

    // Handle the result from UpdateGroupActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_MOD_GROUP -> {
                if (resultCode == RESULT_OK && data != null) {
                    val position = data.getIntExtra("position", -1)
                    val newGroup = data.getSerializableExtra("newGroup", Group::class.java)
                    if (position != -1) {
                        val oldGroupName = groupList[position].name
                        val newGroupName = newGroup!!.name
                        groupList[position] = newGroup
                        groupAdapter!!.notifyItemChanged(position)
                        val displayMessage = if (oldGroupName != newGroupName) {
                            Html.fromHtml(
                                "Group <b>${oldGroupName}</b> updated to <b>${newGroupName}</b>",
                                Html.FROM_HTML_MODE_LEGACY
                            )
                        } else {
                            Html.fromHtml(
                                "Group <b>${newGroup.name}</b> updated",
                                Html.FROM_HTML_MODE_LEGACY
                            )
                        }
                        Toast.makeText(this, displayMessage, Toast.LENGTH_SHORT).show()
                    }
                    else {
                        groupList.add(newGroup!!)
                        groupAdapter!!.notifyItemInserted(groupList.size - 1)
                        val displayMessage = Html.fromHtml("Group <b>${newGroup.name}</b> added", Html.FROM_HTML_MODE_LEGACY)
                        Toast.makeText(this, displayMessage, Toast.LENGTH_SHORT).show()
                    }
                    saveGroups()
                }
            }
            REQUEST_CODE_RETRY_FAILED_CONTACTS -> {
                if (resultCode == RESULT_OK) {
                    val retryContacts = data?.getSerializableExtra("retryContacts") as? List<Contact> ?: return
                    val messageContent = data.getStringExtra("messageContent")
                    sendMessages(retryContacts, messageContent!!, false)
                }
            }
        }
    }

    // Lock screen orientation based on current configuration
    private fun lockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    // Unlock screen orientation to follow user preference
    private fun unlockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    // Functions to used for sending messages
    private fun sendMessages(contacts: List<Contact>, messageContent: String, retryEnabled: Boolean) {
        // Setup for progress dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress_bar)
        val progressPercentage = dialogView.findViewById<TextView>(R.id.tv_progress_percentage)
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        val failedContacts = mutableListOf<Contact>()
        val errorMessages = mutableListOf<String>()
        val messageStatusCounts = IntArray(contacts.size) { 0 }
        var confirmedContacts = 0
        val throttlingHandler = Handler(Looper.getMainLooper())
        val timeoutHandler = Handler(Looper.getMainLooper())
        val smsHandler = SMSHandler(this)
        val delayRange = 1000L..5000L

        val messageId = UUID.randomUUID().toString()
        val timestamp = Date()
        val message = Message(messageId, messageContent, timestamp, contacts, false, failedContacts)
        messageHistoryList.add(message)
        saveMessageHistory()
        alertDialog.show()
        progressBar.max = contacts.size
        lockOrientation()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        fun interpretMessage(msg: String, contactI: Int, contact: Contact) {
            val timedOut = messageStatusCounts[contactI] == STATUS_TIMED_OUT
            if (timedOut) {
                Log.d("TimeoutTesting", "${contact.name} already timed out!")
            }
            if (msg.contains("SMS Error")) {
                if (failedContacts.contains(contact)) {
                    val id = failedContacts.indexOf(contact)
                    val condErrorMsg =
                        msg.split("SMS Error: ").getOrElse(1) { msg }
                    errorMessages[id] += ", $condErrorMsg"
                } else {
                    failedContacts.add(contact)
                    errorMessages.add(msg)
                }
            } else if (failedContacts.contains(contact)) {
                if (!timedOut) {
                    val id = failedContacts.indexOf(contact)
                    failedContacts.removeAt(id)
                    errorMessages.removeAt(id)
                }
            }
            if (!timedOut) {
                messageStatusCounts[contactI]++
            }
        }

        fun updateProgressBar() {
            confirmedContacts++
            progressBar.progress = confirmedContacts
            val percentage = (confirmedContacts * 100) / contacts.size
            progressPercentage.text =
                getString(R.string.progress_bar_update_percent, percentage)

            // Check if all messages have been processed
            if (confirmedContacts == contacts.size) {
                alertDialog.dismiss()

                // unregister the receiver to avoid memory leaks
                smsHandler.unregisterSMSStatusReceiver()

                // Show dialog if there are any failed contacts
                if (failedContacts.isNotEmpty()) {
                    saveMessageHistory()
                    val intent = Intent(this@MainActivity,
                        FailedContactsActivity::class.java)
                    intent.putExtra("failedContacts", ArrayList(failedContacts))
                    intent.putStringArrayListExtra("errorMessages", ArrayList(errorMessages))
                    intent.putExtra("messageContent", messageContent)
                    intent.putExtra("isRetryEnabled", retryEnabled)
                    startActivityForResult(intent, REQUEST_CODE_RETRY_FAILED_CONTACTS)
                } else {
                    message.status = true
                    saveMessageHistory()
                    Toast.makeText(
                        this@MainActivity, "Messages sent successfully!",
                        Toast.LENGTH_SHORT).show()
                }
                unlockOrientation()
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        for ((index, contact) in contacts.withIndex()) {
            // Define the timeout action
            val timeoutRunnable = Runnable {
                if (messageStatusCounts[index] < 2) {  // Check if message is incomplete
                    val logMessage = "Timeout reached for contact $contact.name"
                    Log.d("TimeoutModule", logMessage)
                    Toast.makeText(this@MainActivity, logMessage, Toast.LENGTH_SHORT).show()
                    interpretMessage("SMS Error: Message timeout", index, contact)
                    messageStatusCounts[index] = STATUS_TIMED_OUT
                }
                updateProgressBar()
            }

            timeoutHandler.postDelayed(timeoutRunnable, MESSAGE_TIMEOUT_DURATION)
            val randomDelay = delayRange.random()
            val additionalDelay = (index / 25) * 20000
            val actualDelay = (randomDelay * index + additionalDelay) % (MESSAGE_TIMEOUT_DURATION - 120000)
            val sendRunnable = Runnable {
                smsHandler.sendSMS(contact.number, messageContent, index, messageId) { result ->
                    Log.d(
                        "CallbackFunction",
                        "Callback called for $contact.name with result: $result"
                    )
                    runOnUiThread {
                        interpretMessage(result, index, contact)
                        if (messageStatusCounts[index] == 2) {
//                            timeoutHandler.removeCallbacks(timeoutRunnable)
//                            Handler(Looper.getMainLooper()).postDelayed({
//                                updateProgressBar()
//                            }, 600000L)
                             updateProgressBar()
                        }
                    }
                }
            }

            Log.d("TimeoutTracking", "Using random delay of $randomDelay for contact num $index")
            Log.d("TimeoutTracking", "Using additional delay of $additionalDelay for contact num $index")
            Log.d("TimeoutTracking", "Using actual delay of $actualDelay for contact num $index")
            throttlingHandler.postDelayed(sendRunnable, actualDelay)
        }
    }

    private fun onGroupSelected (group: Group, prevSelGroup: Group?): Group? {
        val selectedGroup: Group? = if (prevSelGroup == group) {
            null
        } else {
            group  // Store the selected group
        }
        groupAdapter!!.updateSelectedGroup(selectedGroup)
        groupAdapter!!.notifyDataSetChanged()
        return selectedGroup
    }

    private fun handleGroupAction(group: Group, action: String) {
        when (action) {
            "edit" -> {
                // Handle group edit logic (open edit screen)
                openEditGroupScreen(group)
            }
            "delete" -> {
                // Handle group deletion logic
                confirmDeleteGroup(group)
            }
        }
    }

    private fun openEditGroupScreen(group: Group) {
        val intent = Intent(this, UpdateGroupActivity::class.java)
        intent.putExtra("group", group)
        intent.putExtra("position", groupList.indexOf(group))
        startActivityForResult(intent, REQUEST_CODE_MOD_GROUP)
    }

    private fun confirmDeleteGroup(group: Group) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Group")
        builder.setMessage("Are you sure you want to delete the group \"${group.name}\"?")
        builder.setPositiveButton("Delete") { _, _ ->
            deleteGroup(group)
            saveGroups()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun deleteGroup(group: Group) {
        val groupPos = groupList.indexOf(group)
        groupList.remove(group)
        groupAdapter!!.notifyItemRemoved(groupPos)  // Refresh the list
        val displayMessage = Html.fromHtml("Group <b>${group.name}</b> deleted", Html.FROM_HTML_MODE_LEGACY)
        Toast.makeText(this, displayMessage, Toast.LENGTH_SHORT).show()
    }

    private fun saveGroups() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(groupList)
        editor.putString("groupList", json)
        editor.apply()
        Toast.makeText(this, "Group list updated", Toast.LENGTH_SHORT).show()
    }

    private fun saveMessageHistory() {
        messageHistoryList.sortBy { it.timestamp }
        messageHistoryList.reverse()
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(messageHistoryList)
        editor.putString("messageHistoryList", json)
        editor.apply()
    }

    private fun loadGroups() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("groupList", null)
        val type = object : TypeToken<MutableList<Group>>() {}.type
        if (json != null) {
            val saveGroups: MutableList<Group> = gson.fromJson(json, type)
            groupList.addAll(saveGroups) // Add all elements from json to app list
        }
        else {
            groupList.addAll(mutableListOf())
        }
    }

    private fun loadMessageHistory() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("messageHistoryList", null)
        val type = object : TypeToken<MutableList<Message>>() {}.type
        if (json != null) {
            val saveMessageHistory: MutableList<Message> = gson.fromJson(json, type)
            messageHistoryList.addAll(saveMessageHistory) // Add all elements from json to app list
        }
        else {
            messageHistoryList.addAll(mutableListOf())
        }
    }

    companion object {
        const val REQUEST_CODE_MOD_GROUP = 101
        const val REQUEST_CODE_RETRY_FAILED_CONTACTS = 24
        const val PERMISSION_REQUEST_CODE = 13
        const val MESSAGE_TIMEOUT_DURATION = 600000L
        const val STATUS_TIMED_OUT = -1
    }
}
