package com.example.travelio.ui

import androidx.compose.foundation.clickable
import com.example.travelio.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun TripsGalleryScreen(onTripClick: (Trip) -> Unit,currentTripList: List<Trip>) {
    var sortType by remember { mutableStateOf("A-Z") }

    // Logika sortiranja
    val sortedTrips = when (sortType) {
        "A-Z" -> currentTripList.sortedBy { it.destination.lowercase() }
        "Z-A" -> currentTripList.sortedByDescending { it.destination.lowercase() }
        else -> currentTripList
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text(
                text = "Tvoja putovanja",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = Color.DarkGray
            )

            // Gumb za sortiranje
            TextButton(onClick = { sortType = if (sortType == "A-Z") "Z-A" else "A-Z" }) {
                Text("Sortiraj ($sortType)", color = Color.Blue)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sortedTrips) { trip ->
                Card(
                    shape = RoundedCornerShape(15.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.clickable {
                        onTripClick(trip) // Sada ovo više neće biti crveno!
                    }
                ) {
                    Column {
                        // PRIKAZ SLIKE KOJU SI UNIO PREKO MOBITELA
                        AsyncImage(
                            model = trip.imageUris.firstOrNull() ?: com.example.travelio.R.drawable.background,
                            contentDescription = "Slika: ${trip.destination}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = trip.destination,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )
                            Text(
                                text = trip.dates,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            // Dodajemo i ocjenu (zvijezdice u tekstu) radi dojma
                            Text(
                                text = "⭐ ".repeat(trip.rating),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}