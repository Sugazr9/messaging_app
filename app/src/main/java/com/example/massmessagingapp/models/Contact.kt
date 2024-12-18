package com.example.massmessagingapp.models

import java.io.Serializable

data class Contact(val name: String, val number: String, val type: String) : Serializable
