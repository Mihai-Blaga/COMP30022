package com.cloudsurfers.crm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ViewMeetingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_meetings)

        val testStrings: ArrayList<String> =  ArrayList<String>()
        testStrings.add("Phil Yang")
        testStrings.add("Eren Jaeger")
        testStrings.add("Joe Mama")

    }
}

