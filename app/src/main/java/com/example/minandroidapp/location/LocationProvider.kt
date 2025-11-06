package com.example.minandroidapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.example.minandroidapp.model.EntryLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationProvider(context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): EntryLocation? {
        val location = try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
        } catch (error: Exception) {
            null
        }

        return location?.let {
            val label = resolveLabel(it.latitude, it.longitude)
            EntryLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                label = label ?: formatCoordinates(it.latitude, it.longitude),
            )
        }
    }

    private suspend fun resolveLabel(latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) {
                    return@withContext null
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                val address = addresses.firstOrNull()
                                val locality = listOfNotNull(address?.featureName, address?.locality)
                                    .distinct()
                                    .joinToString(separator = ", ")
                                continuation.resume(locality.ifEmpty { null })
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(null)
                            }
                        })
                        continuation.invokeOnCancellation { }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()
                    val locality = listOfNotNull(address?.featureName, address?.locality)
                        .distinct()
                        .joinToString(separator = ", ")
                    locality.ifEmpty { null }
                }
            } catch (io: IOException) {
                null
            }
        }
    }

    private fun formatCoordinates(latitude: Double, longitude: Double): String {
        return String.format(Locale.getDefault(), "%.4f, %.4f", latitude, longitude)
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T? {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                val exception = task.exception ?: RuntimeException("Location lookup failed")
                continuation.resumeWithException(exception)
            }
        }
        continuation.invokeOnCancellation { }
    }
}
