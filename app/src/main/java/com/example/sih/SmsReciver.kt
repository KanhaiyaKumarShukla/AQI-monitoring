package com.example.sih

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SmsReceiver : BroadcastReceiver() {

    private val Tag = "SmsReceiver"
    private val senderPhoneNumber = "+919334993909" // Replace with the sender's phone number

    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        Log.d(Tag, "onReceive")
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d(Tag, "SMS received")
            // Will do stuff with message here
            //}
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>
                Log.d(Tag, pdus.toString())
                for (pdu in pdus) {
                    val message = SmsMessage.createFromPdu(pdu as ByteArray)
                    val sender = message.displayOriginatingAddress
                    val body = message.messageBody

                    Log.d(Tag, "Sender: $sender, Message: $body")
                    // Check if the message is from the desired sender
                    if (sender == senderPhoneNumber) {
                        // Broadcast the message to MainActivity
                        Log.d(Tag, "message is from the desired sender")
                        val broadcastIntent = Intent("SmsMessageReceived")
                        broadcastIntent.putExtra("message", body)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
                    } else {
                        Log.d(Tag, "sender is not desired sender")
                    }
                }
            } else {
                Log.d(Tag, "bundle is null")
            }
        }
    }
}
