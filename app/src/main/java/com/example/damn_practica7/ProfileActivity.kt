package com.example.damn_practica7

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.damn_practica7.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * ProfileActivity permite a los usuarios gestionar su perfil,
 * incluyendo la foto de perfil y la contraseña.
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    private var selectedImageUri: Uri? = null

    // Launcher para seleccionar imagen de la galería
    private val pickImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                // Mostrar la imagen seleccionada en el ImageView
                binding.ivProfilePicture.setImageURI(uri)
            }
        }

    // Launcher para solicitar permisos de lectura de almacenamiento
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido, proceder a seleccionar la imagen
                pickImageLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        // Mostrar el correo del usuario actual
        displayUserProfile()

        // Cargar la foto de perfil existente si la hay
        loadProfilePicture()

        // Configurar listeners de botones
        binding.btnSelectImage.setOnClickListener {
            checkStoragePermissionAndPickImage()
        }

        binding.btnUploadImage.setOnClickListener {
            uploadProfilePicture()
        }

        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }

        binding.btnSignOut.setOnClickListener {
            signOut()
        }
    }

    /**
     * Muestra el correo electrónico del usuario actual en la UI.
     */
    private fun displayUserProfile() {
        val user = auth.currentUser
        user?.email?.let { email ->
            binding.tvUserEmail.text = "Correo: $email"
        } ?: run {
            binding.tvUserEmail.text = "Correo: N/A"
        }
    }

    /**
     * Carga la foto de perfil del usuario desde Firestore y Storage.
     */
    private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return

        // Obtener la URL de la foto de perfil desde Firestore
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImageUrl")
                if (!profileImageUrl.isNullOrEmpty()) {
                    // Cargar la imagen usando Glide
                    Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_default_profile) // Imagen por defecto mientras carga
                        .error(R.drawable.ic_default_profile) // Imagen por defecto si hay error
                        .into(binding.ivProfilePicture)
                } else {
                    // Si no hay URL, mostrar la imagen por defecto
                    binding.ivProfilePicture.setImageResource(R.drawable.ic_default_profile)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileActivity", "Error al cargar URL de perfil: ${e.message}")
                binding.ivProfilePicture.setImageResource(R.drawable.ic_default_profile)
            }
    }

    /**
     * Verifica los permisos de almacenamiento y lanza el selector de imágenes.
     */
    private fun checkStoragePermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    // Permiso ya concedido, seleccionar imagen
                    pickImageLauncher.launch("image/*")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    // Explicar por qué se necesita el permiso
                    showPermissionRationaleDialog()
                }
                else -> {
                    // Solicitar el permiso
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        } else {
            // Permisos no necesarios para API < 23, seleccionar imagen directamente
            pickImageLauncher.launch("image/*")
        }
    }

    /**
     * Muestra un diálogo explicando la necesidad del permiso de almacenamiento.
     */
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso de Almacenamiento")
            .setMessage("Esta aplicación necesita permiso para acceder a tu galería y seleccionar una foto de perfil.")
            .setPositiveButton("Aceptar") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Sube la imagen de perfil seleccionada a Firebase Storage.
     */
    private fun uploadProfilePicture() {
        val user = auth.currentUser
        val imageUri = selectedImageUri

        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
            return
        }
        if (imageUri == null) {
            Toast.makeText(this, "Por favor, selecciona una imagen primero.", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Crear una referencia en Firebase Storage para la imagen del usuario
        val profileImageRef = storageRef.child("profile_pictures/${user.uid}.jpg")

        profileImageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Obtener la URL de descarga de la imagen
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    // Guardar la URL en Firestore en el documento del usuario
                    firestore.collection("users").document(user.uid)
                        .update("profileImageUrl", imageUrl)
                        .addOnSuccessListener {
                            showLoading(false)
                            Toast.makeText(this, "Foto de perfil actualizada.", Toast.LENGTH_SHORT).show()
                            // Cargar la nueva imagen en el ImageView (Glide ya lo hará si la URL es la misma)
                            Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_default_profile)
                                .error(R.drawable.ic_default_profile)
                                .into(binding.ivProfilePicture)
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            Toast.makeText(this, "Error al guardar URL en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }.addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(this, "Error al obtener URL de descarga: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener { taskSnapshot ->
                // Opcional: Mostrar progreso de la subida
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("ProfileActivity", "Subiendo: $progress%")
                // Puedes actualizar una ProgressBar si quieres mostrar el progreso
            }
    }

    /**
     * Cambia la contraseña del usuario.
     * Requiere que el usuario haya iniciado sesión recientemente.
     */
    private fun changePassword() {
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa y confirma la nueva contraseña.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Contraseña actualizada exitosamente.", Toast.LENGTH_SHORT).show()
                    binding.etNewPassword.text.clear()
                    binding.etConfirmPassword.text.clear()
                } else {
                    // Manejar errores como FirebaseAuthRecentLoginRequiredException
                    if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                        Toast.makeText(this, "Por favor, inicia sesión de nuevo para cambiar la contraseña.", Toast.LENGTH_LONG).show()
                        // Opcional: Redirigir al usuario a la pantalla de inicio de sesión para re-autenticarse
                        signOut()
                    } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "La nueva contraseña es inválida. Intenta con otra.", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(this, "Error al cambiar contraseña: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    /**
     * Cierra la sesión del usuario y lo redirige a la AuthActivity.
     */
    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Limpiar el stack de actividades
        startActivity(intent)
        finish()
    }

    /**
     * Muestra u oculta el ProgressBar y habilita/deshabilita los elementos de la UI.
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBarProfile.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSelectImage.isEnabled = !isLoading
        binding.btnUploadImage.isEnabled = !isLoading
        binding.btnChangePassword.isEnabled = !isLoading
        binding.btnSignOut.isEnabled = !isLoading
        binding.etNewPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
    }
}
