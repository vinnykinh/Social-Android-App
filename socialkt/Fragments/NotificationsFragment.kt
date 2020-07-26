package com.example.socialkt.Fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.example.socialkt.R



class NotificationsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view  = inflater.inflate(R.layout.fragment_notifications, container, false)

        val toolbar:Toolbar =view.findViewById(R.id.notification_toolbar)
        toolbar.title = "Notifications"


        return view
    }


}
