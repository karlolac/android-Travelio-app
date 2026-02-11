package com.example.travelio.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(onSaveSuccess: () -> Unit) {
    val context = LocalContext.current

    // Stanja unosa
    var destination by remember { mutableStateOf("") }
    var impression by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var isUploading by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> selectedImages = uris }

    val dateRangePickerState = rememberDateRangePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    val startDate = dateRangePickerState.selectedStartDateMillis
    val endDate = dateRangePickerState.selectedEndDateMillis

    val dateDisplay = if (startDate != null && endDate != null) {
        "${formatter.format(Date(startDate))} - ${formatter.format(Date(endDate))}"
    } else {
        "Odaberi datume putovanja"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Novi unos", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(24.dp))

        // Polje za destinaciju
        Text("Destinacija", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gumb za kalendar
        Text("Datumi putovanja", fontWeight = FontWeight.SemiBold)
        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f), contentColor = Color.Black)
        ) {
            Text(dateDisplay)
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } }
            ) {
                DateRangePicker(state = dateRangePickerState, modifier = Modifier.height(400.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dojam i ocjena
        Text("Tvoj dojam", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = impression,
            onValueChange = { impression = it },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < rating) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp).clickable { rating = index + 1 }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Slike prikaz i gumb
        if (selectedImages.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedImages) { uri ->
                    AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                }
            }
        }

        Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null)
            Text(" Odaberi slike")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // GLAVNI GUMB ZA SPREMANJE
        Button(
            onClick = {
                isUploading = true
                val storageRef = FirebaseStorage.getInstance().reference
                val uploadedUrls = mutableListOf<String>()

                if (selectedImages.isEmpty()) {
                    saveTripToFirestore(destination, dateDisplay, impression, rating, emptyList(),
                        { isUploading = false; onSaveSuccess() },
                        { isUploading = false; Toast.makeText(context, "Greška baze", Toast.LENGTH_SHORT).show() }
                    )
                } else {
                    var processedCount = 0
                    selectedImages.forEach { uri ->
                        val fileRef = storageRef.child("trip_images/${UUID.randomUUID()}.jpg")
                        fileRef.putFile(uri).continueWithTask { task ->
                            if (!task.isSuccessful) task.exception?.let { throw it }
                            fileRef.downloadUrl
                        }.addOnCompleteListener { task ->
                            processedCount++
                            if (task.isSuccessful) {
                                uploadedUrls.add(task.result.toString())
                            } else {
                                // ISPISUJE TOČNU GREŠKU ZAŠTO SLIKA NE IDE (npr. No Permission)
                                android.util.Log.e("STORAGE", "Greška: ${task.exception?.message}")
                            }

                            if (processedCount == selectedImages.size) {
                                saveTripToFirestore(destination, dateDisplay, impression, rating, uploadedUrls,
                                    { isUploading = false; onSaveSuccess() },
                                    { isUploading = false; Toast.makeText(context, "Spremljeno bez nekih slika", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        }
                    }
                }
            },
            enabled = !isUploading && destination.isNotEmpty()
        ) {
            if (isUploading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            else Text("Spremi putovanje u Cloud")
        }
    }
}

// POMOĆNA FUNKCIJA - MORA BITI IZVAN AddTripScreen
private fun saveTripToFirestore(dest: String, dates: String, impr: String, rate: Int, urls: List<String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val tripData = hashMapOf(
        "destination" to dest,
        "dates" to dates,
        "impression" to impr,
        "rating" to rate,
        "imageUris" to urls
    )
    db.collection("trips").add(tripData).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
}
