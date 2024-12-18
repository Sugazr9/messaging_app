package com.example.massmessagingapp.helpers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SMSHandler(private val context: Context) {

    private var smsStatusReceiver: BroadcastReceiver? = null

    companion object {
        // Define broadcast actions
        const val SENT_ACTION = "com.example.massmessagingapp.SMS_SENT"
        const val DELIVERY_ACTION = "com.example.massmessagingapp.SMS_DELIVERED"
    }


    // Function to send SMS
    fun sendSMS(phoneNumber: String, message: String, contactID: Int, messageID: String, callback: (String) -> Unit) {

//        Log.d("SMSHandler", "$phoneNumber           $contactID")
//        return
        val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
        val smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)

        val messageParts = smsManager.divideMessage(message)
        val sentIntents = ArrayList<PendingIntent>()
        val deliveryIntents = ArrayList<PendingIntent>()

        val sentAction = SENT_ACTION  + "_${messageID}_${contactID}"
        val deliveryAction = DELIVERY_ACTION + "_${messageID}_${contactID}"

        // Set up unique PendingIntent for each message using the service and actions
        val sentIntent = PendingIntent.getService(
            context, 0, Intent(context, SMSStatusService::class.java).setAction(sentAction),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val deliveryIntent = PendingIntent.getService(
            context, 0, Intent(context, SMSStatusService::class.java).setAction(deliveryAction),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        repeat(messageParts.size) {
            sentIntents.add(sentIntent)
            deliveryIntents.add(deliveryIntent)
        }

        // Register the BroadcastReceiver to listen for SMS status
        registerSMSStatusReceiver(contactID, messageID, messageParts.size, callback)

        try {
            smsManager.sendMultipartTextMessage(phoneNumber, null, messageParts, sentIntents, deliveryIntents)
        } catch (e: Exception) {
            callback("SMS Error: ${e.message}")
        }
    }

    // Register the BroadcastReceiver for SMS status updates
    private fun registerSMSStatusReceiver(contactID: Int, messageID: String, msgParts: Int, callback: (String) -> Unit) {
        var sentCount = 0
        var receivedCount = 0
        val messages = arrayOf("", "")

        fun interpretMessage(msg: String, type: String) {
            val msgI = if (type == "sent") 0 else 1
            if (msg.contains("SMS Error")) {
                if (messages[msgI].isNotEmpty()) {
                    val condErrorMsg =
                        msg.split("SMS Error: ").getOrElse(1) { msg }
                    messages[msgI] += ", $condErrorMsg"
                } else {
                    messages[msgI] = msg
                }
            }
        }

        smsStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("StatusReceiver", intent.action!!)
                val message = intent.getStringExtra(SMSStatusService.LOCAL_BROADCAST_MESSAGE)
                    ?: "Unknown message"
                val type =
                    intent.getStringExtra(SMSStatusService.LOCAL_BROADCAST_TYPE) ?: "unknown type"
                // Pass the message to the callback function
                if (type == "unknown" || message == "Unknown message") {
                    callback("StatusService received weird broadcast")
                    return
                }
                if (type == "sent") {
                    interpretMessage(message, type)
                    sentCount++
                    if (sentCount == msgParts) {
                        callback(messages[0].ifEmpty { "Message sent successfully" })
                    }
                } else if (type == "delivered") {
                    interpretMessage(message, type)
                    receivedCount++
                    if (receivedCount == msgParts) {
                        callback(messages[1].ifEmpty { "Message delivered successfully!" })
                    }
                } else {
                    callback("SMS Error: Problems with local broadcasting!")
                }
            }
        }
        val filter = IntentFilter(SMSStatusService.SMS_STATUS_ACTION + "_${messageID}_${contactID}")
        LocalBroadcastManager.getInstance(context).registerReceiver(smsStatusReceiver!!, filter)
    }

    // Unregister the BroadcastReceiver to avoid memory leaks
    fun unregisterSMSStatusReceiver() {
        smsStatusReceiver?.let {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(it)
            smsStatusReceiver = null
        }
    }
}
