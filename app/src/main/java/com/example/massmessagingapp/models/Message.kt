package com.example.massmessagingapp.models

import java.io.Serializable
import java.util.Date

data class Message(val id: String, val content: String, val timestamp: Date,
                   val recipients: List<Contact>, var status: Boolean, val failedRecipients: List<Contact>) : Serializable