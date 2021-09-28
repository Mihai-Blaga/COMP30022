package com.cloudsurfers.crm.pages.login

import android.R.attr
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import com.cloudsurfers.crm.databinding.FragmentViewContactBinding
import com.cloudsurfers.crm.databinding.LoginPageBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import android.R.attr.data
import android.util.Log
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.pages.main.MainActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = LoginPageBinding.inflate(layoutInflater);

        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        val account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account)
        resultLauncher = this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)

            }
        }

        binding.loginButton.setOnClickListener{
            signIn()
        }
        setContentView(binding.root)
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            print(e)
            updateUI(null)
        }
    }

    private fun updateUI(account : GoogleSignInAccount?){
        if (account != null){
            // Save to SharedPref, which can be used later for filtering Calendar or Contacts
            val sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString("email", account.email)
                putString("id", account.id)
                apply()
            }

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}