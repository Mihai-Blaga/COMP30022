package com.cloudsurfers.crm.functions

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudsurfers.crm.R
import java.lang.Exception


class ComposeEmail {

    companion object {
        fun getSendEmailIntent(address: String): Intent {
            val subject = ""
            val message = ""

            val emailIntent = Intent(Intent.ACTION_SEND)

            // Configuring the message
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.type = "message/rfc822"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            emailIntent.putExtra(Intent.EXTRA_TEXT, message)

            return emailIntent
        }
    }
}