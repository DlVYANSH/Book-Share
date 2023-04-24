package com.example.bookstore

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView

    companion object{
        var currentFragment: Fragment = HomeFragment()
        const val PICK_PHOTO_CODE = 321
        const val CAMERA_REQUEST_CODE = 111
        const val HOME_FRAGMENT_TAG = "home"
        const val SELL_FRAGMENT_TAG = "sell"
        const val CHAT_FRAGMENT_TAG = "chat"
        const val PROFILE_FRAGMENT_TAG = "profile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        setCurrentFragment(HomeFragment(), HOME_FRAGMENT_TAG)

        handleBottomNavigation()

        handleBottomNavigationCheck()


    }

    private fun handleBottomNavigationCheck() {
        supportFragmentManager.addOnBackStackChangedListener {

            when (supportFragmentManager.findFragmentById(R.id.fragmentContainerView)!!::class.simpleName) {
                HomeFragment()::class.simpleName -> {
                    bottomNavigationView.menu.getItem(0).isChecked = true
                    bottomNavigationView.visibility = View.VISIBLE
                }
                ChatFragment()::class.simpleName -> {
                    bottomNavigationView.menu.getItem(2).isChecked = true
                    bottomNavigationView.visibility = View.VISIBLE
                }
                SellFragment()::class.simpleName -> {
                    bottomNavigationView.menu.getItem(1).isChecked = true
                    bottomNavigationView.visibility = View.VISIBLE
                }
                ProfileFragment()::class.simpleName -> {
                    bottomNavigationView.menu.getItem(3).isChecked = true
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    bottomNavigationView.visibility = View.GONE
                }
            }

        }
    }

    private fun handleBottomNavigation() {

        bottom_navigation.selectedItemId = R.id.home

        bottom_navigation.setOnItemSelectedListener {
            when (it.itemId){
                R.id.home ->{
                    setCurrentFragment(HomeFragment(), HOME_FRAGMENT_TAG)
                }

                R.id.sell-> {

                    setCurrentFragment(SellFragment(), SELL_FRAGMENT_TAG)
                }

                R.id.chat -> {

                    setCurrentFragment(ChatFragment(), CHAT_FRAGMENT_TAG)
                }

               R.id.profile -> {

                   setCurrentFragment(ProfileFragment(), PROFILE_FRAGMENT_TAG)
                }

            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment, TAG: String) {

        if (fragment::class.simpleName != currentFragment::class.simpleName) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainerView, fragment, TAG)
                addToBackStack(TAG)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                commit()
            }
            currentFragment = fragment
        }
        if(fragment::class.simpleName == HomeFragment()::class.simpleName){
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            currentFragment = HomeFragment()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        when (bottomNavigationView.selectedItemId) {
            R.id.home -> currentFragment = HomeFragment()
            R.id.sell -> currentFragment = SellFragment()
            R.id.chat -> currentFragment = ChatFragment()
            R.id.profile -> currentFragment = ProfileFragment()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.log_out, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menu_logOut -> logOut()
        }
        return true
    }

    private fun logOut() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes"
            ) { _, _ ->
                Firebase.auth.signOut()
                googleSignInClient.signOut()
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No"
            ) { _, _ ->

            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    fun setActionBarTitle(title: String){
        supportActionBar?.title = title
    }
}