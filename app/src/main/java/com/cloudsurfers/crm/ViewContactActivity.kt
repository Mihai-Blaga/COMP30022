package com.cloudsurfers.crm

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.DatabaseUtils
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.MotionEvent
import android.view.View
import android.widget.*

class ViewContactActivity : AppCompatActivity() {
    private val hideHandler = Handler()

    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (this.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            val requestCode = 1;
            this.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), requestCode)
        }

        setContentView(R.layout.view_contact)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        val name_txt = findViewById<EditText>(R.id.name_text)
        val mobile_text = findViewById<EditText>(R.id.mobile_text)
        val email_text = findViewById<EditText>(R.id.email_text)
        val notes_text = findViewById<EditText>(R.id.notes_text)


        // A "projection" defines the columns that will be returned for each row
        val mProjection: Array<String> = arrayOf(
            ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1
        )

        val mCursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            mProjection,
            null,
            emptyArray<String>(),
            null
        )

        // Some providers return null if an error occurs, others throw an exception
        when (mCursor?.count) {
            null -> {
                val errormsg = "Error connecting to contacts"
                name_txt.setText(errormsg)
                /*
                 * Insert code here to handle the error. Be sure not to use the cursor!
                 * You may want to call android.util.Log.e() to log this error.
                 *
                 */
            }
            0 -> {
                val errormsg = "Contact not found"
                name_txt.setText(errormsg)
                /*
                 * Insert code here to notify the user that the search was unsuccessful. This isn't
                 * necessarily an error. You may want to offer the user the option to insert a new
                 * row, or re-type the search term.
                 */
            }
            else -> {
                mCursor.moveToFirst()

                var email = ""
                var name = ""
                var phone = ""
                var note = ""

                for (i in 0..mCursor.count - 1){
                    mCursor.moveToPosition(i)
                    when(mCursor.getString(1)) {
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                            phone = mCursor.getString(2) ?: "Phone not found"
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                            email = mCursor.getString(2) ?: "Email not found"
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE ->
                            name = mCursor.getString(2) ?: "Name not found"
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                            note = mCursor.getString(2) ?: "Note not found"
                    }
                }

                name_txt.setText(name)
                mobile_text.setText(phone)
                email_text.setText(email)
                notes_text.setText(note)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
    }

    private fun show() {
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}