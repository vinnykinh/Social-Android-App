package com.example.socialkt

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.socialkt.Fragments.HomeFragment
import com.example.socialkt.Fragments.NotificationsFragment
import com.example.socialkt.Fragments.ProfileFragment
import com.example.socialkt.Fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val bottomNavigationView  = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigationView.setOnNavigationItemSelectedListener(onItemSelectedListener)
        moveToFragment(HomeFragment())



    }
    private val onItemSelectedListener =BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val itemId = item.itemId
        when(itemId){
         R.id.menu_home ->{
             moveToFragment(HomeFragment())

                return@OnNavigationItemSelectedListener true
            }
            R.id.search_menu ->{
                moveToFragment(SearchFragment())
                return@OnNavigationItemSelectedListener  true
            }
            R.id.addPosts_menu ->{
                val intent =Intent(this,AddPostsActivity::class.java)
                startActivity(intent)

                return@OnNavigationItemSelectedListener true
            }
            R.id.profile_menu ->{
                moveToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.notification ->{
                moveToFragment(NotificationsFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    private fun moveToFragment(fragment: Fragment?){
        val fm =  supportFragmentManager.beginTransaction()
        fm.replace(R.id.fragmentContainer , fragment!!).commit()
    }



}
