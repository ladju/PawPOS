package com.petshop.pos.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petshop.pos.ui.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    posViewModel: PosViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var manualBarcode by remember { mutableStateOf("") }
    var scanSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Laser scan animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Toggle flashlight */ }) {
                        Icon(Icons.Default.FlashlightOn, contentDescription = "Flashlight")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Point camera at pet product barcode or enter barcode manually",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Scanner Viewport Box
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Animated laser line
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(2.dp)
                        .offset(y = (laserOffset - 100).dp)
                        .background(Color.Red)
                )

                // Corner Reticle markers
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(160.dp)
                )

                Text(
                    text = "ALIGN BARCODE HERE",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Manual Barcode Entry
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    label = { Text("Enter Barcode Number") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (manualBarcode.isNotBlank()) {
                            posViewModel.scanBarcode(manualBarcode)
                            scanSuccessMessage = "Scanned: $manualBarcode"
                            manualBarcode = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Demo Barcode Buttons
            Text(
                text = "⚡ Quick Test Pet Barcodes:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val sampleBarcodes = listOf(
                Triple("8991001", "🐶 Royal Canin Dog", "Food"),
                Triple("8991003", "🐶 Frontline Flea", "Health"),
                Triple("8992001", "🐱 Whiskas Cat", "Food"),
                Triple("8992003", "🐱 Tidy Cats Litter", "Grooming"),
                Triple("8993001", "🦜 Kaytee Bird Seed", "Food"),
                Triple("8994001", "🐠 TetraMin Fish Flakes", "Food")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sampleBarcodes) { (code, title, _) ->
                    FilledTonalButton(
                        onClick = {
                            posViewModel.scanBarcode(code)
                            scanSuccessMessage = "Added: $title ($code)"
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = title, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            scanSuccessMessage?.let { msg ->
                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF047857)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
