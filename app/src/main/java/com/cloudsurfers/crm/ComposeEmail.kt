package com.cloudsurfers.crm

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception


class ComposeEmail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.email)

        val sendEmailBtn: Button = findViewById(R.id.button)
        sendEmailBtn.setOnClickListener {
                sendEmail()
        }
    }

    private fun sendEmail() { // maybe pass in these values (to,cc,subject,text) in the function definition
        Log.i("Send email", "")
        val to:Array<String> = arrayOf("cloud.surf.dev@gmail.com") // get these from the app page
        //val cc:Array<String> = arrayOf("apples")
        val emailIntent = Intent(Intent.ACTION_SEND)

        emailIntent.data = Uri.parse("mailto:")
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        //emailIntent.putExtra(Intent.EXTRA_CC, cc)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Test Subject 1") // <--- Need to change these to text fields in the page that are assigned to vars.
        emailIntent.putExtra(Intent.EXTRA_TEXT, R.id.inputField) // ---^

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail using..."))
            finish()
            Log.i("Finished sending email.", "")
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    fun buttonOnClick(v: View) {
        sendEmail()
    }

}