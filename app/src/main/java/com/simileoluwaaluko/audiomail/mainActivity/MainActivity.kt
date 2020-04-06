package com.simileoluwaaluko.audiomail.mainActivity


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.simileoluwaaluko.audiomail.R
import com.simileoluwaaluko.audiomail.RegisterActivity
import com.simileoluwaaluko.audiomail.inbox.InboxActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity(){
    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val host = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment? ?: return
        val navController = host.navController

        setUpBottomNav(navController)
        mainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
    }

    private fun setUpBottomNav(navController : NavController){
        bottom_nav_view.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            for(menuItem in bottom_nav_view.menu.iterator()){
                menuItem.isEnabled = true
            }
            val menu = bottom_nav_view.menu.findItem(destination.id)
            menu?.isEnabled = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.titlebar_title, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit_gmail_info -> {
                startActivity<RegisterActivity>()
            }
            R.id.inbox -> {
                startActivity<InboxActivity>()
            }
        }
        return true
    }
}