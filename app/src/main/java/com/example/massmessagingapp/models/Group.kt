package com.example.massmessagingapp.models

import java.io.Serializable

data class Group(val name: String, val contacts: List<Contact>) : Serializable