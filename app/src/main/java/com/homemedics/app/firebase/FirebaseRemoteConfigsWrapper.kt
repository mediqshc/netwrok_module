package com.homemedics.app.firebase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.homemedics.app.BuildConfig
import com.homemedics.app.R

class FirebaseRemoteConfigsWrapper {

    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    fun initialize(): LiveData<FirebaseRemoteConfigResult> {
        val data =
            MutableLiveData<FirebaseRemoteConfigResult>()
        if (BuildConfig.DEBUG) {
            setDebugConfigSettings(data)
        } else {
            setDefaultConfigs(data)
        }
        return data
    }

    fun refreshConfigs(): LiveData<FirebaseRemoteConfigResult> {
        return fetch(true)
    }

    fun fetchConfigs(): LiveData<FirebaseRemoteConfigResult> {
        return fetch(false)
    }

    private fun fetch(bypassCache: Boolean): LiveData<FirebaseRemoteConfigResult> {
        var cacheExpiration = CACHE_EXPIRATION
        val data =
            MutableLiveData<FirebaseRemoteConfigResult>()
        if (firebaseRemoteConfig.info.configSettings.minimumFetchIntervalInSeconds == 0L || bypassCache) {
            cacheExpiration = 0
        }
        val fetch =
            firebaseRemoteConfig.fetchAndActivate()

        fetch.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseRemoteConfigResult = firebaseSuccessResult()
                firebaseRemoteConfigResult.isSuccessful =false
                data.postValue(firebaseRemoteConfigResult)
            } else {
                val exception = getException(
                    task,
                    "Fetch was not successful but there was no exception either"
                )
                val firebaseRemoteConfigResult = firebaseFailureResult(
                    exception
                )
                data.postValue(firebaseRemoteConfigResult)
            }
        }

        return data
    }

    private fun getException(
        task: Task<*>,
        fallbackMessage: String
    ): Exception {
        var exception = task.exception
        if (exception == null) {
            exception = IllegalStateException(fallbackMessage)
        }
        return exception
    }

    private fun setDebugConfigSettings(data: MutableLiveData<FirebaseRemoteConfigResult>) {
        val configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0L)
                .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    setDefaultConfigs(data)
                } else {
                    val exception = getException(
                        task,
                        "Setting config was not successful but there was no exception either"
                    )
                    val firebaseRemoteConfigResult =
                        firebaseFailureResult(
                            exception
                        )

                    data.postValue(firebaseRemoteConfigResult)
                }
            }
    }

    private fun setDefaultConfigs(data: MutableLiveData<FirebaseRemoteConfigResult>) {
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            .addOnCompleteListener { task: Task<Void?> ->
                val firebaseRemoteConfigResult: FirebaseRemoteConfigResult
                if (task.isSuccessful) {
                    firebaseRemoteConfigResult = firebaseSuccessResult()

                } else {
                    val exception = getException(
                        task, "Setting defaults was not " +
                                "successful but there was no exception either"
                    )
                    firebaseRemoteConfigResult = firebaseFailureResult(exception)

                }
                data.postValue(firebaseRemoteConfigResult)
            }
    }

    private fun firebaseSuccessResult(): FirebaseRemoteConfigResult {
        val firebaseRemoteConfigResult = FirebaseRemoteConfigResult()
        firebaseRemoteConfigResult.isSuccessful =true
        return firebaseRemoteConfigResult
    }

    private fun firebaseFailureResult(exception: Exception): FirebaseRemoteConfigResult {
        val firebaseRemoteConfigResult = FirebaseRemoteConfigResult()
        firebaseRemoteConfigResult.isSuccessful =false
        firebaseRemoteConfigResult.exception= exception
        return firebaseRemoteConfigResult
    }


    fun getConfigString(key: String): String {
        return firebaseRemoteConfig.getString(key)
    }

    fun get(key: String): String {
        return firebaseRemoteConfig.getString(key)
    }

    fun getConfigInteger(key: String): Int {
        return firebaseRemoteConfig.getLong(key).toInt()
    }

    fun getConfigLong(key: String): Long {
        return firebaseRemoteConfig.getLong(key)
    }

    fun getConfigBoolean(key: String): Boolean {
        return firebaseRemoteConfig.getBoolean(key)
    }

    companion object {
        private val TAG = FirebaseRemoteConfigsWrapper::class.java.simpleName

        // Default cache expiration is 1 hour
        private const val CACHE_EXPIRATION: Long = 3600
    }


}