package com.cloudsurfers.crm

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception


class ComposeEmail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        val sendEmailBtn= findViewById<Button>(R.id.button2)
        sendEmailBtn.setOnClickListener {
            sendEmail()
        }
    }

    private fun sendEmail() { // maybe pass in these values (to,cc,subject,text) in the function definition

        // Parsing the email's text fields
        // TODO: Change these values to the correct fields in the MVP
        val subject = findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
        val message = findViewById<EditText>(R.id.editTextTextPersonName4).text
        val address: Array<String> = arrayOf(findViewById<EditText>(R.id.editTextTextPersonName2).text.toString())
        // TODO: consider CCs and BCCs, attachments

        Log.i("Send email", "")
        // val to:Array<String> = arrayOf("cloud.surf.dev@gmail.com") // get these from the contacts page, or lke a  selection box??
        //val cc:Array<String> = arrayOf("apples")
        val emailIntent = Intent(Intent.ACTION_SEND)

        // Configuring the message
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address)
        //emailIntent.putExtra(Intent.EXTRA_CC, cc)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, message)

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