package dz.esi.athena

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.esi.athena.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signupactivity.*

class Signupactivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signupactivity)

        auth = Firebase.auth

        val confirmButton = findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            signUpUser()
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun signUpUser() {
        if (email.text.toString().isEmpty()) {
            email.error = "Please enter email"
            email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            email.error = "Please enter valid email"
            email.requestFocus()
            return
        }

        if (password.text.toString().isEmpty()) {
            password.error = "Please enter password"
            password.requestFocus()
            return
        }

        if (licenseExpiry.text.toString().isEmpty() || licenseExpiry.text.toString().split("/")[0] > 31.toString()
            || licenseExpiry.text.toString().split("/")[1] > 12.toString()
            || licenseExpiry.text.toString().split("/")[2] > 9999.toString())
        {
            return
        }

        auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val store = FirebaseFirestore.getInstance()
                        store.collection("drivers").add(hashMapOf<String, Any>(
                            "email" to email.text.toString(),
                            "firstname" to editTextTextPersonName.text.toString(),
                            "lastname" to editTextTextPersonName2.text.toString(),
                            "licenseExpiry" to licenseExpiry.text.toString(),
                            "licenseID" to licenseID.text.toString()
                        ))
                            .addOnSuccessListener {
                                startActivity(Intent(this, Loginactivity::class.java))
                                finish()
                            }
                            .addOnFailureListener{ e ->
                                Log.w("Firestore", "Error adding user", e)
                                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show()
                            }

                    } else {
                        Log.w("ERROR", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, task.exception?.message,
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
}
