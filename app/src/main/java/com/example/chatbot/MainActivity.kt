
package com.example.chatbot

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient() // Initialize OkHttpClient for making network requests

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display
        setContentView(R.layout.activity_main) // Set the layout for the activity
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()) // Get system bar insets
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom) // Apply padding to the view
            insets
        }
        val etQuestion = findViewById<EditText>(R.id.etQuestion) // EditText for user input
        val btnSubmit = findViewById<Button>(R.id.btnSubmit) // Submit button for sending user query
        val txtResponse = findViewById<TextView>(R.id.txtResponse) // TextView to display response

        // Set up OnClickListener for the submit button
        btnSubmit.setOnClickListener {
            val question = etQuestion.text.toString() // Get the user's query from EditText
            // Display toast for user query
            Toast.makeText(this, question.toString(), Toast.LENGTH_SHORT).show()
            // Call function to get response from OpenAI API
            getResponse(question) { response ->
                runOnUiThread {
                    // Display response in TextView
                    txtResponse.text = response
                }
            }
        }
    }

    /**
     * Function/Module: getResponse
     * Description: Makes a request to OpenAI's API to generate a response based on user input.
     * Parameters:
     * - question: The user's query.
     * - callback: Callback function to handle the response from the API.
     * Returns: None.
     */
    fun getResponse(question: String, callback: (String) -> Unit) {
        val apiKey = "sk-proj-B4T93fnOF2IWM4ho96NlT3BlbkFJT8Y9AlJAp6IpOyfYYTE4" // API Key for authentication
        val url = "https://api.openai.com/v1/completions" // API endpoint for generating completions

        // Request body in JSON format
        val requestBody = """
            {
            "model": "gpt-3.5-turbo-instruct",
            "prompt": "$question",
            "max_tokens": 10,
            "temperature": 0
            }
        """.trimIndent()

        // Build the HTTP request
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        // Execute the asynchronous HTTP request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed", e) // Log error if API request fails
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() // Get the response body
                if (body != null) {
                    Log.d("Response", body) // Print the response body to log

                    val jsonObject = JSONObject(body) // Parse JSON response
                    if (jsonObject.has("error")) {
                        val errorMessage = jsonObject.getJSONObject("error").getString("message")
                        Log.e("API Error", errorMessage)
                        // Handle the error message here
                    } else if (jsonObject.has("choices")) {
                        val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                        val textResult = jsonArray.getJSONObject(0).getString("text")
                        callback(textResult) // Pass the response text to the callback function
                    } else {
                        Log.e("API Error", "Unexpected response format")
                        // Handle unexpected response format here
                    }
                } else {
                    Log.d("Response", "Empty body")
                }
            }
        })
    }
}
