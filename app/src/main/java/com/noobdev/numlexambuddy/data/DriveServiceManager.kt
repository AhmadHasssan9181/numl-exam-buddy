package com.noobdev.numlexambuddy.data

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveServiceManager(private val context: Context) {
    private val scopes = listOf(DriveScopes.DRIVE_READONLY)
    private val credentialDataStore = "numl-exam-buddy"

    suspend fun getDriveService(): Drive = withContext(Dispatchers.IO) {
        val transport = NetHttpTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        val credential = GoogleAccountCredential.usingOAuth2(
            context, scopes
        ).setBackOff(ExponentialBackOff())

        // Retrieve stored account name or prompt user to select account
        val accountName = getStoredAccount() ?: selectAccount()
        credential.selectedAccountName = accountName

        return@withContext Drive.Builder(transport, jsonFactory, credential)
            .setApplicationName("NUML Exam Buddy")
            .build()
    }

    private fun getStoredAccount(): String? {
        val preferences = context.getSharedPreferences(credentialDataStore, Context.MODE_PRIVATE)
        return preferences.getString("account_name", null)
    }

    private suspend fun selectAccount(): String = withContext(Dispatchers.Main) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
        val account = GoogleSignIn.getLastSignedInAccount(context)

        if (account != null) {
            // Save the account name for future use
            context.getSharedPreferences(credentialDataStore, Context.MODE_PRIVATE)
                .edit()
                .putString("account_name", account.email)
                .apply()

            return@withContext account.email ?: ""
        } else {
            // This should be handled in the UI with a proper sign-in flow
            // For simplicity, we'll just return an empty string here
            return@withContext ""
        }
    }
}