package dz.esi.athena

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.esi.athena.R
import com.google.firebase.FirebaseApp


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        val signUpButton = findViewById<Button>(R.id.btn_signup)
        val loginButton = findViewById<Button>(R.id.btn_login)


        signUpButton.setOnClickListener{
            startActivity(Intent(this, Signupactivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, Loginactivity::class.java))
            finish()
        }
    }
}