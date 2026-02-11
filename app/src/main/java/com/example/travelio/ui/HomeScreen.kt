package com.example.travelio.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Gallery
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelio.R
import android.net.Uri
import androidx.compose.foundation.clickable
import com.google.firebase.firestore.FirebaseFirestore


data class Trip(
    val id: String = "", // Dodajemo ID kako bismo lakše brisali ili ažurirali
    val destination: String = "",
    val dates: String = "",
    val impression: String = "",
    val rating: Int = 0,
    val imageUris: List<String> = emptyList() // Promijenjeno iz Uri u String
)
//val tripList = mutableStateListOf<Trip>()

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val db = FirebaseFirestore.getInstance()
    // 1. STANJA
    var searchText by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf("home") } // Prati koji je ekran aktivan
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var tripList by remember { mutableStateOf<List<Trip>>(emptyList()) }

    //val filteredTrips = tripList.filter {
    //   it.destination.contains(searchText, ignoreCase = true)
    //}

    LaunchedEffect(Unit) {
        db.collection("trips").addSnapshotListener { value, error ->
            if (value != null) {
                // Mapiranje dokumenata iz baze u tvoju Trip klasu
                val items = value.documents.map { doc ->
                    Trip(
                        id = doc.id,
                        destination = doc.getString("destination") ?: "",
                        dates = doc.getString("dates") ?: "",
                        impression = doc.getString("impression") ?: "",
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        imageUris = (doc.get("imageUris") as? List<String>) ?: emptyList()
                    )
                }
                tripList = items
            }
        }
    }

    if (searchText.isNotEmpty()) {
        val filteredTrips =
            tripList.filter { it.destination.contains(searchText, ignoreCase = true) }
        if (filteredTrips.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column {
                    filteredTrips.forEach { trip ->
                        Text(
                            text = trip.destination,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTrip = trip // Postavljaš koji je trip odabran
                                    searchText = ""     // Čistiš polje
                                    currentScreen = "details" // Šalješ ga na ekran s detaljima
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }

// 3. LOGIKA FILTRIRANJA
    val filteredTrips = tripList.filter {
        it.destination.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White.copy(alpha = 0.9f)) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentScreen == "home",
                    onClick = { currentScreen = "home" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Dodaj") },
                    label = { Text("Dodaj") },
                    selected = currentScreen == "add",
                    onClick = { currentScreen = "add" }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_map_24),
                            contentDescription = "Galerija"
                        )
                    },
                    label = { Text("Putovanja") },
                    selected = currentScreen == "gallery",
                    onClick = { currentScreen = "gallery" }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "home" -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.background),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                            Spacer(modifier = Modifier.height(40.dp))
                            Text(
                                "TRAVELIO",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.DarkGray
                            )

                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "Pretražite svoje putovanje",
                                        color = Color.Gray
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null
                                    )
                                },
                                shape = RoundedCornerShape(15.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.9f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f)
                                )
                            )

                            // Prikaz rezultata pretrage (koristi filteredTrips varijablu)
                            if (searchText.isNotEmpty() && filteredTrips.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    Column {
                                        filteredTrips.forEach { trip ->
                                            Text(
                                                text = trip.destination,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedTrip = trip
                                                        searchText = ""
                                                        currentScreen = "details"
                                                    }
                                                    .padding(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "add" -> AddTripScreen(onSaveSuccess = { currentScreen = "home" })
                "gallery" -> TripsGalleryScreen(
                    onTripClick = { trip ->
                        selectedTrip = trip
                        currentScreen = "details"
                    },
                    currentTripList = tripList // Ovdje šaljemo listu
                )

                "details" -> selectedTrip?.let { trip ->
                    TripDetailsScreen(
                        trip = trip,
                        onBack = { currentScreen = "gallery" },
                        onDelete = {
                            // Logika za brisanje iz Firebasea
                            val db = FirebaseFirestore.getInstance()
                            db.collection("trips").document(trip.id).delete()
                                .addOnSuccessListener {
                                    currentScreen =
                                        "gallery" // Vrati korisnika natrag nakon brisanja
                                }
                                .addOnFailureListener {
                                    // Ovdje možeš dodati obavijest ako brisanje ne uspije
                                }
                        }
                    )
                }
            }
        }
    }
}