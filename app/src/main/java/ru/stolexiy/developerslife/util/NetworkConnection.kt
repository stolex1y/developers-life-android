package ru.stolexiy.developerslife.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.core.content.ContextCompat.getSystemService

class NetworkConnection {
    companion object {
        fun isOnline(context: Context): Boolean {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
            return networkInfo?.isConnected == true
        }
    }
}