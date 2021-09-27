package com.cloudsurfers.crm.pages.contacts

import android.graphics.Bitmap
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.Contact
import java.util.*
import kotlin.collections.ArrayList
import android.text.StaticLayout

import android.text.TextPaint





class ViewContactsAdapter(private val contacts: ArrayList<Contact>) :
    RecyclerView.Adapter<ViewContactsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactNameTextView: TextView = view.findViewById(R.id.view_contacts_list_item_contact_name_text_view)
        val contactImageView: ImageView = view.findViewById(R.id.view_contacts_list_item_icon_imageView)
        lateinit var contact: Contact

        init {
            // Define click listener for the ViewHolder's View.
            view.setOnClickListener {
                val activity: AppCompatActivity = view.context as AppCompatActivity
                val c: Contact = Contact.readContact(contact, activity)

                val bundle = bundleOf("name" to c.name, "email" to c.email, "mobile" to c.phone, "notes" to c.note)

                Navigation.findNavController(view).navigate(R.id.action_viewContactsList_to_viewContactFragment, bundle)

//                activity.supportFragmentManager.commit {
//                    setReorderingAllowed(true)
//
//                    val c: Contact = Contact.readContact(contact, activity)
//
//                    // Replace whatever is in the fragment_container view with this fragment
//
//
//                    replace(R.id.nav_host_fragment_container, ViewContactFragment.newInstance(
//                        c.name!!, c.email!!, c.phone!!, c.note!!))
//                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
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

        // For drawing the circles
//        val tempBitmap = ImageBitmap(50,50, ImageBitmapConfig.Argb8888)
//        val canvas = Canvas(tempBitmap)
//        val paint = Paint()
        val rand = Random()

        // Using a different library
        val bitmap2 = Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(bitmap2)
        val paint2 = Paint()

        // Letter to appear in the centre of the circle
        val letter: String? = contacts[position].name?.substring(0,1)

//        paint.color = Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)).toArgb()
        paint2.color = Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)).toArgb()

//        canvas.drawCircle(Offset(25f,25f),25f, paint)
        canvas2.drawCircle(25f,25f,25f, paint2)
        if (letter != null) {
            canvas2.drawText(letter,25f,25f,Paint())
        }

//        val textPaint = TextPaint()
//        textPaint.isAntiAlias = true
//        textPaint.textSize = 16f * R.style.Theme_MaterialComponents_Dialog_FixedSize
//        textPaint.color = -0x1000000
//
//        val width = textPaint.measureText(letter).toInt()
//        val staticLayout = StaticLayout(
//            letter, textPaint,
//            width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false
//        )
//        staticLayout.draw(canvas2)

//        viewHolder.contactImageView.setImageBitmap(tempBitmap.asAndroidBitmap())
        viewHolder.contactImageView.setImageBitmap(bitmap2)

        Log.i("debug",viewHolder.contactImageView.matrix.toString()+"   ,   "+letter)



    }

    fun drawCircle() {

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = contacts.size

}
