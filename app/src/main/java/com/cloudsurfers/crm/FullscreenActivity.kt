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

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: FrameLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
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

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_content)
        fullscreenContent.setOnClickListener { toggle() }

        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById<Button>(R.id.button1).setOnTouchListener(delayHideTouchListener)


        val contactBtn = findViewById<Button>(R.id.button1)
        val btn2 = findViewById<Button>(R.id.button2)
        val btn3 = findViewById<Button>(R.id.button3)
        val btn4 = findViewById<Button>(R.id.button4)

        val txt1 = findViewById<EditText>(R.id.textField1)
        val txt2 = findViewById<EditText>(R.id.textField2)
        val txt3 = findViewById<EditText>(R.id.textField3)
        val txt4 = findViewById<EditText>(R.id.textField4)


        //Interacting with the first button (testing contact functionality)
        contactBtn.setOnClickListener {
            // A "projection" defines the columns that will be returned for each row
            val mProjection: Array<String> = arrayOf(
                ContactsContract.Data._ID,    // Contract class constant for the _ID column name
                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,   // Contract class constant for the name column name
                ContactsContract.CommonDataKinds.Phone.NUMBER,  // Contract class constant for the phone column name
                ContactsContract.CommonDataKinds.Email.ADDRESS
            )

            if (this.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                val requestCode = 1;
                this.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), requestCode);
            }

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
                    txt1.setText("Contact not found")
                    /*
                     * Insert code here to handle the error. Be sure not to use the cursor!
                     * You may want to call android.util.Log.e() to log this error.
                     *
                     */
                }
                0 -> {
                    txt1.setText("Contact not found")
                    /*
                     * Insert code here to notify the user that the search was unsuccessful. This isn't
                     * necessarily an error. You may want to offer the user the option to insert a new
                     * row, or re-type the search term.
                     */
                }
                else -> {
                    mCursor.moveToFirst()

                    var txt1Str = mCursor.getColumnName(0) + ": " + mCursor.getString(0)
                    var txt2Str = mCursor.getColumnName(1) + ": " + mCursor.getString(1)
                    var txt3Str = mCursor.getColumnName(2) + ": " + mCursor.getString(2)
                    var txt4Str = mCursor.getColumnName(3) + ": " + mCursor.getString(3)


                    txt2Str = mCursor.columnCount.toString()
                    txt3Str = mCursor.count.toString()
                    txt4Str = DatabaseUtils.dumpCursorToString(mCursor)

                    txt1.setText(txt1Str)
                    txt2.setText(txt2Str)
                    txt3.setText(txt3Str)
                    txt4.setText(txt4Str)
                    // Insert code here to do something with the results
                }
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
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
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