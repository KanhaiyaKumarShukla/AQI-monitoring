package com.example.sih.presentation.broadcast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sih.model.AqiData

class SmsReceiver : BroadcastReceiver() {

    private val Tag = "AqiReceiver"
    private val senderPhoneNumber = "+917645992680" // Replace with the sender's phone number

    override fun onReceive(context: Context, intent: Intent) {
        val bundle: Bundle? = intent.extras
        Log.d(Tag, "onReceive")

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d(Tag, "SMS received")
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>
                Log.d(Tag, pdus.toString())
                for (pdu in pdus) {
                    if (pdu != null) {
                        handleSmsMessage(context, pdu)
                    }
                }
            } else {
                Log.d(Tag, "Bundle is null")
            }
        }
    }

    private fun handleSmsMessage(context: Context, pdu: Any) {
        val message = SmsMessage.createFromPdu(pdu as ByteArray)
        val sender = message.displayOriginatingAddress
        val body = message.messageBody

        Log.d(Tag, "Sender: $sender, Message: $body")
        if (sender == senderPhoneNumber) {
            Log.d(Tag, "Message is from the desired sender")
            processMessageBody(context, body)
        } else {
            Log.d(Tag, "Sender is not the desired sender")
        }
    }

    private fun processMessageBody(context: Context, body: String) {
        try {
            val dataMap = parseMessageBody(body)
            val aqiData = createAqiData(dataMap)
            Log.d(Tag, "createAqiData:\n$aqiData")

            // Broadcast the parsed AQI data
            val broadcastIntent = Intent("AqiDataReceived").apply {
                putExtra("country", aqiData.country)
                putExtra("state", aqiData.state)
                putExtra("city", aqiData.city)
                putExtra("pm25", aqiData.pm25)
                putExtra("pm10", aqiData.pm10)
                putExtra("so2", aqiData.so2)
                putExtra("co", aqiData.co)
                putExtra("o3", aqiData.o3)
                putExtra("no2", aqiData.no2)
                putExtra("lastUpdatedTime", aqiData.timeStamp)
            }

            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
            Log.d(Tag, "AQI data broadcasted")

        } catch (e: Exception) {
            Log.e(Tag, "Error processing AQI data: ${e.message}")
        }
    }

    private fun parseMessageBody(body: String): Map<String, String> {
        val dataMap = mutableMapOf<String, String>()
        val lines = body.split("\n")
        for (line in lines) {
            val parts = line.split(": ")
            Log.d("element", parts.toString())
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = if (parts[1].contains(",")) {
                    parts[1].split(",").joinToString("\n") { it.trim() }
                } else {
                    parts[1].trim()
                }
                dataMap[key] = value
                Log.d("element", "key=$key, \nvalue=$value")
            }
        }
        Log.d(Tag, "parsed Message:\n${dataMap.toString()}")
        return dataMap
    }

    private fun createAqiData(dataMap: Map<String, String>): AqiData {
        return AqiData(
            country = dataMap["country"] ?: "",
            state = dataMap["state"] ?: "",
            city = dataMap["city"] ?: "",
            pm25 = dataMap["pm25"] ?: "",
            pm10 = dataMap["pm10"] ?: "",
            so2 = dataMap["so2"] ?: "",
            co = dataMap["co"] ?: "",
            o3 = dataMap["o3"] ?: "",
            no2 = dataMap["no2"] ?: "",
            timeStamp = dataMap["lastUpdatedTime"] ?: ""
        )
    }
}
