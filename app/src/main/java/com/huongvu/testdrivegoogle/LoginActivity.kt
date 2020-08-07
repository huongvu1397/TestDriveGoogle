package com.huongvu.testdrivegoogle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    internal var mGoogleSignInClient: GoogleSignInClient? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null


    private var RC_SIGN_IN = 1312

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(
            Scope("https://www.googleapis.com/auth/drive")
        ).build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        val accountAccount = lastSignedInAccount?.account

        if(lastSignedInAccount == null){
            startActivityForResult(mGoogleSignInClient?.signInIntent, RC_SIGN_IN)
        }else{
            getAllImages()
        }

        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val result = GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener {
                        Log.e("HVV1312"," result success")
                }.addOnFailureListener {
                    Log.e("HVV1312"," result fail")
                }.getResult(ApiException::class.java)
                // Signed in successfully, show authenticated UI.
                handleSignInResult(result)
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.e("HVV1312"," ApiException REQUEST_ACCOUNT_YOUTUBE_PICKER : status code = ${e.statusCode} message : ${e.message} ")
                when (e.statusCode) {
                    CommonStatusCodes.NETWORK_ERROR -> Snackbar.make(
                        btnLogin,
                        "Network not connected",
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                    CommonStatusCodes.TIMEOUT -> Snackbar.make(
                        btnLogin,
                        "Network timeout",
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                    CommonStatusCodes.DEVELOPER_ERROR -> Snackbar.make(
                        btnLogin,
                        "Developer error",
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                    CommonStatusCodes.INTERNAL_ERROR -> Snackbar.make(
                        btnLogin,
                        "Internal error",
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                    CommonStatusCodes.INVALID_ACCOUNT -> Snackbar.make(
                        btnLogin,
                        "Invalid account",
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                    CommonStatusCodes.API_NOT_CONNECTED -> Snackbar.make(
                        btnLogin,
                        "Api not connected",
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                    CommonStatusCodes.INTERRUPTED -> Snackbar.make(
                        btnLogin,
                        "Interrupted error",
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                    //else -> Snackbar.make(rootStream, "Something wrong!", BaseTransientBottomBar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleSignInResult(result: GoogleSignInAccount?) {
        Log.e("HVV1312X","handleSignInResult")
        result?.idToken
        val accName = result?.account?.name  // email
        val accType = result?.account?.type
        val personName = result?.displayName
        val personGivenName = result?.givenName
        val personFamilyName = result?.familyName
        val personEmail = result?.email
        val personId = result?.id
        val photoUrl = result?.photoUrl

        val credential = GoogleAccountCredential.usingOAuth2(
            this, setOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = result?.account
        val googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName(getString(R.string.app_name))
            .build()

        mDriveServiceHelper = DriveServiceHelper(googleDriveService)
        getAllImages()
    }

    private fun getAllImages(){
        Log.e("HVV1312","getAllImages")
        // query Image
//        mDriveServiceHelper?.queryImages()
//            ?.addOnSuccessListener { fileList ->
//                Log.e("HVV1312", "File size: ${fileList.files.size}")
//                for (file in fileList.files) {
//                    if (file.mimeType.toLowerCase().contains("image/")) {
//                        //tempList.add(DriveFile(file))
//                        Log.e("HVV1312File"," file : ${file}")
//                    }
//                }
//
//            }
//            ?.addOnFailureListener { exception: Exception ->
//
//                exception.printStackTrace()
//                Log.e("HVV1312","fail get image $exception")
//                if((exception is UserRecoverableAuthIOException)){
//                    startActivityForResult((exception as UserRecoverableAuthIOException) .intent, 69)
//                }
//            }
        // searchImage
        GlobalScope.launch(Dispatchers.IO){
            mDriveServiceHelper?.searchImage()
        }
    }
}