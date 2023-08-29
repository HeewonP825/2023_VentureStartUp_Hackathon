package com.hackathon.gameselect
import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.hackathon.gameselect.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val AUTH_SCOPE = "https://www.googleapis.com/auth/youtube" // 사용자 인증 스코프
    private lateinit var accountManager: AccountManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleLoginLauncher: ActivityResultLauncher<Intent>

    //firebase
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var firebaseAuth: FirebaseAuth

    private var email: String = ""
    private var tokenId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseAuth = FirebaseAuth.getInstance()
        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), ActivityResultCallback { result ->
                Log.e(TAG, "resultCode : ${result.resultCode}")
                Log.e(TAG, "result : $result")
                if (result.resultCode == RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        task.getResult(ApiException::class.java)?.let { account ->
                            tokenId = account.idToken
                            if (tokenId != null && tokenId != "") {
                                val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                                firebaseAuth.signInWithCredential(credential)
                                    .addOnCompleteListener {
                                        if (firebaseAuth.currentUser != null) {
                                            val user: FirebaseUser = firebaseAuth.currentUser!!
                                            email = user.email.toString()
                                            Log.e(TAG, "email : $email")
                                            val googleSignInToken = account.idToken ?: ""
                                            if (googleSignInToken != "") {
                                                Log.e(TAG, "googleSignInToken : $googleSignInToken")
                                            } else {
                                                Log.e(TAG, "googleSignInToken이 null")
                                            }
                                        }
                                    }
                            }
                        } ?: throw Exception()
                    }   catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

        binding.googleLoginBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)
                val signInIntent: Intent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        }

        binding.sendDataBtn.setOnClickListener {

            accountManager = AccountManager.get(this)

            // 실제 사용할 Account 객체를 얻어와서 myAccount에 할당
            val myAccount = getMyAccount()
            Log.e("", "${myAccount}")
            myAccount?.let {
                getAuthToken(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }
    private fun getMyAccount(): Account? {

        val accounts = accountManager.getAccountsByType("com.google")
        return if (accounts.isNotEmpty()) {
            accounts[0]
        } else {
            null
        }
    }

    private fun getAuthToken(account: Account) {
        val options = Bundle()

        accountManager.getAuthToken(
            account,
            AUTH_SCOPE,
            options,
            this,
            OnTokenAcquired(),
            handler
        )
    }

    private inner class OnTokenAcquired : AccountManagerCallback<Bundle> {
        override fun run(future: AccountManagerFuture<Bundle>) {
            try {
                val bundle = future.result
                val authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN)
                // authToken을 이용하여 필요한 작업 수행
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private inner class OnError : AccountManagerCallback<Bundle> {
        override fun run(future: AccountManagerFuture<Bundle>) {
            // 에러 처리를 여기에 구현
        }
    }

    inner class ErrorHandler : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            // 에러 처리를 여기에 구현
            return true
        }
    }
    val errorHandler = ErrorHandler()
    val handler = Handler(errorHandler)

}
