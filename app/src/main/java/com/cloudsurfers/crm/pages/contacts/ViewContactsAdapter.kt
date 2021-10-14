package com.cloudsurfers.crm.pages.contacts

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.*
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.Contact
import kotlin.collections.ArrayList


class ViewContactsAdapter(private val contacts: ArrayList<Contact>):
    RecyclerView.Adapter<ViewContactsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    @RequiresApi(Build.VERSION_CODES.N)
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactNameTextView: TextView = view.findViewById(R.id.view_contacts_list_item_contact_name_text_view)
        val contactImageView: ImageView = view.findViewById(R.id.view_contacts_list_item_icon_imageView)
        lateinit var contact: Contact

        init {
            // Define click listener for the ViewHolder's View.
            view.setOnClickListener {
                val activity: AppCompatActivity = view.context as AppCompatActivity
//                val c: Contact = Contact.readContact(contact, activity)
                val c: Contact = contact

                val bundle = bundleOf("name" to c.name, "email" to c.email, "mobile" to c.phone, "notes" to c.note, "tags" to c.getGroupNames(activity))
                println(c.getGroupNames(activity))
                Navigation.findNavController(view).navigate(R.id.action_viewContactsList_to_viewContactFragment, bundle)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view_contacts_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.contactNameTextView.text = contacts[position].name
        viewHolder.contact = contacts[position]

        // Draws the icon in the ImageView
        drawIcons(viewHolder.contactImageView, contacts[position].name?.substring(0,1))

    }

    // Draws the icons into the ImageView in the layout page
    private fun drawIcons(imageView: ImageView, letter: String?) {

        // For drawing the circle
        val bitmap = Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = chooseColour(letter)
        paint.isAntiAlias = true

        // For drawing the letter
        val textPaint = Paint()
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 20f
        textPaint.setARGB(255,255,255,255)
        textPaint.isFakeBoldText = true

        // Actually rendering the icon
        canvas.drawCircle(25f,25f,25f, paint)
        if (letter != null) {
            canvas.drawText(letter,24.5f,31.5f,textPaint)
        }
        imageView.setImageBitmap(bitmap)

    }

    // Chooses the colour of the icon from a predefined array of the colours
    private fun chooseColour(letter: String?): Int {

        // Chooses which colour to pick
        var charNum = 0
        if (letter != null && letter != "")
            charNum = letter[0].code.rem(7)

        val colours = arrayOf(Color(103, 159, 56).toArgb(),
            Color(177, 86, 73).toArgb(),
            Color(238, 110, 2).toArgb(),
            Color(236, 64, 122).toArgb(),
            Color(242, 81, 29).toArgb(),
            Color(93, 65, 56).toArgb(),
            Color(81, 47, 170).toArgb()
//            Color(, , ).toArgb() Format for adding for colours
        )

        return colours[charNum]
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = contacts.size
}
