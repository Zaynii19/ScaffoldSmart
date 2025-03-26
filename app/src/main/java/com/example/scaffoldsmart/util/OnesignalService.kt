package com.example.scaffoldsmart.util

import android.content.Context
import android.util.Log
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class OnesignalService(val context: Context) {

    private val appId = "4c3f5def-07a5-46b9-9fbb-f8336f1dfa8a"
    private val apiKey = "os_v2_app_jq7v33yhuvdlth537azw6hp2rir3kktmejgui44gno5haipvnp2bjxn3nfjx4yrhzmcwsj5ajhphiqfwfs7zdltixzcfsuot5av5tki"
    private val reqPreferences = context.getSharedPreferences("RENTALREQ", Context.MODE_PRIVATE)

    // Initialize OneSignal
    fun initializeOneSignal() {
        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // OneSignal Initialization
        OneSignal.initWithContext(context, appId)

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }

    // Login as OneSignal user
    fun oneSignalLogin(email: String, role: String) {
        val tags: Map<String, String> = mapOf("role" to role)
        if (email.isNotEmpty()) {
            Log.d("HomeFragDebug", "Admin Email: $email")
            try {
                OneSignal.login(email)
                OneSignal.User.addTags(tags)
            } catch (e: Exception) {
                Log.e("HomeFragDebug", "Failed to log in to OneSignal: ${e.message}")
            }
        } else {
            Log.e("HomeFragDebug", "User email is null or empty")
        }
    }

    fun createOrUpdateOneSignalUserOnLogin(
        externalId: String, // Unique user ID, e.g., email or UUID
        tags: Map<String, String> = emptyMap()
    ) {
        val country = "PK"
        val language = "EN"
        val client = OkHttpClient()

        // Build JSON body
        val jsonBody = JSONObject().apply {
            put("properties", JSONObject().apply {
                put("country", country)
                put("language", language)

                // Add tags if provided
                if (tags.isNotEmpty()) {
                    val tagsJson = JSONObject()
                    tags.forEach { (key, value) ->
                        tagsJson.put(key, value)
                    }
                    put("tags", tagsJson)
                }
            })

            put("identity", JSONObject().apply {
                put("external_id", externalId)
            })
        }

        // Build request
        val request = Request.Builder()
            .url("https://api.onesignal.com/apps/$appId/users")
            .header("Authorization", "Basic $apiKey")
            .header("Content-Type", "application/json; charset=utf-8")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        // Execute request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OneSignalDebug", "Failed to create or update user: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        Log.d("OneSignalDebug", "User created or updated successfully: ${response.body?.string()}")
                    } else {
                        Log.e("OneSignalDebug", "Error: ${response.code} - ${response.body?.string()}")
                    }
                }
            }
        })
    }

    fun sendNotiByOneSignalToExternalId(
        title: String,
        message: String,
        externalId: List<String> // This can accept multiple IDs or a single in a list
    ) {
        // Create the notification JSON object for include_aliases
        val notification = JSONObject().apply {
            put("app_id", appId)
            put("target_channel", "push")
            put("headings", JSONObject().put("en", title))
            put("contents", JSONObject().put("en", message))
            put("include_aliases", JSONObject().put("external_id", JSONArray(externalId)))
            put("small_icon", "app_logo")
            put("large_icon", "app_logo")
            put("android_vibrate", true)
            put("android_lights", true)
            put("priority", 10) // Set priority to high (10)
        }

        val url = "https://api.onesignal.com/notifications"
        val requestBody = notification.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Basic $apiKey")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HomeFragDebug", "Error sending notification: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val notificationId = jsonResponse.getString("id")
                            Log.d("HomeFragDebug", "Notification sent successfully. ID: $notificationId")
                        } catch (e: JSONException) {
                            Log.e("HomeFragDebug", "Error parsing response: $e")
                        }
                    }
                } else {
                    val responseBody = response.body?.string() ?: "No response body"
                    Log.e("HomeFragDebug", "Notification failed: ${response.code}, Response: $responseBody")
                }
            }
        })
    }

    fun sendReqNotiByOneSignalToSegment(
        clientID: String,
        clientName: String,
        clientAddress: String,
        clientEmail: String,
        clientPhone: String,
        clientCnic: String,
        rentalAddress: String,
        startDuration: String,
        endDuration: String,
        pipes: String,
        pipesLength: String,
        joints: String,
        wench: String,
        pumps: String,
        motors: String,
        generators: String,
        wheel: String,
        rent: Int) {
        val message = "A new rental request has been submitted. Click to view rental details."
        val title = "Rental Request Alert"

        // Create custom data JSON
        val customData = JSONObject().apply {
            put("clientID", clientID)
            put("clientName", clientName)
            put("clientAddress", clientAddress)
            put("clientEmail", clientEmail)
            put("clientPhone", clientPhone)
            put("clientCnic", clientCnic)
            put("rentalAddress", rentalAddress)
            put("startDuration", startDuration)
            put("endDuration", endDuration)
            put("pipes", pipes)
            put("pipesLength", pipesLength)
            put("joints", joints)
            put("wench", wench)
            put("pumps", pumps)
            put("motors", motors)
            put("generators", generators)
            put("wheel", wheel)
            put("rent", rent)
        }

        // Create the notification JSON object for included_segments
        val notification = JSONObject().apply {
            put("app_id", appId)
            put("target_channel", "push")
            put("included_segments", JSONArray().put("Rental Request Subscribers"))
            put("contents", JSONObject().put("en", message))
            put("headings", JSONObject().put("en", title))
            put("small_icon", "app_logo")
            put("large_icon", "app_logo")
            put("android_vibrate", true)
            put("android_lights", true)
            put("priority", 10) // Set priority to high (10)
            put("data", customData) // Attach custom data
        }

        val url = "https://api.onesignal.com/notifications"
        val requestBody = notification.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Basic $apiKey")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OneSignalDebug", "Error sending notification: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val notificationId = jsonResponse.getString("id")
                            Log.d("OneSignalDebug", "Notification sent successfully. ID: $notificationId")
                        } catch (e: JSONException) {
                            Log.e("OneSignalDebug", "Error parsing response: $e")
                        }
                    }
                } else {
                    val responseBody = response.body?.string() ?: "No response body"
                    Log.e("InventoryReqDebug", "Notification failed: ${response.code}, Response: $responseBody")
                }
            }
        })
    }

    fun getOneSignalNoti(prevNotiCompletedAt: String, prevNotificationId: String) {
        // Construct the URL for fetching the notification details
        val notiId = ""
        val url = "https://api.onesignal.com/notifications/$notiId?app_id=$appId"

        // Build the request
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Basic $apiKey") // Use the API key for authorization
            .get()
            .build()

        // Execute the request
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                Log.e("OneSignalDebug", "Failed to fetch notification details: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            Log.d("OneSignalDebug", "Notification details: $jsonResponse")

                            val notifications = jsonResponse.optJSONArray("notifications")
                            notifications?.let {
                                if (it.length() > 0) {
                                    val currentNotification = it.getJSONObject(0)
                                    Log.d("OneSignalDebug", "Current Notification: $currentNotification")

                                    // Safely extract 'data' from the notification
                                    val reqData = currentNotification.optJSONObject("data")
                                    if (reqData != null) {

                                        val currentNotiCompletedAt = currentNotification.optString("completed_at")
                                        val currentNotificationId = currentNotification.optString("id")
                                        reqPreferences.edit().putString("CompletedAt", currentNotiCompletedAt).apply()
                                        reqPreferences.edit().putString("NotificationId", currentNotificationId).apply()

                                        if (currentNotiCompletedAt != prevNotiCompletedAt && currentNotificationId != prevNotificationId) {
                                            AdminMainActivity.handleReqData(reqData)
                                        } else {
                                            Log.d("OneSignalDebug", "Request data already found!")
                                        }

                                    } else {
                                        Log.e("OneSignalDebug", "No data found in the notification")
                                    }
                                } else {
                                    Log.d("OneSignalDebug", "No notifications found.")
                                }
                            }
                        } catch (e: JSONException) {
                            Log.e("OneSignalDebug", "Error parsing JSON response: $e")
                        }
                    }
                } else {
                    // Handle error response
                    val errorBody = response.body?.string() ?: "No error body"
                    Log.e("OneSignalDebug", "Error fetching notification details: ${response.code}, Response: $errorBody")
                }
            }
        })
    }
}