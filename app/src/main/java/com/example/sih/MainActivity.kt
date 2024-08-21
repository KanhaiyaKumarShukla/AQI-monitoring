package com.example.sih

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.sih.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val Tag="SmsReceiver"
    private val SMS_PERMISSION_CODE = 1
    private val senderPhoneNumber = "+919755666218"

//    private val smsReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val message = intent?.getStringExtra("message")
//            message?.let {
//                binding.tv.append("New SMS: $it\n")
//            }
//        }
//    }

    private fun checkForSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            ), SMS_PERMISSION_CODE)
        } else {
            Log.d(Tag, "Request granted in check")
            readSmsMessages()
            //LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, IntentFilter("SmsMessageReceived"))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(Tag, "Request granted in request permission")
            readSmsMessages()
            //LocalBroadcastManager.getInstance(this).registerReceiver(smsReceiver, IntentFilter("SmsMessageReceived"))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkForSmsPermission()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to prevent memory leaks
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(smsReceiver)
    }
    private fun readSmsMessages() {
        val smsList = mutableListOf<String>()
        val uri = Uri.parse("content://sms/inbox")

        // Selection criteria to filter messages from a specific sender
        val selection = "address = ?"
        val selectionArgs = arrayOf(senderPhoneNumber)

        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        if (cursor != null && cursor.moveToFirst()) {
            val indexBody = cursor.getColumnIndex("body")
            val indexAddress = cursor.getColumnIndex("address")

            do {
                val address = cursor.getString(indexAddress)
                val body = cursor.getString(indexBody)
                smsList.add("Sender: $address\nMessage: $body")
            } while (cursor.moveToNext())

            cursor.close()
        }

        // Display or use the SMS messages
        for (sms in smsList) {
            binding.tv.append(sms+"\n\n")
        }
    }

}
