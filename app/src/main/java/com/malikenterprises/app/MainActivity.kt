package com.malikenterprises.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val client = SupabaseClient()
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun showLogin() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Malik Enterprises\nملک انٹرپرائزز"
            textSize = 28f
        }
        val email = EditText(this).apply { hint = "Email" }
        val password = EditText(this).apply {
            hint = "Password"
            inputType = 0x81
        }
        val login = Button(this).apply { text = "Login / لاگ اِن" }
        val status = TextView(this).apply { text = "Supabase connected app" }

        layout.addView(title)
        layout.addView(email)
        layout.addView(password)
        layout.addView(login)
        layout.addView(status)
        setContentView(layout)

        login.setOnClickListener {
            status.text = "Connecting..."
            executor.execute {
                val result = client.signIn(email.text.toString().trim(), password.text.toString())
                runOnUiThread {
                    if (result.code in 200..299) showDashboard()
                    else status.text = "Login failed (${result.code})\n${result.body}"
                }
            }
        }
    }

    private fun showDashboard() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Malik Enterprises\nملک انٹرپرائزز"
            textSize = 28f
        }
        val info = TextView(this).apply {
            text = "Connected to Supabase\n\nLoading products..."
            textSize = 18f
        }
        val products = TextView(this)

        layout.addView(title)
        layout.addView(info)
        layout.addView(products)
        setContentView(layout)

        executor.execute {
            val result = client.getProducts()
            runOnUiThread {
                if (result.code in 200..299) {
                    info.text = "Supabase connected successfully.\nProducts:"
                    products.text = result.body
                } else {
                    info.text = "Connected, but products could not be loaded."
                    products.text = "HTTP ${result.code}\n${result.body}"
                }
            }
        }
    }

    override fun onDestroy() {
        executor.shutdown()
        super.onDestroy()
    }
}
