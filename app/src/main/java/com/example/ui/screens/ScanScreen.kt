package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.MainViewModel
import com.example.util.BarcodeAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)
    val isFlashOn by viewModel.isFlashOn.collectAsState()
    val isFrontCamera by viewModel.isFrontCamera.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isScannedAlready by remember { mutableStateOf(false) }

    // Image Picker for decoding QR from gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val scanner = BarcodeScanning.getClient()
                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty() && !isScannedAlready) {
                                val first = barcodes[0]
                                val raw = first.rawValue ?: first.displayValue ?: ""
                                if (raw.isNotEmpty()) {
                                    isScannedAlready = true
                                    viewModel.onScanResult(raw, "QR_CODE", onNavigateToDetail)
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(isFlashOn, cameraInstance) {
        try {
            cameraInstance?.cameraControl?.enableTorch(isFlashOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(zoomLevel, cameraInstance) {
        try {
            cameraInstance?.cameraControl?.setLinearZoom((zoomLevel - 1f) / 4f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("scan_screen")
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Full screen Camera Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val executor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(executor, BarcodeAnalyzer { raw, format ->
                                        if (!isScannedAlready) {
                                            isScannedAlready = true
                                            viewModel.onScanResult(raw, format, onNavigateToDetail)
                                        }
                                    })
                                }

                            val cameraSelector = if (isFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            cameraProvider.unbindAll()
                            cameraInstance = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission request placeholder with button
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To scan QR codes & Barcodes in real-time, please grant camera access.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("grant_camera_permission_button")
                ) {
                    Text("Grant Permission", color = Color.White)
                }
            }
        }

        // Viewfinder overlay matching the screenshot (blue corners + red laser line)
        ViewfinderOverlay(
            modifier = Modifier.fillMaxSize()
        )

        // Top Toolbar Overlay matching screenshot: Menu icon on left, Gallery, Flash, Flip Camera on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("drawer_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Gallery pick button
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("gallery_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Scan from Image",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Flash toggle button
                IconButton(
                    onClick = { viewModel.toggleFlash() },
                    modifier = Modifier.testTag("flash_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash",
                        tint = if (isFlashOn) Color(0xFFFFD54F) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Flip camera button
                IconButton(
                    onClick = { viewModel.toggleCamera() },
                    modifier = Modifier.testTag("flip_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip Camera",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Bottom Controls: Clean Zoom Slider matching screenshot
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ZoomOut,
                contentDescription = "Zoom Out",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(26.dp)
            )
            Slider(
                value = zoomLevel,
                onValueChange = { viewModel.setZoom(it) },
                valueRange = 1.0f..5.0f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.85f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .testTag("zoom_slider")
            )
            Icon(
                imageVector = Icons.Default.ZoomIn,
                contentDescription = "Zoom In",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun ViewfinderOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_animation"
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val boxSize = (canvasWidth * 0.78f).coerceAtMost(320.dp.toPx())
        val left = (canvasWidth - boxSize) / 2
        val top = (canvasHeight - boxSize) / 2 - 30.dp.toPx()
        val right = left + boxSize
        val bottom = top + boxSize

        // Draw 4 Blue Corner Guides ([ ])
        val cornerLength = 32.dp.toPx()
        val cornerStroke = 3.5.dp.toPx()
        val cornerColor = Color(0xFF4285F4) // Google Blue matching screenshot

        // Top-Left
        drawLine(cornerColor, Offset(left, top + cornerLength), Offset(left, top), cornerStroke, StrokeCap.Round)
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), cornerStroke, StrokeCap.Round)

        // Top-Right
        drawLine(cornerColor, Offset(right - cornerLength, top), Offset(right, top), cornerStroke, StrokeCap.Round)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLength), cornerStroke, StrokeCap.Round)

        // Bottom-Left
        drawLine(cornerColor, Offset(left, bottom - cornerLength), Offset(left, bottom), cornerStroke, StrokeCap.Round)
        drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLength, bottom), cornerStroke, StrokeCap.Round)

        // Bottom-Right
        drawLine(cornerColor, Offset(right - cornerLength, bottom), Offset(right, bottom), cornerStroke, StrokeCap.Round)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLength), cornerStroke, StrokeCap.Round)

        // Red Laser Horizontal Scan Line
        val laserY = top + (boxSize * laserProgress)
        drawLine(
            color = Color(0xFFE53935),
            start = Offset(left + 4.dp.toPx(), laserY),
            end = Offset(right - 4.dp.toPx(), laserY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
