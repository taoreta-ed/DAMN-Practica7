package com.example.damn_practica7

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.damn_practica7.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * AuthActivity es la actividad principal para el registro y el inicio de sesión de usuarios.
 * Utiliza Firebase Authentication para gestionar las cuentas de usuario.
 */
class AuthActivity : AppCompatActivity() {

    // View Binding para acceder a los elementos de la UI de forma segura
    private lateinit var binding: ActivityAuthBinding

    // Instancia de FirebaseAuth para interactuar con el servicio de autenticación
    private lateinit var auth: FirebaseAuth

    /**
     * Se llama cuando la actividad es creada por primera vez.
     * Aquí se inicializa la vista y los listeners de los botones.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar el layout usando View Binding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la instancia de FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Configurar el listener para el botón de registro
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        // Configurar el listener para el botón de inicio de sesión
        binding.btnLogin.setOnClickListener {
            loginUser()
        }
    }

    /**
     * Se llama cuando la actividad se inicia.
     * Comprueba si un usuario ya ha iniciado sesión. Si es así, navega a HomeActivity.
     */
    override fun onStart() {
        super.onStart()
        // Verificar si el usuario ya está autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si el usuario ya inició sesión, navegar a la actividad principal (HomeActivity)
            navigateToHome()
        }
    }

    /**
     * Intenta registrar un nuevo usuario con el correo electrónico y la contraseña proporcionados.
     * Muestra mensajes de éxito o error al usuario.
     */
    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validar que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa correo y contraseña.", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar ProgressBar y deshabilitar botones
        showLoading(true)

        // Crear usuario con correo y contraseña en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false) // Ocultar ProgressBar

                if (task.isSuccessful) {
                    // Registro exitoso
                    Log.d("AuthActivity", "createUserWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(this, "Registro exitoso para ${user?.email}.", Toast.LENGTH_SHORT).show()
                    navigateToHome() // Navegar a la actividad principal
                } else {
                    // Si el registro falla, mostrar un mensaje al usuario.
                    Log.w("AuthActivity", "createUserWithEmail:failure", task.exception)
                    handleAuthException(task.exception)
                }
            }
    }

    /**
     * Intenta iniciar sesión con el correo electrónico y la contraseña proporcionados.
     * Muestra mensajes de éxito o error al usuario.
     */
    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validar que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa correo y contraseña.", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar ProgressBar y deshabilitar botones
        showLoading(true)

        // Iniciar sesión con correo y contraseña en Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false) // Ocultar ProgressBar

                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    Log.d("AuthActivity", "signInWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(this, "Bienvenido, ${user?.email}.", Toast.LENGTH_SHORT).show()
                    navigateToHome() // Navegar a la actividad principal
                } else {
                    // Si el inicio de sesión falla, mostrar un mensaje al usuario.
                    Log.w("AuthActivity", "signInWithEmail:failure", task.exception)
                    handleAuthException(task.exception)
                }
            }
    }

    /**
     * Maneja las excepciones comunes de Firebase Authentication y muestra un Toast apropiado.
     */
    private fun handleAuthException(exception: Exception?) {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Toast.makeText(this, "La contraseña es muy débil.", Toast.LENGTH_LONG).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Credenciales inválidas. Verifica tu correo o contraseña.", Toast.LENGTH_LONG).show()
            }
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(this, "Ya existe una cuenta con este correo.", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Error de autenticación: ${exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Muestra u oculta el ProgressBar y habilita/deshabilita los botones de autenticación.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.btnLogin.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    /**
     * Navega a la HomeActivity y finaliza la actividad actual para evitar que el usuario regrese.
     */
    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Finaliza AuthActivity para que el usuario no pueda volver con el botón "atrás"
    }
}
