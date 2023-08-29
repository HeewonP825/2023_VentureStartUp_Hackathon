package com.hackathon.gameselect

import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.hackathon.gameselect.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleLoginLauncher: ActivityResultLauncher<Intent>
    private lateinit var googleApiClient: GoogleApiClient

    // Firebase
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var firebaseAuth: FirebaseAuth

    private var email: String = ""
    private var tokenId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        firebaseAuth = FirebaseAuth.getInstance()
        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(), ActivityResultCallback { result ->
                if (result.resultCode == RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        account?.let { handleGoogleSignInResult(account) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

        // GoogleSignInOptions 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope("https://www.googleapis.com/auth/youtube.force-ssl"))
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // GoogleApiClient 생성
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        binding.googleLoginBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                googleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)
                val signInIntent: Intent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
//            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
//            startActivityForResult(signInIntent, RC_SIGN_IN)

        }

        binding.sendDataBtn.setOnClickListener {

        }
    }
    // onActivityResult 함수에서 사용자 정보 및 토큰을 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (result!!.isSuccess) {
                // 인증 성공
                val account = result.signInAccount
                val idToken = account?.idToken // 사용할 ID Token
                getSubscriptions(idToken)

            } else {
                val errorMessage = result.status.statusMessage
                Log.e("실패", "구글 로그인 실패: $errorMessage")
                // 인증 실패
            }
        }
    }
    fun getSubscriptions(token: String?) {

        val apiKey = token
        val apiManager = YouTubeApiManager(apiKey!!)

        apiManager.getSubscriptions(object : Callback<SubscriptionResponse> {
            override fun onResponse(call: Call<SubscriptionResponse>, response: Response<SubscriptionResponse>) {
                Log.e("response", "${response}")

                if (response.isSuccessful) {
                    val subscriptions = response.body()?.items
                    Log.e("subscriptions", "${subscriptions}")
                    // 구독 리스트를 사용하여 UI 업데이트
                } else {
                    Log.e("실패", "")

                    // API 요청이 실패한 경우 처리
                }
            }

            override fun onFailure(call: Call<SubscriptionResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }
        }, apiKey)
    }

    private fun handleGoogleSignInResult(account: GoogleSignInAccount) {
        tokenId = account.idToken
        if (tokenId != null && tokenId != "") {
            val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val user: FirebaseUser? = firebaseAuth.currentUser
                        user?.let {
                            email = user.email ?: ""
                            Log.e("email", "email : $email")
                            Log.e(TAG, "googleSignInToken : $tokenId")
                        }
                    }
                }
        }
    }


    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001

    }
}
