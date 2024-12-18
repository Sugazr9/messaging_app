package com.example.massmessagingapp.helpers

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SMSStatusService : Service() {

    companion object {
        const val SMS_STATUS_ACTION = "com.example.massmessagingapp.SMS_STATUS"
        const val LOCAL_BROADCAST_MESSAGE = "message"
        const val LOCAL_BROADCAST_TYPE = "type"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        Log.d("SMSStatusService", "action: $action")

        val splitAction = action.split("_")
        val contactId = splitAction[splitAction.size - 1] // Extract the unique contactID
        val messageId = splitAction[splitAction.size - 2]
        val resultCode = intent.getIntExtra("resultCode", -1)
        val errorMessage: String
        val type: String

        // Determine if this is a sent or delivery action
        if (action.startsWith(SMSHandler.SENT_ACTION)) {
            type = "sent"
            errorMessage = when (resultCode) {
                Activity.RESULT_OK -> "Message sent successfully."
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "SMS Error: Generic failure."
                SmsManager.RESULT_ERROR_NO_SERVICE -> "SMS Error: No service."
                SmsManager.RESULT_ERROR_NULL_PDU -> "SMS Error: Null PDU."
                SmsManager.RESULT_ERROR_RADIO_OFF -> "SMS Error: Radio off."
                else -> "SMS Error: Unknown error."
            }
//            if (contactId == "0" || contactId == "1") {
//                errorMessage = "SMS Error: Just testing sent errors"
//            }
            //errorMessage = "SMS Error: testing, testing, testing"
            //Log.d("SMSStatusService", "SMS Sent new message: $errorMessage")
        } else if (action.startsWith(SMSHandler.DELIVERY_ACTION)){
            Log.d("SMSStatusService", "SMS Delivered - Handling Delivery Action")
            type = "delivered"
            errorMessage = if (resultCode == Activity.RESULT_OK) {
                "Message delivered successfully."
            } else {
                "SMS Error: Delivery failed."
            }
//            if (contactId == "2" || contactId == "1") {
//                errorMessage = "SMS Error: Just testing delivery errors"
//            }
//              errorMessage = "SMS Error: testing, testing"
//            Log.d("SMSStatusService", "SMS Delivery new message: $errorMessage")
        } else {
            type = "unknown"
            errorMessage = "SMS Error: Unknown action received."
        }

        // Broadcast the result to the activity
        val broadcastIntent = Intent(SMS_STATUS_ACTION + "_${messageId}_${contactId}")
        broadcastIntent.putExtra(LOCAL_BROADCAST_MESSAGE, errorMessage)
        broadcastIntent.putExtra(LOCAL_BROADCAST_TYPE, type)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)

        // Stop the service after handling the broadcast
        stopSelf()
        return START_NOT_STICKY
    }
}
