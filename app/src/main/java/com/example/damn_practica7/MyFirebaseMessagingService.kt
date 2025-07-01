package com.example.damn_practica7

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * MyFirebaseMessagingService extiende FirebaseMessagingService para manejar
 * la recepción de mensajes de FCM y la generación de notificaciones.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Constante para el ID del canal de notificación
    companion object {
        private const val CHANNEL_ID = "DAMN_Practica7_Channel"
        private const val TAG = "MyFirebaseMsgService"
    }

    /**
     * Se llama cuando se recibe un nuevo token de registro de FCM.
     * Este token es único para cada instancia de la aplicación y puede cambiar.
     * Deberías enviar este token a tu servidor si planeas enviar notificaciones dirigidas.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Aquí podrías enviar el token a tu servidor de aplicaciones
        // sendRegistrationToServer(token)
    }

    /**
     * Se llama cuando se recibe un mensaje de FCM.
     * Este método se ejecuta cuando la aplicación está en primer plano.
     * Si la aplicación está en segundo plano, la notificación se mostrará automáticamente
     * y onMessageReceived solo se llamará si el mensaje contiene solo datos (no notificación).
     *
     * @param remoteMessage Objeto RemoteMessage que contiene los datos del mensaje.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Comprobar si el mensaje contiene una carga de datos.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // Procesar los datos si es necesario (ej. actualizar UI, guardar en DB)
            // Por ejemplo, aquí podrías manejar una notificación de "nueva publicación"
            val communityName = remoteMessage.data["communityName"] ?: "una comunidad"
            val postTitle = remoteMessage.data["postTitle"] ?: "Nueva Publicación"
            val postDescription = remoteMessage.data["postDescription"] ?: "Se ha publicado algo nuevo."
            val communityId = remoteMessage.data["communityId"] ?: ""

            // Si el mensaje contiene datos, puedes construir una notificación local
            sendNotification(postTitle, postDescription, communityId, communityName)
        }

        // Comprobar si el mensaje contiene una carga de notificación.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // Si el mensaje de FCM ya tiene una parte de notificación, la manejamos aquí
            // Esto es útil si quieres personalizar cómo se muestra la notificación
            // cuando la app está en primer plano. Si solo quieres que se muestre
            // la notificación por defecto de FCM, no necesitas esta parte.
            // Para este caso, ya estamos construyendo la notificación desde los datos,
            // pero si tuvieras un mensaje de notificación puro, lo harías así:
            // sendNotification(it.title, it.body, "", "") // Sin communityId ni name si no vienen en data
        }
    }

    /**
     * Crea y muestra una notificación push.
     * @param title Título de la notificación.
     * @param messageBody Cuerpo del mensaje de la notificación.
     * @param communityId ID de la comunidad (para abrir la actividad correcta al hacer clic).
     * @param communityName Nombre de la comunidad (para mostrar en la actividad).
     */
    private fun sendNotification(title: String?, messageBody: String?, communityId: String, communityName: String) {
        // Crear un Intent que se activará cuando el usuario toque la notificación
        val intent = Intent(this, CommunityPostsActivity::class.java).apply {
            // Pasar los datos necesarios a la actividad que se abrirá
            putExtra("communityId", communityId)
            putExtra("communityName", communityName)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Limpiar el stack de actividades
        }

        // Crear un PendingIntent para el Intent
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE es necesario para API 23+
        )

        // Obtener el NotificationManager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear un canal de notificación para Android 8.0 (Oreo) y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones de Comunidades", // Nombre visible para el usuario
                NotificationManager.IMPORTANCE_DEFAULT // Nivel de importancia
            ).apply {
                description = "Canal para notificaciones de nuevas publicaciones en comunidades."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Construir la notificación
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Icono pequeño de la notificación
            .setContentTitle(title) // Título de la notificación
            .setContentText(messageBody) // Cuerpo del mensaje
            .setAutoCancel(true) // La notificación se cancela automáticamente al tocarla
            .setSound(defaultSoundUri) // Sonido de notificación por defecto
            .setContentIntent(pendingIntent) // Intent que se activa al tocar la notificación
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridad para versiones anteriores a Oreo

        // Mostrar la notificación
        notificationManager.notify(0 /* ID de notificación */, notificationBuilder.build())
    }
}
