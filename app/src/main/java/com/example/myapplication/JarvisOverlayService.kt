package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.myapplication.data.Champion
import com.example.myapplication.data.ChampionRepository
import com.example.myapplication.data.MetaRepository
import com.example.myapplication.ui.screens.getCountersFor

class JarvisOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var rootContainer: FrameLayout
    private lateinit var compactButton: Button
    private lateinit var hudCard: LinearLayout
    private lateinit var hudTitle: TextView
    private lateinit var hudDescription: TextView
    private lateinit var hudCounters: TextView
    private lateinit var hudCountersRow: LinearLayout

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("RESULT_CODE", 0) ?: 0
        val resultData = intent?.getParcelableExtra<Intent>("RESULT_DATA")

        if (resultCode != 0 && resultData != null) {
            startNotificationForeground()

            // Initialize MediaProjection
            mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)
            
            mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    virtualDisplay?.release()
                    imageReader?.close()
                }
            }, null)

            setupVirtualDisplay()
        }
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi

        createFloatingWidget()
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun createFloatingWidget() {
        rootContainer = FrameLayout(this)

        // 1. Compact Mode - Small Round Button
        compactButton = Button(this).apply {
            text = "JARVIS"
            setBackgroundColor(Color.parseColor("#00BFFF"))
            setTextColor(Color.BLACK)
            textSize = 10f
            clipToOutline = true
        }

        // 2. Expanded Mode - HUD Card Layout
        hudCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#161B22"))
            setPadding(24, 24, 24, 24)
            visibility = View.GONE
        }

        hudTitle = TextView(this).apply {
            text = "JARVIS HUD CANLI ANALİZ"
            setTextColor(Color.parseColor("#00BFFF"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }

        hudDescription = TextView(this).apply {
            text = "Savaş ekranını okumak için 'TARA' tuşuna dokunun."
            setTextColor(Color.WHITE)
            textSize = 11f
            setPadding(0, 0, 0, 8)
        }

        hudCounters = TextView(this).apply {
            text = "Counters: --"
            setTextColor(Color.parseColor("#FFFFD700")) // Gold
            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        }

        hudCountersRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 12)
        }

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val scanButton = Button(this).apply {
            text = "TARA"
            setBackgroundColor(Color.parseColor("#00BFFF"))
            setTextColor(Color.BLACK)
            textSize = 10f
            setOnClickListener {
                Toast.makeText(this@JarvisOverlayService, "Ekran taranıyor...", Toast.LENGTH_SHORT).show()
                captureAndAnalyzeScreen()
            }
        }

        val closeButton = Button(this).apply {
            text = "KAPAT"
            setBackgroundColor(Color.parseColor("#30363D"))
            setTextColor(Color.WHITE)
            textSize = 10f
            setOnClickListener {
                hudCard.visibility = View.GONE
                compactButton.visibility = View.VISIBLE
            }
        }

        actionRow.addView(scanButton)
        // Add small spacer
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(16, 1)
        }
        actionRow.addView(spacer)
        actionRow.addView(closeButton)

        hudCard.addView(hudTitle)
        hudCard.addView(hudDescription)
        hudCard.addView(hudCounters)
        hudCard.addView(hudCountersRow)
        hudCard.addView(actionRow)

        // Add views to container
        rootContainer.addView(compactButton, FrameLayout.LayoutParams(160, 160))
        rootContainer.addView(hudCard, FrameLayout.LayoutParams(600, FrameLayout.LayoutParams.WRAP_CONTENT))

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 200

        windowManager.addView(rootContainer, params)

        // Dragging logic for root container
        compactButton.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(rootContainer, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val diffX = (event.rawX - initialTouchX).toInt()
                        val diffY = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(diffX) < 15 && Math.abs(diffY) < 15) {
                            // Clicked: open Expanded HUD Card
                            compactButton.visibility = View.GONE
                            hudCard.visibility = View.VISIBLE
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    @SuppressLint("WrongConstant")
    private fun setupVirtualDisplay() {
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "JarvisScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun captureAndAnalyzeScreen() {
        val image: Image? = imageReader?.acquireLatestImage()
        if (image == null) {
            Toast.makeText(this, "Görüntü alınamadı, tekrar deneyin.", Toast.LENGTH_SHORT).show()
            return
        }

        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * screenWidth

        val bitmap = android.graphics.Bitmap.createBitmap(
            screenWidth + rowPadding / pixelStride,
            screenHeight,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        val croppedBitmap = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)

        // Process with ML Kit
        val inputImage = InputImage.fromBitmap(croppedBitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                analyzeText(visionText.text)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Okuma hatası: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun analyzeText(text: String) {
        val lowerText = text.lowercase()
        Log.d("JARVIS", "Canlı HUD Taraması: $text")

        // 1. Şampiyon Eşleştirme (Champion Recognition)
        var detectedChampion: Champion? = null
        for (champ in ChampionRepository.champions) {
            val nameLower = champ.name.lowercase()
            if (lowerText.contains(nameLower) && (detectedChampion == null || nameLower.length > detectedChampion.name.length)) {
                detectedChampion = champ
            }
        }

        // 2. Karo Eşleştirme (Node Recognition)
        var detectedNode: com.example.myapplication.data.MetaNode? = null
        for (season in MetaRepository.seasons) {
            for (node in season.nodes) {
                val nodeNameLower = node.name.lowercase()
                if (lowerText.contains(nodeNameLower) && (detectedNode == null || nodeNameLower.length > detectedNode.name.length)) {
                    detectedNode = node
                }
            }
        }

        // 3. HUD Arayüzünü Güncelleme
        if (detectedChampion != null && detectedNode != null) {
            hudTitle.text = "🎯 RAKİP: ${detectedChampion.name} | KARO: ${detectedNode.name}"
            hudDescription.text = "Karo Etkisi: ${detectedNode.effect}"
            
            // Hem şampiyon counter'larını hem de karo için önerilenleri birleştir
            val countersList = getCountersFor(detectedChampion)
            val champCounters = countersList.joinToString(", ") { it.first.name }
            val nodeAttackersNames = detectedNode.bestAttackers.mapNotNull { id -> 
                ChampionRepository.champions.find { it.id == id }?.name 
            }.joinToString(", ")
            
            hudCounters.text = "Önerilen Saldırganlar:\n• Şampiyona Karşı: $champCounters\n• Karoya Karşı: $nodeAttackersNames"
            
            val nodeAttackersList = detectedNode.bestAttackers.mapNotNull { id -> 
                ChampionRepository.champions.find { it.id == id } 
            }
            updateCountersRow(countersList.map { it.first } + nodeAttackersList)
        } else if (detectedChampion != null) {
            hudTitle.text = "🎯 RAKİP: ${detectedChampion.name}"
            hudDescription.text = "Sınıf: ${detectedChampion.mcocClass.displayName} | Tier: ${detectedChampion.tier}"
            val countersList = getCountersFor(detectedChampion)
            val champCounters = countersList.joinToString(", ") { it.first.name }
            hudCounters.text = "En İyi Counterlar: $champCounters"
            updateCountersRow(countersList.map { it.first })
        } else if (detectedNode != null) {
            hudTitle.text = "🛡️ KARO: ${detectedNode.name}"
            hudDescription.text = "Karo Etkisi: ${detectedNode.effect}"
            val nodeAttackers = detectedNode.bestAttackers.mapNotNull { id -> 
                ChampionRepository.champions.find { it.id == id }?.name 
            }.joinToString(", ")
            hudCounters.text = "Önerilen Saldırganlar: $nodeAttackers"
            
            val nodeAttackersList = detectedNode.bestAttackers.mapNotNull { id -> 
                ChampionRepository.champions.find { it.id == id } 
            }
            updateCountersRow(nodeAttackersList)
        } else {
            // Hiçbiri bulunamazsa yedek kelime eşleşmesi
            var nodeName = "Bilinmeyen Karo"
            var desc = "Özel bir tehlike tespit edilmedi. Agresif oynayabilirsiniz."
            var counters = "Doom, Hercules, Hulkling"
            
            if (lowerText.contains("bleed") || lowerText.contains("kanama")) {
                nodeName = "Kanama Karosu (Bleed)"
                desc = "Zamanla can götüren Kanama zayıflatıcısı uygular."
                counters = "Nimrod, Warlock, Colossus (Kanama Bağışıklığı)"
            } else if (lowerText.contains("biohazard") || lowerText.contains("biyotehlike")) {
                nodeName = "Biyotehlike (Biohazard)"
                desc = "Blok yapıldığında Zehir, vurulduğunda Kanama tetikler."
                counters = "Nimrod, Warlock, Nebula (Çift Bağışıklık)"
            } else if (lowerText.contains("durdurulamaz") || lowerText.contains("unstoppable")) {
                nodeName = "Durdurulamaz (Unstoppable)"
                desc = "Rakip durdurulamaz mod açar, düz vuruşları kesilmez."
                counters = "Titania, She-Hulk, Spider-Man 2099"
            } else if (lowerText.contains("şok") || lowerText.contains("shock")) {
                nodeName = "Şok Karosu (Shock)"
                desc = "Enerji hasarı veren Şok zayıflatıcısı uygular."
                counters = "Doktor Doom, Hulkling, Absorbing Man"
            }
            
            hudTitle.text = "JARVIS: $nodeName"
            hudDescription.text = "Açıklama: $desc"
            hudCounters.text = "Tavsiye Counterlar: $counters"
            
            // Populate fallback portraits
            val fallbackChamps = listOf("doctordoom", "hercules", "hulkling").mapNotNull { id ->
                ChampionRepository.champions.find { it.id == id }
            }
            updateCountersRow(fallbackChamps)
        }
    }

    private fun updateCountersRow(champions: List<com.example.myapplication.data.Champion>) {
        hudCountersRow.removeAllViews()
        for (champ in champions.distinctBy { it.id }.take(4)) {
            // Container for portrait and name label
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(0, 0, 24, 0)
            }

            // Portrait layout
            val drawableName = champ.id.lowercase().replace("-", "_").let { if (it[0].isDigit() || it == "void") "img_$it" else it }
            val imageResId = resources.getIdentifier(drawableName, "drawable", packageName)

            val portrait = android.widget.ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(96, 96)
                if (imageResId != 0) {
                    setImageResource(imageResId)
                } else {
                    setBackgroundColor(Color.GRAY)
                }
                
                // MCOC class border glow effect
                val classColor = champ.mcocClass.color.toInt()
                setPadding(6, 6, 6, 6)
                setBackgroundColor(classColor)
            }

            // Text label
            val name = TextView(this).apply {
                text = champ.name
                setTextColor(Color.WHITE)
                textSize = 9f
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(96, LinearLayout.LayoutParams.WRAP_CONTENT)
                setPadding(0, 4, 0, 0)
            }

            container.addView(portrait)
            container.addView(name)
            hudCountersRow.addView(container)
        }
    }

    private fun startNotificationForeground() {
        val channelId = "jarvis_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "JARVIS Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("JARVIS Aktif")
            .setContentText("MCOC Asistanı ekranın üzerinde çalışıyor.")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualDisplay?.release()
        mediaProjection?.stop()
        imageReader?.close()
        if (::rootContainer.isInitialized) {
            windowManager.removeView(rootContainer)
        }
    }
}
